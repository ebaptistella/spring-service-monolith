package dev.ebaptistella.monolith.config.modulith;

import io.micrometer.observation.ObservationRegistry;
import org.aopalliance.aop.Advice;
import org.springframework.beans.factory.ObjectProvider;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.Advisor;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.lang.Nullable;
import org.springframework.modulith.observability.support.ModulithObservationConvention;
import org.springframework.modulith.observability.support.ObservedModule;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Spring Modulith 1.4.x registers {@code ModuleEntryInterceptor} without a custom
 * {@link ModulithObservationConvention}, which causes inconsistent Prometheus tag keys.
 * This processor rebinds module advisors to use the application convention bean.
 */
final class ModulithObservationInterceptorPatcher implements BeanPostProcessor, Ordered {

    private static final String MODULE_ADVISOR_SUFFIX = "ApplicationModuleObservingAdvisor";
    private static final String MODULE_INTERCEPTOR_NAME = "org.springframework.modulith.observability.support.ModuleEntryInterceptor";

    private final ModulithObservationConvention observationConvention;
    private final ObjectProvider<ObservationRegistry> observationRegistryProvider;
    private final Environment environment;

    ModulithObservationInterceptorPatcher(
            ModulithObservationConvention observationConvention,
            ObjectProvider<ObservationRegistry> observationRegistryProvider,
            Environment environment) {
        this.observationConvention = observationConvention;
        this.observationRegistryProvider = observationRegistryProvider;
        this.environment = environment;
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 40;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (!(bean instanceof Advised advised)) {
            return bean;
        }
        for (Advisor advisor : advised.getAdvisors()) {
            if (!isModuleObservingAdvisor(advisor)) {
                continue;
            }
            Advice advice = advisor.getAdvice();
            if (!isModuleEntryInterceptor(advice)) {
                continue;
            }
            ObservedModule module = readField(advice, "module", ObservedModule.class);
            MethodInterceptor replacement = createInterceptor(module);
            if (advisor instanceof DefaultPointcutAdvisor pointcutAdvisor) {
                pointcutAdvisor.setAdvice(replacement);
            }
        }
        return bean;
    }

    private MethodInterceptor createInterceptor(ObservedModule module) {
        clearInterceptorCache();
        try {
            Class<?> interceptorType = Class.forName(MODULE_INTERCEPTOR_NAME);
            Method factory = ReflectionUtils.findMethod(
                    interceptorType,
                    "of",
                    ObservedModule.class,
                    ObservationRegistry.class,
                    ModulithObservationConvention.class,
                    Environment.class);
            if (factory == null) {
                throw new IllegalStateException("ModuleEntryInterceptor.of factory method not found");
            }
            ReflectionUtils.makeAccessible(factory);
            return (MethodInterceptor) ReflectionUtils.invokeMethod(
                    factory,
                    null,
                    module,
                    observationRegistryProvider.getObject(),
                    observationConvention,
                    environment);
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException("Spring Modulith observability is not on the classpath", ex);
        }
    }

    private static boolean isModuleObservingAdvisor(Advisor advisor) {
        return advisor.getClass().getName().endsWith(MODULE_ADVISOR_SUFFIX);
    }

    private static boolean isModuleEntryInterceptor(Advice advice) {
        return advice != null && advice.getClass().getName().equals(MODULE_INTERCEPTOR_NAME);
    }

    private static void clearInterceptorCache() {
        try {
            Class<?> interceptorType = Class.forName(MODULE_INTERCEPTOR_NAME);
            Field cacheField = ReflectionUtils.findField(interceptorType, "CACHE");
            if (cacheField == null) {
                return;
            }
            ReflectionUtils.makeAccessible(cacheField);
            Object cache = ReflectionUtils.getField(cacheField, null);
            if (cache instanceof Map<?, ?> map) {
                map.clear();
            }
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException("Spring Modulith observability is not on the classpath", ex);
        }
    }

    @Nullable
    private static <T> T readField(Object target, String fieldName, Class<T> type) {
        Field field = ReflectionUtils.findField(target.getClass(), fieldName, type);
        if (field == null) {
            throw new IllegalStateException("Field '%s' not found on %s".formatted(fieldName, target.getClass()));
        }
        ReflectionUtils.makeAccessible(field);
        return type.cast(ReflectionUtils.getField(field, target));
    }
}

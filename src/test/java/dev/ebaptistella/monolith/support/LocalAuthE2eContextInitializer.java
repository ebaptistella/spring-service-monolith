package dev.ebaptistella.monolith.support;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.springframework.test.util.TestSocketUtils;

public final class LocalAuthE2eContextInitializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    public static final int PORT = TestSocketUtils.findAvailableTcpPort();
    public static final String ISSUER = "http://localhost:" + PORT;

    @Override
    public void initialize(ConfigurableApplicationContext context) {
        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
                context,
                "server.port=" + PORT,
                "local.auth.issuer=" + ISSUER,
                "spring.security.oauth2.resourceserver.jwt.issuer-uri=" + ISSUER,
                "app.security.authorization-server.issuer=" + ISSUER,
                "app.security.jwt.local-issuer=" + ISSUER,
                "app.security.strategies.local=true");
    }

    private LocalAuthE2eContextInitializer() {
    }
}

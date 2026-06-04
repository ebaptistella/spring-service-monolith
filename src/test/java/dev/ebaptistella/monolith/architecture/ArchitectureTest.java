package dev.ebaptistella.monolith.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@Tag("unit")
class ArchitectureTest {

    @ParameterizedTest
    @ValueSource(strings = {"customer", "notification", "email", "identity", "catalog", "inventory", "order", "finance"})
    void logicDoesNotDependOnExternalLayers(String module) {
        JavaClasses classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("dev.ebaptistella.monolith.modules." + module);

        ArchRule rule = noClasses()
                .that().resideInAPackage("..logic..")
                .should().dependOnClassesThat()
                .resideInAnyPackage(
                        "..modules..diplomat..",
                        "..adapters..",
                        "..wire..",
                        "..controllers..")
                .allowEmptyShould(true);

        rule.check(classes);
    }

    @ParameterizedTest
    @ValueSource(strings = {"customer", "notification", "email", "identity", "catalog", "inventory", "order", "finance"})
    void adaptersDoNotDependOnLogicOrControllers(String module) {
        JavaClasses classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("dev.ebaptistella.monolith.modules." + module);

        ArchRule rule = noClasses()
                .that().resideInAPackage("..adapters..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("..logic..", "..controllers..")
                .allowEmptyShould(true);

        rule.check(classes);
    }

    @ParameterizedTest
    @ValueSource(strings = {"notification", "email", "inventory", "order", "finance"})
    void rabbitConsumersDependOnEventAdapters(String module) {
        JavaClasses classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("dev.ebaptistella.monolith.modules." + module);

        ArchRule rule = classes()
                .that().resideInAPackage("..diplomat.consumer..")
                .should().dependOnClassesThat().resideInAPackage("..adapters..")
                .allowEmptyShould(true);

        rule.check(classes);
    }

    @ParameterizedTest
    @ValueSource(strings = {"customer", "catalog", "inventory"})
    void inboundSpiDoesNotDependOnControllers(String module) {
        JavaClasses classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("dev.ebaptistella.monolith.modules." + module);

        ArchRule rule = noClasses()
                .that().resideInAPackage("..diplomat.inbound..")
                .should().dependOnClassesThat()
                .resideInAPackage("..controllers..")
                .allowEmptyShould(true);

        rule.check(classes);
    }

    @ParameterizedTest
    @ValueSource(strings = {"customer", "notification", "email", "identity", "catalog", "inventory", "order", "finance"})
    void controllersDoNotDependOnWire(String module) {
        JavaClasses classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("dev.ebaptistella.monolith.modules." + module);

        ArchRule rule = noClasses()
                .that().resideInAPackage("..controllers..")
                .should().dependOnClassesThat()
                .resideInAPackage("..wire..")
                .allowEmptyShould(true);

        rule.check(classes);
    }

    @ParameterizedTest
    @ValueSource(strings = {"customer", "notification", "email", "identity", "catalog", "inventory", "order", "finance"})
    void httpServerDoesNotDependOnJpaDirectly(String module) {
        JavaClasses classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("dev.ebaptistella.monolith.modules." + module);

        ArchRule rule = noClasses()
                .that().resideInAPackage("..diplomat.http_server..")
                .should().dependOnClassesThat()
                .resideInAPackage("..diplomat.jpa..")
                .allowEmptyShould(true);

        rule.check(classes);
    }

    @ParameterizedTest
    @ValueSource(strings = {"notification", "email", "inventory", "order", "finance"})
    void rabbitConsumersDoNotUseSlf4j(String module) {
        JavaClasses classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("dev.ebaptistella.monolith.modules." + module);

        ArchRule rule = noClasses()
                .that().resideInAPackage("..diplomat.consumer..")
                .should().beAnnotatedWith("lombok.extern.slf4j.Slf4j")
                .allowEmptyShould(true);

        rule.check(classes);
    }
}

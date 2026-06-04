package dev.ebaptistella.monolith.architecture;

import dev.ebaptistella.monolith.MonolithApplication;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

@Tag("unit")
class ModulithStructureTest {

    @Test
    void verifiesModularStructure() {
        ApplicationModules modules = ApplicationModules.of(MonolithApplication.class);
        modules.verify();
        new Documenter(modules).writeModulesAsPlantUml();
    }
}

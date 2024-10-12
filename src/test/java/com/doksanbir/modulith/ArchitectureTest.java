package com.doksanbir.modulith;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;

public class ArchitectureTest {

    public static final DescribedPredicate<JavaClass> IGNORED_MODULES =
            resideInAnyPackage( "com.doksanbir.modulith.shared");

    public static final ApplicationModules modules =
            ApplicationModules.of(ModulithApplication.class, IGNORED_MODULES);


    @Test
    void generateDiagrams() {
        new Documenter(modules)
                .writeModulesAsPlantUml()
                .writeIndividualModulesAsPlantUml();
    }

    @Test
    void encapsulated_and_withoutCycles() {

        for(var module : modules) {
           System.out.println("Module: " + module.getName() + " : "+ module.getBasePackage());
        }
        // 1. modules only access each others' root package (or explicitly allowed packages)
        // 2. no cycles between modules
        modules.verify();
        // Note: this test runs even after split in Maven modules
    }

}

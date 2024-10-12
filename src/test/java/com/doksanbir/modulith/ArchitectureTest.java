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


    /*
        * This test verifies that there are no cycles between packages.
        * This is important to ensure that the codebase is maintainable and that the packages are well structured.
        * The test will fail if there are cycles between packages.
     */
    @Test
    void no_cycles_between_packages() {
        for(var module : modules) {
           System.out.println("Module: " + module.getName() + " : "+ module.getBasePackage());
        }
        modules.verify();
    }

}

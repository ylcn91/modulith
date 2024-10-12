package com.doksanbir.modulith;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.web.bind.annotation.RestController;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;


import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

public class ArchitectureTest {

    public static final DescribedPredicate<JavaClass> IGNORED_MODULES =
            resideInAnyPackage( "com.doksanbir.modulith.shared");

    public static final ApplicationModules modules =
            ApplicationModules.of(ModulithApplication.class, IGNORED_MODULES);

    private final JavaClasses importedClasses = new ClassFileImporter().importPackages("com.doksanbir.modulith");


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

    /*
     * This test ensures that no classes in the "service" layer access the "repository" layer directly.
     * It enforces the rule that services should access repositories through dedicated interfaces.
     */
    @Test
    void services_should_not_access_repositories_directly() {
        noClasses()
                .that().resideInAPackage("..service..")
                .should().dependOnClassesThat().resideInAPackage("..repository..")
                .check(importedClasses);
    }

    /*
     * This test ensures that classes in the domain model (entities) do not depend on service or repository layers.
     */
    @Test
    void domain_model_should_not_depend_on_services_or_repositories() {
        noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage("..service..", "..repository..")
                .check(importedClasses);
    }

    @Test
    void controllers_should_be_annotated_with_rest_controller() {
        classes()
                .that().resideInAPackage("..controller..")
                .should().beAnnotatedWith(RestController.class)
                .check(importedClasses);
    }

    @Test
    void no_field_injection_allowed() {
        fields()
                .should().notBeAnnotatedWith(Autowired.class)
                .check(importedClasses);
    }


    /*
        * This test ensures that all fields in services are private.
        * it violates the encapsulation principle to have public fields in services
     */
    @Test
    void all_fields_in_services_should_be_private() {
        fields()
                .that().areDeclaredInClassesThat().resideInAPackage("..service..")
                .should().bePrivate()
                .check(importedClasses);
    }

}

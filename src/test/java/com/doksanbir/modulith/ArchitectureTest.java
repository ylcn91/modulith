package com.doksanbir.modulith;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

@DisplayName("Architecture Tests")
public class ArchitectureTest {

    public static final DescribedPredicate<JavaClass> IGNORED_MODULES =
            resideInAnyPackage("com.doksanbir.modulith.shared");

    public static final ApplicationModules modules =
            ApplicationModules.of(ModulithApplication.class, IGNORED_MODULES);

    private final JavaClasses importedClasses = new ClassFileImporter().importPackages("com.doksanbir.modulith");

    @Nested
    @DisplayName("Package Structure Rules")
    class PackageStructureRules {

        @Test
        @DisplayName("No Cycles Between Packages")
        void no_cycles_between_packages() {
            for (var module : modules) {
                System.out.println("Module: " + module.getName() + " : " + module.getBasePackage());
            }
            modules.verify();
        }

        @Test
        @DisplayName("Services should not access Repositories directly")
        void services_should_not_access_repositories_directly() {
            noClasses()
                    .that().resideInAPackage("..service..")
                    .should().dependOnClassesThat().resideInAPackage("..repository..")
                    .check(importedClasses);
        }

        @Test
        @DisplayName("Domain model should not depend on Services or Repositories")
        void domain_model_should_not_depend_on_services_or_repositories() {
            noClasses()
                    .that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat().resideInAnyPackage("..service..", "..repository..")
                    .check(importedClasses);
        }
    }

    @Nested
    @DisplayName("Controller Layer Rules")
    class ControllerLayerRules {

        @Test
        @DisplayName("Controllers should be annotated with @RestController")
        void controllers_should_be_annotated_with_rest_controller() {
            classes()
                    .that().resideInAPackage("..controller..")
                    .should().beAnnotatedWith(RestController.class)
                    .check(importedClasses);
        }
    }

    @Nested
    @DisplayName("Field Injection and Access Rules")
    class FieldInjectionAndAccessRules {

        @Test
        @DisplayName("No field injection allowed (Autowired)")
        void no_field_injection_allowed() {
            fields()
                    .should().notBeAnnotatedWith(Autowired.class)
                    .check(importedClasses);
        }

        @Test
        @DisplayName("All fields in services should be private")
        void all_fields_in_services_should_be_private() {
            fields()
                    .that().areDeclaredInClassesThat().resideInAPackage("..service..")
                    .should().bePrivate()
                    .check(importedClasses);
        }
    }

    @Nested
    @DisplayName("Layered Architecture Rules")
    class LayeredArchitectureRules {

        @Test
        @DisplayName("Controllers should only depend on Use Cases")
        void controllers_should_only_depend_on_use_cases() {
            classes()
                    .that().resideInAPackage("..controller..")
                    .should().dependOnClassesThat().resideInAPackage("..application.port.in..")  // Adjust the package according to your use case location
                    .check(importedClasses);
        }


        @Test
        @DisplayName("Services should only depend on Ports (not directly on Repositories)")
        void services_should_only_depend_on_ports() {
            classes()
                    .that().resideInAPackage("..service..")
                    .should().dependOnClassesThat().resideInAPackage("..application.port.out..")
                    .check(importedClasses);
        }


        @Test
        @DisplayName("Repositories should only depend on Domain Entities")
        void repositories_should_only_depend_on_domain_entities() {
            classes()
                    .that().resideInAPackage("..repository..")
                    .should().dependOnClassesThat().resideInAPackage("..domain..")
                    .check(importedClasses);
        }
    }


    @Nested
    @DisplayName("Repository Layer Rules")
    class RepositoryLayerRules {

        @Test
        @DisplayName("Repositories should be interfaces")
        void repositories_should_be_interfaces() {
            classes()
                    .that().resideInAPackage("..repository..")
                    .should().beInterfaces()
                    .check(importedClasses);
        }

        @Test
        @DisplayName("Repositories should be annotated with @Repository")
        void repositories_should_be_annotated_with_repository() {
            classes()
                    .that().resideInAPackage("..repository..")
                    .should().beAnnotatedWith(org.springframework.stereotype.Repository.class)
                    .check(importedClasses);
        }
    }


}

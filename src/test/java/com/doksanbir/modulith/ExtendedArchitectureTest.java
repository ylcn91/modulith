package com.doksanbir.modulith;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

@DisplayName("Extended Architecture Tests")
class ExtendedArchitectureTest {

    private final JavaClasses importedClasses = new ClassFileImporter().importPackages("com.doksanbir.modulith");


    @Nested
    @DisplayName("Layered Architecture Rules")
    class LayeredArchitectureRules {

        @Test
        @DisplayName("Layered architecture should be respected")
        void layered_architecture_should_be_respected() {
            ArchRule rule = layeredArchitecture()
                    .consideringAllDependencies()
                    .layer("Controller").definedBy("..web.controller..")
                    .layer("DTO").definedBy("..web.dto..")
                    .layer("Application").definedBy("..application..")
                    .layer("Domain").definedBy("..domain..")
                    .layer("Infrastructure").definedBy("..infrastructure..")

                    .whereLayer("Controller").mayNotBeAccessedByAnyLayer()
                    .whereLayer("DTO").mayOnlyBeAccessedByLayers("Controller", "Application")
                    .whereLayer("Application").mayOnlyBeAccessedByLayers("Controller", "Infrastructure")
                    .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Infrastructure", "DTO")
                    .whereLayer("Infrastructure").mayOnlyBeAccessedByLayers("Application")

                    // Allow Infrastructure to access Application ports
                    .ignoreDependency(JavaClass.Predicates.resideInAPackage("..infrastructure.."),
                            JavaClass.Predicates.resideInAPackage("..application.port.."))

                    // Allow DTOs to use Domain enums
                    .ignoreDependency(JavaClass.Predicates.resideInAPackage("..web.dto.."),
                            JavaClass.Predicates.resideInAPackage("..domain..").and(JavaClass.Predicates.simpleNameEndingWith("Status")));

            rule.check(importedClasses);
        }
    }

    @Nested
    @DisplayName("Naming Convention Rules")
    class NamingConventionRules {

        @Test
        @DisplayName("Repository classes should be suffixed with 'Repository'")
        void repository_classes_should_be_suffixed_with_repository() {
            classes()
                    .that().resideInAPackage("..repository..")
                    .should().haveSimpleNameEndingWith("Repository")
                    .check(importedClasses);
        }

    }

    @Nested
    @DisplayName("Dependency Rules")
    class DependencyRules {

        @Test
        @DisplayName("Services should not depend on controllers")
        void services_should_not_depend_on_controllers() {
            noClasses()
                    .that().resideInAPackage("..service..")
                    .should().dependOnClassesThat().resideInAPackage("..controller..")
                    .check(importedClasses);
        }

        @Test
        @DisplayName("Domain should not depend on infrastructure")
        void domain_should_not_depend_on_infrastructure() {
            noClasses()
                    .that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat().resideInAnyPackage("..repository..", "..service..", "..controller..")
                    .check(importedClasses);
        }
    }

    @Nested
    @DisplayName("Cyclic Dependency Rules")
    class CyclicDependencyRules {

        @Test
        @DisplayName("Slices should be free of cycles")
        void slices_should_be_free_of_cycles() {
            slices().matching("com.doksanbir.modulith.(*)..").should().beFreeOfCycles()
                    .check(importedClasses);
        }
    }

    @Nested
    @DisplayName("Exception Handling Rules")
    class ExceptionHandlingRules {

        @Test
        @DisplayName("Exception classes should have names ending with Exception")
        void exception_classes_should_have_names_ending_with_exception() {
            classes()
                    .that().areAssignableTo(Exception.class)
                    .should().haveSimpleNameEndingWith("Exception")
                    .check(importedClasses);
        }

        @Test
        @DisplayName("Exceptions should only be caught in controller or service layer")
        void exceptions_should_only_be_caught_in_controller_or_service_layer() {
            methods()
                    .that().areDeclaredInClassesThat().resideOutsideOfPackages("..controller..", "..service..")
                    .should().notDeclareThrowableOfType(Exception.class)
                    .check(importedClasses);
        }
    }

    @Nested
    @DisplayName("Port Usage Rules")
    class PortUsageRules {

        @Test
        @DisplayName("Input ports should only be used by controllers or other application services")
        void input_ports_should_only_be_used_by_controllers_or_application_services() {
            noClasses()
                    .that().resideOutsideOfPackages("..web.controller..", "..application.service..")
                    .should().dependOnClassesThat().resideInAPackage("..application.port.in..")
                    .check(importedClasses);
        }

        @Test
        @DisplayName("Output ports should only be implemented by infrastructure adapters")
        void output_ports_should_only_be_implemented_by_infrastructure_adapters() {
            classes()
                    .that().resideInAPackage("..application.port.out..")
                    .should().beInterfaces()
                    .check(importedClasses);

            classes()
                    .that().implement(JavaClass.Predicates.resideInAPackage("..application.port.out.."))
                    .should().resideInAPackage("..infrastructure.adapter..")
                    .check(importedClasses);
        }
    }

    @Nested
    @DisplayName("Adapter Rules")
    class AdapterRules {

        @Test
        @DisplayName("Adapters should implement output ports")
        void adapters_should_implement_output_ports() {
            classes()
                    .that().resideInAPackage("..infrastructure.adapter..")
                    .should().implement(JavaClass.Predicates.resideInAPackage("..application.port.out.."))
                    .check(importedClasses);
        }

        @Test
        @DisplayName("Adapters should not be used directly by application services")
        void adapters_should_not_be_used_directly_by_application_services() {
            noClasses()
                    .that().resideInAPackage("..application.service..")
                    .should().dependOnClassesThat().resideInAPackage("..infrastructure.adapter..")
                    .check(importedClasses);
        }
    }

    @Nested
    @DisplayName("DTO Usage Rules")
    class DTOUsageRules {

        @Test
        @DisplayName("DTOs should not be used in the domain layer")
        void dtos_should_not_be_used_in_domain_layer() {
            noClasses()
                    .that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat().resideInAPackage("..web.dto..")
                    .check(importedClasses);
        }
    }
}
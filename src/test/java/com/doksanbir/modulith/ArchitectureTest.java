package com.doksanbir.modulith;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.jmolecules.event.annotation.DomainEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;
import static com.tngtech.archunit.library.GeneralCodingRules.*;

@DisplayName("Architecture Tests")
public class ArchitectureTest {

    public static final DescribedPredicate<JavaClass> IGNORED_MODULES =
            resideInAnyPackage("com.doksanbir.modulith.shared..");

    public static final ApplicationModules modules =
            ApplicationModules.of(ModulithApplication.class, IGNORED_MODULES);

    private final JavaClasses importedClasses = new ClassFileImporter().importPackages("com.doksanbir.modulith");

    @Nested
    @DisplayName("Package Structure Rules")
    class PackageStructureRules {

        @Test
        @DisplayName("No Cycles Between Packages")
        void no_cycles_between_packages() {
            /*
            for (var module : modules) {
                System.out.println("Module: " + module.getName() + " : " + module.getBasePackage());
            }
             */
            modules.verify();
        }

        // Uncomment the following test to generate PlantUML diagrams and Asciidoc documentation
        /*
        @Test
        void generateDiagrams() {
            new Documenter(modules)
                    .writeModulesAsPlantUml()
                    .writeIndividualModulesAsPlantUml();
        }

         */

        /*
        // Uncomment the following test to generate Asciidoc documentation
        @Test
        void generateAsciidoc() {
            var canvasOptions = Documenter.CanvasOptions.defaults();

            var docOptions = Documenter.DiagramOptions.defaults()
                    .withStyle(Documenter.DiagramOptions.DiagramStyle.UML);

            new Documenter(modules) //
                    .writeDocumentation(docOptions, canvasOptions);
        }

         */

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
        @DisplayName("Classes with @Component, @Service, or @Controller should use constructor injection")
        void should_use_constructor_injection() {
            classes()
                    .that().areAnnotatedWith(Component.class)
                    .or().areAnnotatedWith(Service.class)
                    .or().areAnnotatedWith(Controller.class)
                    .should(new ArchCondition<>("use constructor injection") {
                        @Override
                        public void check(JavaClass javaClass, ConditionEvents events) {
                            boolean hasConstructorWithParameters = javaClass.getConstructors().stream()
                                    .anyMatch(constructor -> !constructor.getParameters().isEmpty());

                            if (!hasConstructorWithParameters) {
                                String message = String.format("%s does not use constructor injection", javaClass.getName());
                                events.add(SimpleConditionEvent.violated(javaClass, message));
                            }
                        }
                    })
                    .because("Classes with dependencies should use constructor injection to manage dependencies")
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
        @DisplayName("Services should only depend on allowed packages")
        void services_should_only_depend_on_allowed_packages() {
            classes()
                    .that().resideInAPackage("..service..")
                    .should().onlyDependOnClassesThat().resideInAnyPackage(
                            "..application.port.out..",
                            "..application.port.in..",
                            "..domain..",
                            "..web.dto..",
                            "com.doksanbir.modulith.shared..",
                            "com.doksanbir.modulith.shared.events..",
                            "jakarta.persistence..",
                            "java..",
                            "org.springframework..",
                            "org.slf4j..",
                            "lombok.."
                    )
                    .because("Services should only depend on specific allowed packages")
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


    @Nested
    @DisplayName("Domain Layer Rules")
    class DomainLayerRules {

        @Test
        @DisplayName("Domain layer should only depend on core Java, Jakarta, Lombok, and other domain classes")
        void domain_layer_should_not_depend_on_infrastructure() {
            classes()
                    .that().resideInAPackage("..domain..")
                    .should().onlyDependOnClassesThat(resideInAnyPackage(
                            "java..",                     // Core Java
                            "jakarta.persistence..",       // JPA for entities
                            "org.springframework.stereotype..", // Spring stereotypes (e.g., @Entity)
                            "lombok..",                   // Allow Lombok annotations (e.g., @Builder, @Getter, @Setter)
                            "..domain.."                   // Self-contained domain logic
                    ))
                    .because("Domain layer should not depend on infrastructure or external libraries except allowed ones")
                    .check(importedClasses);
        }
    }


    @Nested
    @DisplayName("Event Architecture Rules")
    class EventArchitectureRules {

        @Test
        @DisplayName("Event classes should be annotated with @DomainEvent and reside in the 'events' package")
        void event_classes_should_be_annotated_with_domain_event_and_reside_in_events_package() {
            classes()
                    .that().areAnnotatedWith(DomainEvent.class)
                    .should().resideInAPackage("..shared.events..")
                    .because("All event classes should be annotated with @DomainEvent and located in the 'events' package")
                    .check(importedClasses);
        }

        @Test
        @DisplayName("Only Services should publish events")
        void only_services_should_publish_events() {
            classes()
                    .that().areAnnotatedWith(Service.class)
                    .and().resideInAPackage("..application.service..")
                    .should().dependOnClassesThat().areAnnotatedWith(DomainEvent.class)
                    .because("Only services should publish domain events")
                    .check(importedClasses);
        }


        @Test
        @DisplayName("No cyclic dependencies between event publishers and listeners")
        void no_cyclic_dependencies_between_publishers_and_listeners() {
            noClasses()
                    .that().resideInAPackage("..product.application.service..")
                    .should().dependOnClassesThat().resideInAPackage("..inventory.application.service..")
                    .because("Publishers should not depend on listeners to avoid cyclic dependencies")
                    .check(importedClasses);
        }

        @Test
        @DisplayName("All event handler methods should be annotated with @ApplicationModuleListener")
        void all_event_handler_methods_should_be_annotated() {
            methods()
                    .that().areDeclaredInClassesThat().resideInAPackage("..inventory.application.service..")
                    .and().haveNameStartingWith("handle")
                    .should().beAnnotatedWith(ApplicationModuleListener.class)
                    .because("All event handler methods should be annotated with @ApplicationModuleListener")
                    .check(importedClasses);
        }
    }


    @Nested
    @DisplayName("Service Layer Rules")
    class ServiceLayerRules {

        @Test
        @DisplayName("Services should only depend on allowed packages (use cases, domain, DTOs, etc.)")
        void services_should_only_depend_on_allowed_packages() {
            classes()
                    .that().resideInAPackage("..service..")
                    .should().onlyDependOnClassesThat().resideInAnyPackage(
                            "..domain..",
                            "..application.port.in..",
                            "..application.port.out..",
                            "org.springframework..",
                            "com.doksanbir.modulith.shared..",
                            "java..",
                            "jakarta..",
                            "lombok..",
                            "org.slf4j..",    // Allow SLF4J for logging
                            "..web.dto.."     // Allow DTOs in service layer
                    )
                    .because("Services should only depend on specific allowed packages to keep separation of concerns")
                    .check(importedClasses);
        }
    }


    @Nested
    @DisplayName("Prohibited Standard Streams")
    class StandardStreamsTest {

        @Test
        @DisplayName("No classes should access standard streams")
        void no_classes_should_access_standard_streams() {
            NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS.check(importedClasses);
        }

        @Test
        @DisplayName("No classes should use java.util.logging")
        void no_classes_should_use_java_util_logging() {
            NO_CLASSES_SHOULD_USE_JAVA_UTIL_LOGGING.check(importedClasses);
        }
    }


    @Nested
    @DisplayName("General Coding Rules")
    class GeneralCodingRulesTest {

        @Test
        @DisplayName("No field injection allowed")
        void no_field_injection() {
            NO_CLASSES_SHOULD_USE_FIELD_INJECTION.check(importedClasses);
        }

        @Test
        @DisplayName("Loggers should be private static final")
        void loggers_should_be_private_static_final() {
            fields()
                    .that().haveRawType(org.slf4j.Logger.class)
                    .should().bePrivate()
                    .andShould().beStatic()
                    .andShould().beFinal()
                    .because("we agreed on this convention for loggers")
                    .check(importedClasses);
        }
    }


    @Nested
    @DisplayName("Naming Convention Rules")
    class NamingConventionRules {

        @Test
        @DisplayName("Controller classes should end with 'Controller'")
        void controller_classes_should_end_with_controller() {
            classes()
                    .that().resideInAPackage("..controller..")
                    .should().haveSimpleNameEndingWith("Controller")
                    .check(importedClasses);
        }

        @Test
        @DisplayName("Service classes should end with 'Service'")
        void service_classes_should_end_with_service() {
            classes()
                    .that().resideInAPackage("..service..")
                    .and().areNotAnonymousClasses() // This line excludes anonymous classes
                    .should().haveSimpleNameEndingWith("Service")
                    .check(importedClasses);
        }

        @Test
        @DisplayName("Repository interfaces should end with 'Repository'")
        void repository_interfaces_should_end_with_repository() {
            classes()
                    .that().resideInAPackage("..repository..")
                    .should().haveSimpleNameEndingWith("Repository")
                    .check(importedClasses);
        }
    }


}

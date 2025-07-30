package com.codehacks.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * Architecture tests for the email-service using ArchUnit
 */
class EmailServiceArchitectureTest {

    private static JavaClasses importedClasses;

    @BeforeAll
    static void setUp() {
        importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.codehacks.email");
    }

    @Test
    void layerDependenciesAreRespected() {
        layeredArchitecture()
                .consideringAllDependencies()
                .layer("Controllers").definedBy("..controller..")
                .layer("Services").definedBy("..service..")
                .layer("Repositories").definedBy("..repository..")
                .layer("Models").definedBy("..model..")
                .layer("DTOs").definedBy("..dto..")
                .layer("Scheduler").definedBy("..scheduler..")
                .layer("Exception").definedBy("..exception..")

                .whereLayer("Controllers").mayNotBeAccessedByAnyLayer()
                .whereLayer("Services").mayOnlyBeAccessedByLayers("Controllers", "Scheduler")
                .whereLayer("Repositories").mayOnlyBeAccessedByLayers("Services")
                .whereLayer("Models").mayOnlyBeAccessedByLayers("Services", "Repositories", "Controllers", "DTOs")
                .whereLayer("DTOs").mayOnlyBeAccessedByLayers("Controllers", "Services")
                .whereLayer("Scheduler").mayNotBeAccessedByAnyLayer()
                .whereLayer("Exception").mayOnlyBeAccessedByLayers("Controllers", "Services")

                .check(importedClasses);
    }

    @Test
    void controllerClassesShouldBeInControllerPackage() {
        ArchRule rule = classes()
                .that().haveSimpleNameEndingWith("Controller")
                .should().resideInAPackage("..controller..");

        rule.check(importedClasses);
    }

    @Test
    void serviceClassesShouldBeInServicePackage() {
        ArchRule rule = classes()
                .that().haveSimpleNameEndingWith("Service")
                .should().resideInAPackage("..service..");

        rule.check(importedClasses);
    }

    @Test
    void repositoryClassesShouldBeInRepositoryPackage() {
        ArchRule rule = classes()
                .that().haveSimpleNameEndingWith("Repository")
                .should().resideInAPackage("..repository..");

        rule.check(importedClasses);
    }

    @Test
    void entityClassesShouldBeInModelPackage() {
        ArchRule rule = classes()
                .that().areAnnotatedWith("jakarta.persistence.Entity")
                .should().resideInAPackage("..model..");

        rule.check(importedClasses);
    }

    @Test
    void dtoClassesShouldBeInDtoPackage() {
        ArchRule rule = classes()
                .that().haveSimpleNameEndingWith("Request")
                .or().haveSimpleNameEndingWith("Response")
                .should().resideInAPackage("..dto..");

        rule.check(importedClasses);
    }

    @Test
    void schedulerClassesShouldBeInSchedulerPackage() {
        ArchRule rule = classes()
                .that().haveSimpleNameEndingWith("Scheduler")
                .should().resideInAPackage("..scheduler..");

        rule.check(importedClasses);
    }

    @Test
    void exceptionClassesShouldBeInExceptionPackage() {
        ArchRule rule = classes()
                .that().haveSimpleNameEndingWith("Exception")
                .should().resideInAPackage("..exception..");

        rule.check(importedClasses);
    }

    @Test
    void controllersShouldBeAnnotatedWithRestController() {
        ArchRule rule = classes()
                .that().resideInAPackage("..controller..")
                .should().beAnnotatedWith("org.springframework.web.bind.annotation.RestController");

        rule.check(importedClasses);
    }

    @Test
    void servicesShouldBeAnnotatedWithService() {
        ArchRule rule = classes()
                .that().resideInAPackage("..service..")
                .and().areNotInterfaces()
                .should().beAnnotatedWith("org.springframework.stereotype.Service");

        rule.check(importedClasses);
    }

    @Test
    void repositoriesShouldExtendJpaRepository() {
        ArchRule rule = classes()
                .that().resideInAPackage("..repository..")
                .should().beInterfaces()
                .andShould().beAssignableTo("org.springframework.data.jpa.repository.JpaRepository");

        rule.check(importedClasses);
    }

    @Test
    void schedulerClassesShouldBeAnnotatedWithComponent() {
        ArchRule rule = classes()
                .that().resideInAPackage("..scheduler..")
                .should().beAnnotatedWith("org.springframework.stereotype.Component");

        rule.check(importedClasses);
    }

    @Test
    void noCircularDependencies() {
        slices().matching("com.codehacks.email.(*)..")
                .should().beFreeOfCycles()
                .check(importedClasses);
    }

    @Test
    void noDirectAccessToRepositoriesFromControllers() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..controller..")
                .should().dependOnClassesThat().resideInAPackage("..repository..");

        rule.check(importedClasses);
    }

    @Test
    void noDirectAccessToModelsFromControllers() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..controller..")
                .should().dependOnClassesThat().resideInAPackage("..model..");

        rule.check(importedClasses);
    }

    @Test
    void servicesShouldNotDependOnControllers() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..service..")
                .should().dependOnClassesThat().resideInAPackage("..controller..");

        rule.check(importedClasses);
    }

    @Test
    void repositoriesShouldNotDependOnServices() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..repository..")
                .should().dependOnClassesThat().resideInAPackage("..service..");

        rule.check(importedClasses);
    }

    @Test
    void modelsShouldNotDependOnServices() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..model..")
                .should().dependOnClassesThat().resideInAPackage("..service..");

        rule.check(importedClasses);
    }

    @Test
    void modelsShouldNotDependOnControllers() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..model..")
                .should().dependOnClassesThat().resideInAPackage("..controller..");

        rule.check(importedClasses);
    }

    @Test
    void dtosShouldNotDependOnServices() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..dto..")
                .should().dependOnClassesThat().resideInAPackage("..service..");

        rule.check(importedClasses);
    }

    @Test
    void dtosShouldNotDependOnControllers() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..dto..")
                .should().dependOnClassesThat().resideInAPackage("..controller..");

        rule.check(importedClasses);
    }

    @Test
    void dtosShouldNotDependOnRepositories() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..dto..")
                .should().dependOnClassesThat().resideInAPackage("..repository..");

        rule.check(importedClasses);
    }

    @Test
    void controllerMethodsShouldBeAnnotatedWithRequestMapping() {
        ArchRule rule = methods()
                .that().arePublic()
                .and().areDeclaredInClassesThat().resideInAPackage("..controller..")
                .should().beAnnotatedWith("org.springframework.web.bind.annotation.GetMapping")
                .orShould().beAnnotatedWith("org.springframework.web.bind.annotation.PostMapping")
                .orShould().beAnnotatedWith("org.springframework.web.bind.annotation.PutMapping")
                .orShould().beAnnotatedWith("org.springframework.web.bind.annotation.DeleteMapping")
                .orShould().beAnnotatedWith("org.springframework.web.bind.annotation.PatchMapping");

        rule.check(importedClasses);
    }

    @Test
    void serviceMethodsShouldBeTransactional() {
        ArchRule rule = methods()
                .that().arePublic()
                .and().areDeclaredInClassesThat().resideInAPackage("..service..")
                .and().haveNameNotMatching(".*find.*|.*get.*|.*load.*|.*read.*|.*generate.*")
                .should().beAnnotatedWith("org.springframework.transaction.annotation.Transactional");

        rule.check(importedClasses);
    }

    @Test
    void exceptionClassesShouldExtendRuntimeException() {
        ArchRule rule = classes()
                .that().resideInAPackage("..exception..")
                .should().beAssignableTo("java.lang.RuntimeException");

        rule.check(importedClasses);
    }

    @Test
    void schedulerMethodsShouldBeAnnotatedWithScheduled() {
        ArchRule rule = methods()
                .that().arePublic()
                .and().areDeclaredInClassesThat().resideInAPackage("..scheduler..")
                .should().beAnnotatedWith("org.springframework.scheduling.annotation.Scheduled");

        rule.check(importedClasses);
    }

    @Test
    void dtoClassesShouldBeRecords() {
        ArchRule rule = classes()
                .that().resideInAPackage("..dto..")
                .and().haveSimpleNameEndingWith("Request")
                .or().haveSimpleNameEndingWith("Response")
                .should().beRecords();

        rule.check(importedClasses);
    }
} 
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
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * Architecture tests using ArchUnit to enforce architectural rules
 */
class ArchitectureTest {

    private static JavaClasses importedClasses;

    @BeforeAll
    static void setUp() {
        importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.codehacks");
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
                .and().resideInAnyPackage("..service..", "..config..")
                .should().resideInAPackage("..service..")
                .orShould().resideInAPackage("..config..");

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
    void configClassesShouldBeInConfigPackage() {
        ArchRule rule = classes()
                .that().haveSimpleNameEndingWith("Config")
                .should().resideInAPackage("..config..");

        rule.check(importedClasses);
    }

    @Test
    void exceptionClassesShouldBeInExceptionPackage() {
        ArchRule rule = classes()
                .that().haveSimpleNameEndingWith("Exception")
                .and().resideInAPackage("..exception..")
                .should().resideInAPackage("..exception..")
                .allowEmptyShould(true);

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
    void noCircularDependencies() {
        slices().matching("com.codehacks.(*)..")
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
    void exceptionClassesShouldExtendRuntimeException() {
        ArchRule rule = classes()
                .that().resideInAPackage("..exception..")
                .and().haveSimpleNameEndingWith("Exception")
                .should().beAssignableTo("java.lang.RuntimeException")
                .allowEmptyShould(true);

        rule.check(importedClasses);
    }

    @Test
    void configClassesShouldBeAnnotatedWithConfiguration() {
        ArchRule rule = classes()
                .that().resideInAPackage("..config..")
                .and().haveSimpleNameEndingWith("Config")
                .should().beAnnotatedWith("org.springframework.context.annotation.Configuration");

        rule.check(importedClasses);
    }

    @Test
    void clientClassesShouldBeAnnotatedWithService() {
        ArchRule rule = classes()
                .that().resideInAPackage("..client..")
                .should().beAnnotatedWith("org.springframework.stereotype.Service");

        rule.check(importedClasses);
    }
} 
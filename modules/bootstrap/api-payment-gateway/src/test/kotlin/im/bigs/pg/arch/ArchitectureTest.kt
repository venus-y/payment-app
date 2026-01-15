package im.bigs.pg.arch

import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.library.Architectures.layeredArchitecture
import org.junit.jupiter.api.Test

class ArchitectureTest {

    private val classes = ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackages("im.bigs.pg")

    @Test
    fun `계층간 의존성 방향 검사`() {


        layeredArchitecture().consideringOnlyDependenciesInLayers()
            .layer("Domain").definedBy("..domain..")
            .layer("Application").definedBy("..application..")
            .layer("Infrastructure").definedBy("..infra..")
            .layer("External").definedBy("..external..")
            .layer("Bootstrap").definedBy("..api..", "..bootstrap..")

            .whereLayer("Domain").mayOnlyBeAccessedByLayers(
                "Application", "Infrastructure", "External", "Bootstrap"
            )
            .whereLayer("Application").mayOnlyBeAccessedByLayers(
                "Infrastructure", "External", "Bootstrap"
            )
            .whereLayer("Infrastructure").mayOnlyBeAccessedByLayers("Bootstrap")
            .whereLayer("External").mayOnlyBeAccessedByLayers("Bootstrap")

            .whereLayer("Domain").mayNotAccessAnyLayer()
            .whereLayer("Application").mayOnlyAccessLayers("Domain")
            .whereLayer("Infrastructure").mayOnlyAccessLayers("Application", "Domain")
            .whereLayer("External").mayOnlyAccessLayers("Application", "Domain")
            .whereLayer("Bootstrap").mayOnlyAccessLayers(
                "Application", "Infrastructure", "External", "Domain"
            )

            .check(classes)
    }
}

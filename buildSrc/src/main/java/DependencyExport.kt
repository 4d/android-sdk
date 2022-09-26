import org.gradle.api.Plugin
import org.gradle.api.Project

internal class DependencyExport : Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.create("mavenDependencyExport", MavenDependencyExport::class.java)
    }
}
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.util.concurrent.atomic.AtomicInteger
import org.apache.maven.model.Parent
import org.apache.maven.model.Repository
import org.apache.maven.model.building.ModelSource
import org.apache.maven.model.resolution.InvalidRepositoryException
import org.apache.maven.model.resolution.ModelResolver
import org.apache.maven.model.resolution.UnresolvableModelException
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency

@Suppress("Deprecation")
class ModelResolverImpl(
    private val taskName: String,
    private val project: Project,
    private val listener: ModelResolveListener
) :
    ModelResolver {
    private val configurationCount: AtomicInteger = AtomicInteger(0)

    @Throws(UnresolvableModelException::class)
    override fun resolveModel(
        groupId: String?,
        artifactId: String?,
        version: String?
    ): ModelSource {
        val configName = java.lang.String.format(
            "%s%s",
            taskName, configurationCount.getAndIncrement()
        )
        val config: Configuration = project.configurations.create(configName)
        config.isTransitive = false
        val depNotation = String.format("%s:%s:%s@pom", groupId, artifactId, version)
        val dependency: Dependency = project.dependencies.create(depNotation)
        config.dependencies.add(dependency)
        val pomXml: File = config.singleFile
        listener.onResolveModel(groupId, artifactId, version, pomXml)

        return object : ModelSource {

            override fun getInputStream(): InputStream {
                return FileInputStream(pomXml)
            }

            override fun getLocation(): String {
                return pomXml.absolutePath
            }
        }
    }

    @Throws(UnresolvableModelException::class)
    override fun resolveModel(parent: Parent): ModelSource {
        return resolveModel(parent.getGroupId(), parent.getArtifactId(), parent.getVersion())
    }

    @Throws(UnresolvableModelException::class)
    override fun resolveModel(dependency: org.apache.maven.model.Dependency): ModelSource {
        return resolveModel(dependency.groupId, dependency.artifactId, dependency.version)
    }

    @Throws(InvalidRepositoryException::class)
    override fun addRepository(repository: Repository?) {
        // ignore
    }

    @Throws(InvalidRepositoryException::class)
    override fun addRepository(repository: Repository?, replace: Boolean) {
        // ignore
    }

    override fun newCopy(): ModelResolver {
        return this
    }
}
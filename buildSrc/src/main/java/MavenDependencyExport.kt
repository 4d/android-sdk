import org.apache.maven.model.building.DefaultModelBuilderFactory
import org.apache.maven.model.building.DefaultModelBuildingRequest
import org.apache.maven.model.building.ModelBuildingRequest
import org.apache.maven.model.resolution.ModelResolver
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.FileCollection
import org.gradle.language.base.artifact.SourcesArtifact
import org.gradle.language.java.artifact.JavadocArtifact
import java.io.File
import java.util.*
import org.gradle.api.artifacts.ModuleVersionIdentifier
import org.gradle.api.artifacts.component.ComponentIdentifier
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.result.*
import org.gradle.api.component.Artifact
import org.gradle.api.tasks.*
import org.gradle.jvm.JvmLibrary
import org.gradle.maven.MavenModule
import org.gradle.maven.MavenPomArtifact

open class MavenDependencyExport : DefaultTask() {

    @Internal
    val configurations: MutableList<Configuration> = mutableListOf()
    @Input
    var systemProperties: Properties = System.getProperties()
    @Input
    var exportSources: Boolean = false
    @Input
    var exportJavadoc: Boolean = false

    @InputFiles
    fun getInputFiles(): FileCollection {
        return project.files(prepareConfigurations())
    }

    @OutputDirectory
    var exportDir: File = File(project.buildDir, "maven-dependency-export")

    private fun prepareConfigurations(): List<Configuration> {
        if (configurations.isEmpty()) return configurations

        val defaultConfigurations: MutableList<Configuration> = mutableListOf()
        defaultConfigurations.addAll(project.buildscript.configurations.filter { it.isCanBeResolved })
        defaultConfigurations.addAll(project.configurations.filter { it.isCanBeResolved })
        return defaultConfigurations
    }

    fun setConfiguration(configs: List<Configuration>) {
        configs.forEach { conf ->
            configuration(conf)
        }
    }

    fun configuration(name: String) {
        val config: Configuration = project.configurations.getByName(name)
        config.isCanBeResolved = true
        if (config.isCanBeResolved) {
            configurations.add(config)
        } else {
            logger.warn("Configuration ${config.name} was not added cause it is not resolvable.")
        }
    }

    fun configuration(configuration: Configuration) {
        if (configuration.isCanBeResolved) {
            configurations.add(configuration)
        } else {
            logger.warn("Configuration ${configuration.name} was not added cause it is not resolvable.")
        }
    }

    @TaskAction
    fun build() {
        val resolveListener = object : ModelResolveListener {
            override fun onResolveModel(
                groupId: String?,
                artifactId: String?,
                version: String?,
                pomFile: File?
            ) {
                if (groupId != null && artifactId != null && version != null && pomFile != null)
                    copyAssociatedPom(groupId, artifactId, version, pomFile)
            }
        }

        val modelResolver: ModelResolver = ModelResolverImpl(name, project, resolveListener)
        prepareConfigurations().forEach { config ->
            logger.info("Exporting ${config.name}...")
            copyJars(config)
            copyPoms(config, modelResolver)
            if (exportSources)
                copyAdditionalArtifacts(config, SourcesArtifact::class.java)
            if (exportJavadoc)
                copyAdditionalArtifacts(config, JavadocArtifact::class.java)
        }

        val exportedPaths: MutableSet<String> = TreeSet()
        project.fileTree(exportDir).visit {
            if (!it.isDirectory) {
                exportedPaths.add(it.relativePath.pathString)
            }
        }

        logger.info("Exported ${exportedPaths.size} files to $exportDir")
        exportedPaths.forEach {
            logger.info("   $it")
        }
    }

    private fun copyJars(config: Configuration) {
        config.resolvedConfiguration.resolvedArtifacts.forEach { artifact ->
            val moduleVersionId: ModuleVersionIdentifier = artifact.moduleVersion.id
            val moduleDir: File = File(
                exportDir,
                getPath(moduleVersionId.group, moduleVersionId.name, moduleVersionId.version)
            )
            project.mkdir(moduleDir)
            project.copy { copy ->
                copy.from(artifact.file)
                copy.into(moduleDir)
            }
        }
    }

    private fun <T : Artifact> copyAdditionalArtifacts(
        config: Configuration,
        artifactType: Class<T>
    ) {
        val componentIds: MutableList<ComponentIdentifier> = mutableListOf()
        config.incoming.resolutionResult.allDependencies.forEach { dependencyResult ->
            if (dependencyResult is ResolvedDependencyResult) {
                val selectedId = dependencyResult.selected.id
                componentIds.add(selectedId)
            }
        }

        val result: ArtifactResolutionResult = project.dependencies.createArtifactResolutionQuery()
            .forComponents(componentIds)
            .withArtifacts(JvmLibrary::class.java, artifactType)
            .execute()

        result.resolvedComponents.forEach { component ->
            val componentId: ComponentIdentifier = component.id
            if (componentId is ModuleComponentIdentifier) {
                val moduleDir: File = File(
                    exportDir,
                    getPath(componentId.group, componentId.module, componentId.version)
                )
                project.mkdir(moduleDir)
                val artifacts = component.getArtifacts(artifactType)
                artifacts.forEach { artifactResult: ArtifactResult ->
                    if (artifactResult is ResolvedArtifactResult) {
                        val file: File = artifactResult.file
                        project.copy { copy ->
                            copy.from(file)
                            copy.into(moduleDir)
                        }
                    }
                }
            }
        }
    }

    private fun copyPoms(config: Configuration, modelResolver: ModelResolver) {
        val componentIds: MutableList<ComponentIdentifier> = mutableListOf()
        config.incoming.resolutionResult.allDependencies.forEach { dependencyResult ->
            if (dependencyResult is ResolvedDependencyResult) {
                val selectedId = dependencyResult.selected.id
                componentIds.add(selectedId)
            }
        }

        val result: ArtifactResolutionResult = project.dependencies.createArtifactResolutionQuery()
            .forComponents(componentIds)
            .withArtifacts(MavenModule::class.java, MavenPomArtifact::class.java)
            .execute()

        val factory = DefaultModelBuilderFactory()
        val builder = factory.newInstance()

        result.resolvedComponents.forEach { component ->
            val componentId: ComponentIdentifier = component.id
            if (componentId is ModuleComponentIdentifier) {
                val moduleDir: File = File(
                    exportDir,
                    getPath(componentId.group, componentId.module, componentId.version)
                )
                project.mkdir(moduleDir)
                component.getArtifacts(MavenPomArtifact::class.java)
                    .forEach { artifactResult: ArtifactResult ->
                        if (artifactResult is ResolvedArtifactResult) {
                            val pomFile: File = artifactResult.file
                            project.copy { copy ->
                                copy.from(pomFile)
                                copy.into(moduleDir)
                            }

                            // force the parent POMs and BOMs to be downloaded and copied
                            try {
                                val req: ModelBuildingRequest = DefaultModelBuildingRequest()
                                req.modelResolver = modelResolver
                                req.pomFile = pomFile
                                req.systemProperties = systemProperties
                                req.validationLevel = ModelBuildingRequest.VALIDATION_LEVEL_MINIMAL

                                // execute the model building request
                                builder.build(req).effectiveModel
                            } catch (e: Exception) {
                                logger.error("Error resolving $pomFile", e)
                            }
                        }
                    }
            }
        }
    }

    fun copyAssociatedPom(groupId: String, artifactId: String, version: String, pomFile: File) {
        val moduleDir: File = File(exportDir, getPath(groupId, artifactId, version))
        project.mkdir(moduleDir)
        project.copy { copy ->
            copy.from(pomFile)
            copy.into(moduleDir)
        }
    }

    private fun getPath(group: String, module: String, version: String): String {
        return "${group.replace(".", "/")}/$module/$version"
    }
}
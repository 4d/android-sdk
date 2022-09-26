import java.io.File

interface ModelResolveListener {
    fun onResolveModel(groupId: String?, artifactId: String?, version: String?, pomFile: File?)
}
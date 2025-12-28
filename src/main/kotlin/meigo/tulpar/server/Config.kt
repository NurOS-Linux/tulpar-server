package meigo.tulpar.server

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addFileSource
import com.sksamuel.hoplite.addResourceSource
import java.io.File

data class TulparConfig(
    val api: ApiConfig,
    val server: ServerConfig,
    val cli: CliConfig
)

data class ApiConfig(
    val config: Int
)

data class ServerConfig(
    val address: String = "0.0.0.0",
    val port: Int = 8080,
    val runInBackground: Boolean = false,
    val maxRequests: Int = 50,
    val blockDurationMillis: Long = 60000,
    val logFile: String = "./server.log",
    val httpsRedirect: Boolean = false,
    val maxDownloadsPerIP: Int = 2,
    val maxDownloadSpeed: Long = 1048576,
    val bufferSize: Int = 1024
)

data class CliConfig(
    val color: String = "#cccccc",
    val hello: List<String> = emptyList()
)

object ConfigFactory {
    fun load(configFile: File): TulparConfig {
        val builder = ConfigLoaderBuilder.default()

        if (configFile.exists()) {
            builder.addFileSource(configFile)
        }

        builder.addResourceSource("/application.conf")

        return builder.build().loadConfigOrThrow<TulparConfig>()
    }
}
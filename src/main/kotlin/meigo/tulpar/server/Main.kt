package meigo.tulpar.server

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextStyles
import com.github.ajalt.mordant.terminal.Terminal
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.slf4j.LoggerFactory
import java.io.File

object TulparContext {
    val terminal = Terminal()
    val logger = LoggerFactory.getLogger("meigo.tulpar.server")
    const val VERSION = "2.0.0"
}

class TulparCommand : CliktCommand(name = "tulpar-server") {
    override fun run() = Unit
}

class StartCommand : CliktCommand(name = "start") {
    override fun help(context: Context) = "Start the Tulpar Server"

    private val port by option("-p", "--port", help = "Port to bind to").int()

    private val configPath by option("-c", "--config", help = "Path to application.conf")
        .file(mustExist = false, canBeDir = false)
        .default(File("application.conf"))

    private val daemon by option("-d", "--daemon", help = "Run in detached mode").flag()

    override fun run() {
        val t = TulparContext.terminal

        try {
            val config = ConfigFactory.load(configPath)

            val finalPort = port ?: config.server.port
            val finalHost = config.server.address

            printBanner(config, t)

            TulparContext.logger.info("Initializing server on $finalHost:$finalPort")

            val server = embeddedServer(Netty, port = finalPort, host = finalHost) {
                tulparModule(config)
            }

            server.start(wait = !daemon)

            if (daemon) {
                TulparContext.logger.info("Server started in background (non-blocking mode).")
                // в разработке
                // перенос с java на kotlin
            }

        } catch (e: Exception) {
            t.println(TextColors.red("Fatal Error: ${e.message}"))
            TulparContext.logger.error("Failed to start server", e)
        }
    }

    private fun printBanner(config: TulparConfig, t: Terminal) {
        config.cli.hello.forEach { line ->
            t.println(TextColors.rgb(config.cli.color)(line))
        }
        t.println(TextStyles.bold("Tulpar Server ${TulparContext.VERSION}"))
        t.println(TextColors.gray("OS: ${System.getProperty("os.name")} | Arch: ${System.getProperty("os.arch")}"))
        t.println(TextColors.gray("=".repeat(t.size.width.coerceAtMost(60))))
    }
}

class VersionCommand : CliktCommand(name = "version") {
    override fun help(context: Context) = "Show version info"

    override fun run() {
        TulparContext.terminal.println("Tulpar Server version: ${TulparContext.VERSION}")
    }
}

fun main(args: Array<String>) {
    TulparCommand()
        .subcommands(StartCommand(), VersionCommand())
        .main(args)
}
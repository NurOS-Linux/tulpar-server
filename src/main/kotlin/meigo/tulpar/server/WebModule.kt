package meigo.tulpar.server

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File

fun Application.tulparModule(config: TulparConfig) {

    install(StatusPages) {
        status(HttpStatusCode.NotFound) { call, status ->
            val errorPage = File("errors/404.html")
            if (errorPage.exists()) {
                call.respondFile(errorPage)
            } else {
                call.respondText("404: Page Not Found", status = status)
            }
        }
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: ${cause.message}", status = HttpStatusCode.InternalServerError)
        }
    }

    routing {
        staticResources("/static", "static")
        staticResources("/icons", "icons")

        get("/") {
            val indexFile = File("static/index.html")
            if (indexFile.exists()) {
                call.respondFile(indexFile)
            } else {
                call.respondText("Welcome to Tulpar Server", ContentType.Text.Html)
            }
        }

        get("/favicon.ico") {
            val favicon = File("favicon.ico")
            if (favicon.exists()) {
                call.respondFile(favicon)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        route("/packages") {
            get("/{name}/{command}") {
                val name = call.parameters["name"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                val command = call.parameters["command"] ?: return@get call.respond(HttpStatusCode.BadRequest)

                when (command.lowercase()) {
                    "info" -> {
                        val metaFile = File("packages/$name/metadata.json")
                        if (metaFile.exists()) {
                            call.respondFile(metaFile)
                        } else {
                            call.respond(HttpStatusCode.NotFound, "Package metadata not found")
                        }
                    }
                    "download" -> {
                        val arch = call.request.queryParameters["arch"]
                        val version = call.request.queryParameters["version"]

                        if (arch == null || version == null) {
                            call.respond(HttpStatusCode.BadRequest, "Missing arch or version parameters")
                            return@get
                        }

                        val file = File("packages/$name/$arch/$version.apg")
                        if (file.exists()) {
                            call.response.header(
                                HttpHeaders.ContentDisposition,
                                ContentDisposition.Attachment.withParameter(ContentDisposition.Parameters.FileName, "$name-$version.apg").toString()
                            )
                            call.respondFile(file)
                        } else {
                            call.respond(HttpStatusCode.NotFound, "Package file not found")
                        }
                    }
                    else -> call.respond(HttpStatusCode.BadRequest, "Unknown command")
                }
            }
        }
    }
}
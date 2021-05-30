import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Path


fun main() {
    val home = System.getProperty("user.home")
    val rootPath = "$home/AudiobookCaster"
    val rootPathFile = File(rootPath)
    if(!rootPathFile.exists())
        rootPathFile.mkdirs()
    embeddedServer(Netty, port = 8000) {
        routing {
            get("/") {
                call.respondText("exists?")
            }
        }
    }.start(wait = true)
}

fun generateHomePage() : String{
    return """
        Hello World
    """.trimIndent()
}
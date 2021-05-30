import io.ktor.application.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.html.*
import java.io.File
import java.net.URI
import java.nio.file.FileSystems
import java.nio.file.Path


fun main(args: Array<String>) {
    val hostname = args.firstOrNull() ?: "franks-MacBook-Air.local"
    val home = System.getProperty("user.home")
    val rootPath = "$home/AudiobookCaster"
    val rootPathFile = File(rootPath)
    if (!rootPathFile.exists())
        rootPathFile.mkdirs()
    embeddedServer(Netty, port = 8000) {
        routing {
            get("/") {
                call.respondHtml {
                    head {
                        title("AudiobookCaster")
                    }
                    body {
                        h1 {
                            +hostname
                        }
                        pre {
                            +generateHomePage(rootPathFile)
                        }
                    }
                }

//                call.respondText(generateHomePage(hostname, rootPathFile))
            }
            getAllAudiobookDirectories(rootPathFile).forEach {
                val book = it.name
//                val letter = it
//                get("/$letter") {
//                    call.respondText("this is page $letter")
//                }
                val path = it.name.encodeURLPath()
                get("/$path"){
                    call.respondText("This would be the rss feed for $book")
                }

            }


            static("audiobooks") {
                staticRootFolder = File(rootPath)
                files("./")
            }
        }

    }.start(wait = true)
}

fun getAllAudiobookDirectories(root: File): List<File> {
    return (root.listFiles() ?: emptyArray())
        .filter { !it.startsWith(".") }
        .filter { it.isDirectory }
        .toList()
}

fun getAllAudiobookFiles(bookDirectory: File): List<File> {
    return (bookDirectory.listFiles() ?: emptyArray())
        .filter { !it.startsWith(".") }
        .filter { it.name.endsWith(".mp3") }
}

fun generateHomePage(root: File): String {
    val s = StringBuilder()
//    call.respondText("Request uri: $uri")
    listRootContents(s, root)
    return s.toString()
}

fun listRootContents(s: StringBuilder, root: File) {
    getAllAudiobookDirectories(root)
        .forEach {
            s.appendLine("- ${it.name} (${it.name.encodeURLPath()})")
            listFolderContents(s, it)
        }

}

fun listFolderContents(s: StringBuilder, dir: File) {
    getAllAudiobookFiles(dir)
        .forEach {
            s.appendLine("|- ${it.name}")
        }
}
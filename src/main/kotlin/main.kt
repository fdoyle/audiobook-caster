import io.ktor.application.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.html.*
import java.io.File


fun main(args: Array<String>) {
    val hostname = args.firstOrNull() ?: "franks-MacBook-Air.local"
    val home = System.getProperty("user.home")
    val rootPath = "$home/AudiobookCaster"
    val rootPathFile = File(rootPath)
    if (!rootPathFile.exists())
        rootPathFile.mkdirs()
    embeddedServer(Netty, port = 80) {
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
                        ul {
                            getAllAudiobookDirectories(rootPathFile).forEach {
                                val encodedFileName = it.name.encodeURLPath()
                                val href = "/$encodedFileName"

                                li {
                                    a(href = href) {
                                        +it.name
                                    }
                                }
                            }
                        }

                        pre {
                            +generateHomePage(rootPathFile)
                        }
                    }
                }
            }
            getAllAudiobookDirectories(rootPathFile).forEach {
                val book = it.name
                val path = it.name.encodeURLPath()
                val directory = it
                get("/$path") {
//                    call.respondText("This would be the rss feed for $book")
//                    call.respond("This would be the rss feed for $book")
                    call.respond(buildRssForDirectory(directory))
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

fun buildRssForDirectory(directory: File): String {
    val title = directory.name

    val episodes = getAllAudiobookFiles(directory)
    return buildXmlString {
        rootElement("rss", mapOf(
            "xmlns:itunes" to "http://www.itunes.com/dtds/podcast-1.0.dtd"
        )) {
            element("channel") {
//                element("atom:link", mapOf("href" to "http://google.com"))
                element("title"){ text(title)}
                element("itunes:summary")
                episodes.forEach {
                    val episode = it
                    element("item") {
                        element("title", text = it.name.removeSuffix(".mp3"))
                        element("itunes:duration", text="fuck i donno")
                    }
                }
            }
        }
    }
}
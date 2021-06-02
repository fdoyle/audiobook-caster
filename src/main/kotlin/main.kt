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


fun main(args: Array<String>) {
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
                        h3 {
                            +call.request.host()
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
                    call.respondText(
                        buildRssForDirectory(call.request.host(), directory),
                        contentType = ContentType.Text.Xml
                    )
                }

            }


            static("files/") {
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
        .sortedBy { it.name }
}

fun getAllAudiobookFiles(bookDirectory: File): List<File> {
    return (bookDirectory.listFiles() ?: emptyArray())
        .filter { !it.startsWith(".") }
        .filter { it.name.endsWith(".mp3")
                ||it.name.endsWith(".opus")}
        .sortedBy { it.name }
}

fun getImageFile(bookDirectory: File): File? {
    return (bookDirectory.listFiles() ?: emptyArray())
        .filter {
            it.name.endsWith(".jpg")
                    || it.name.endsWith(".png")
        }
        .firstOrNull()
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

fun buildRssForDirectory(host: String, directory: File): String {
    val title = directory.name
    val backupImageUrl = "https://freevector-images.s3.amazonaws.com/uploads/vector/preview/40538/large_1x_vecteezy_background-colorful-abstract_fj0221_generated.jpg"
    val imageFile = getImageFile(directory)
    val imageUrl = if(imageFile != null) buildUrlForFile(host, imageFile) else backupImageUrl
    val episodes = getAllAudiobookFiles(directory)
    return buildXmlString {
        rootElement(
            "rss", mapOf(
                "xmlns:itunes" to "http://www.itunes.com/dtds/podcast-1.0.dtd",
                "xmlns:atom" to "http://www.w3.org/2005/Atom",
                "xmlns:content" to "http://purl.org/rss/1.0/modules/content/",
                "xmlns:googleplay" to "http://www.google.com/schemas/play-podcasts/1.0",
                "xmlns:media" to "http://search.yahoo.com/mrss/",
                "xmlns:cc" to "http://web.resource.org/cc/",
                "xmlns:podcast" to "https://podcastindex.org/namespace/1.0",
                "xmlns:rdf" to "http://www.w3.org/1999/02/22-rdf-syntax-ns#"
            )
        ) {
            element("channel") {
                element(
                    "atom:link",
                    mapOf(
                        "href" to "http://google.com",
                        "rel" to "self",
                        "type" to "application/rss+xml"
                    )
                )
                element("title") { text(title) }
                element("pubDate", "Wed, 26 May 2021 13:00:00 +0000")
                element("lastBuildDate", "Sat, 29 May 2021 22:42:58 +0000")
                element("generator", "libsyn")
                element("link", "https://syntax.fm")
                element("language", "en")
                element("copyright", "http://foo.com")
                element("docs", "http://foo.com")
                element("managingEditor", "fdoyl001@gmail.com")
                element("itunes:summary", directory.nameWithoutExtension)
                element("image") {
                    element("url", imageUrl)
                    element("title", "a title")
                    element("link", "http://www.gogole.com")
                }
                element("itunes:author", "Frankd")
                element("itunes:keywords", "keyword")
                element(
                    "itunes:category",
                    mapOf(
                        "text" to "News"
                    )
                )
                element(
                    "itunes:image",
                    mapOf(
                        "href" to imageUrl
                    )
                )
                element("itunes:subtitle", "A subtitle")
                element("itunes:type", "episodic")

//                element("tts", text="1")
                element("description", "This is a description")
                episodes.sortedBy { it.name }.forEach {
                    val episode = it
                    val title = it.name.removeSuffix(".mp3")
                    val url = buildUrlForFile(host, episode)
                    element("item") {
                        element("pubDate", "Mon, 24 May 2021 13:00:00 +0000")
                        element(
                            "guid",
                            "http://lacronicus.com/$url"
                        )
                        element("link", "http://www.google.com")
                        element("itunes:image", mapOf("href" to imageUrl))
                        element("category","none")
                        element("description", "foo")
                        element("content:encoded", "content")
                        element("itunes:duration", "01:00:00")
                        element("title", title)
                        element(
                            "enclosure",
                            mapOf(
                                "length" to "12345",
                                "type" to "audio/mpeg",
                                "url" to url
                            )
                        )
                    }
                }
            }
        }
    }
}

fun buildUrlForFile(host: String, file: File): String {
    val fileName = file.name.encodeURLPath()
    val parentDir = file.parentFile.name.encodeURLPath()
    return "http://$host/files/$parentDir/$fileName"
}
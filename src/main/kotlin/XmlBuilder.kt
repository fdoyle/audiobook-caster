import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.StringWriter
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

//XML DSL, works like it should work.
fun buildXmlString(init: Document.() -> Unit): String {
    val docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
    val doc: Document = docBuilder.newDocument()
    doc.init()

    val transformer = TransformerFactory.newInstance().newTransformer()
    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no")
    transformer.setOutputProperty(OutputKeys.METHOD, "xml")
    transformer.setOutputProperty(OutputKeys.INDENT, "yes")
    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8")


    val writer = StringWriter()
    transformer.transform(DOMSource(doc), StreamResult(writer))
    return writer.toString()
}

//this is special because there can only be one top-level element
fun Document.rootElement(
    tag: String,
    attributes: Map<String, String> = mapOf(),
    init: Element.() -> Unit = {}
) {
    val element = this.createElement(tag)
    attributes.forEach { key, value ->
        element.setAttribute(key, value)
    }
    element.init()

    this.appendChild(element)
}

fun Document.rootElement(
    tag: String,
    attributes: Map<String, String> = mapOf(),
    text: String
) = rootElement(tag, attributes) {
    text(text)
}

fun Element.element(
    tag: String,
    attributes: Map<String, String> = mapOf(),
    init: Element.() -> Unit = {}
) {
    val element = this.ownerDocument.createElement(tag)
    attributes.forEach { key, value ->
        element.setAttribute(key, value)
    }
    element.init()
    this.appendChild(element)
}

fun Element.element(
    tag: String,
    attributes: Map<String, String> = mapOf(),
    text: String
) = element(tag, attributes) {
    text(text)
}

fun Element.text(text: String) {
    this.appendChild(this.ownerDocument.createTextNode(text))
}
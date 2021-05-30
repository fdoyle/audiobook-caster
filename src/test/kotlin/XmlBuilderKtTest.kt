import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class XmlBuilderKtTest {

    @Test
    fun sanity() {
        assertEquals(1, 1)
    }

    @Test
    fun empty() {
        val s = buildXmlString {

        }
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>", s.trim())
    }

    @Test
    fun `single element`() {
        assertEquals(
            """
                <?xml version="1.0" encoding="UTF-8" standalone="no"?>
                <Foo/>
            """.trimIndent(),
            buildXmlString {
                rootElement("Foo")
            }.trim()
        )
    }

    @Test
    fun `sibling element`() {
        assertEquals(
            """
                <?xml version="1.0" encoding="UTF-8" standalone="no"?>
                <Root>
                    <Foo/>
                    <Bar/>
                </Root>
            """.trimIndent(),
            buildXmlString {
                rootElement("Root") {
                    element("Foo")
                    element("Bar")
                }
            }.trim()
        )
    }

    @Test
    fun `nested elements`() {
        assertEquals(
            """
                <?xml version="1.0" encoding="UTF-8" standalone="no"?>
                <Foo>
                    <Bar/>
                </Foo>
            """.trimIndent(),
            buildXmlString {
                rootElement("Foo") {
                    element("Bar")
                }
            }.trim()
        )
    }

    @Test
    fun `text content`() {
        assertEquals(
            """
                <?xml version="1.0" encoding="UTF-8" standalone="no"?>
                <Foo>Bar</Foo>
            """.trimIndent(),
            buildXmlString {
                rootElement("Foo") {
                    text("Bar")
                }
            }.trim()
        )
    }

    @Test
    fun `text content alt`() {
        assertEquals(
            """
                <?xml version="1.0" encoding="UTF-8" standalone="no"?>
                <Foo>
                    <Bar>baz</Bar>
                </Foo>
            """.trimIndent(),
            buildXmlString {
                rootElement("Foo") {
                    element("Bar", text = "Baz")
                }
            }.trim()
        )
    }

    @Test
    fun `attributes`() {
        assertEquals(
            """
                <?xml version="1.0" encoding="UTF-8" standalone="no"?>
                <Foo bar="baz"/>
            """.trimIndent(),
            buildXmlString {
                rootElement("Foo", attributes = mapOf("bar" to "baz"))
            }.trim()
        )
    }
}
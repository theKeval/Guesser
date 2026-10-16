package io.keval.apps.numbcrunch.gameplay.privacy

import androidx.compose.ui.text.font.FontWeight
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PrivacyPolicyDocumentTest {

    private val sample = """
        # Policy Title

        **Effective date:** today

        Intro line one
        continues here.

        ## First Section

        - **Settings:** music and
          sound effects.
        - Second item with `code`.

        Closing paragraph.

        ## Contact

        Email [hello@keval.io](mailto:hello@keval.io) or read
        [Google's Privacy Policy](https://policies.google.com/privacy).
    """.trimIndent()

    @Test
    fun `splits the title, lead text and sections`() {
        val document = PrivacyPolicyDocument.parse(sample)

        assertEquals("Policy Title", document.title)
        assertEquals(listOf("First Section", "Contact"), document.sections.map { it.title })
        assertEquals(
            listOf("Effective date: today", "Intro line one continues here."),
            document.lead.map { (it as PolicyBlock.Paragraph).text.text },
        )
    }

    @Test
    fun `joins wrapped bullet lines and keeps paragraphs after a list separate`() {
        val blocks = PrivacyPolicyDocument.parse(sample).sections.first().blocks

        val bullets = blocks[0] as PolicyBlock.Bullets
        assertEquals(listOf("Settings: music and sound effects.", "Second item with code."), bullets.items.map { it.text })
        assertEquals("Closing paragraph.", (blocks[1] as PolicyBlock.Paragraph).text.text)
    }

    @Test
    fun `bold text is styled and its markers removed`() {
        val bullet = (PrivacyPolicyDocument.parse(sample).sections.first().blocks[0] as PolicyBlock.Bullets).items[0]
        val bold = bullet.spanStyles.single()

        assertEquals("Settings:", bullet.text.substring(bold.start, bold.end))
        assertEquals(FontWeight.Bold, bold.item.fontWeight)
    }

    @Test
    fun `links become plain text that names the address`() {
        val contact = PrivacyPolicyDocument.parse(sample).sections.last().blocks.single() as PolicyBlock.Paragraph

        assertEquals(
            "Email hello@keval.io or read Google's Privacy Policy (policies.google.com/privacy).",
            contact.text.text,
        )
    }

    @Test
    fun `the repository privacy policy parses without leftover markdown`() {
        val document = PrivacyPolicyDocument.parse(repositoryPolicy().readText())
        val texts = document.lead.flatMap(::texts) + document.sections.flatMap { it.blocks.flatMap(::texts) }
        val allText = (listOf(document.title) + document.sections.map { it.title } + texts).joinToString("\n")

        assertEquals("NumbCrunch Privacy Policy", document.title)
        assertTrue(document.sections.any { it.title == "Children's Privacy" })
        assertTrue(allText.contains("hello@keval.io"))
        listOf("**", "](", "`", "# ").forEach { marker ->
            assertFalse("Unrendered markdown \"$marker\" in the privacy policy", allText.contains(marker))
        }
    }

    private fun texts(block: PolicyBlock): List<String> = when (block) {
        is PolicyBlock.Paragraph -> listOf(block.text.text)
        is PolicyBlock.Bullets -> block.items.map { it.text }
    }

    /** PRIVACY.md lives at the repository root; unit tests run from the module directory. */
    private fun repositoryPolicy(): File = generateSequence(File("").absoluteFile) { it.parentFile }
        .map { File(it, "PRIVACY.md") }
        .first { it.isFile }
}

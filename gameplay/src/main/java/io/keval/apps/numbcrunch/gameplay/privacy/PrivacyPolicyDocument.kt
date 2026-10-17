package io.keval.apps.numbcrunch.gameplay.privacy

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

/** Asset name of the bundled copy of the repository's PRIVACY.md (see gameplay/build.gradle). */
internal const val PRIVACY_POLICY_ASSET = "privacy_policy.md"

internal sealed interface PolicyBlock {
    data class Paragraph(val text: AnnotatedString) : PolicyBlock
    data class Bullets(val items: List<AnnotatedString>) : PolicyBlock
}

internal data class PolicySection(val title: String, val blocks: List<PolicyBlock>)

/**
 * The privacy policy, split into the text before the first section heading ([lead]) and its
 * `##` [sections]. Links are shown as plain text so nothing in the game opens a website.
 */
internal data class PrivacyPolicyDocument(
    val title: String,
    val lead: List<PolicyBlock>,
    val sections: List<PolicySection>,
) {
    companion object {
        /**
         * Parses the small Markdown subset PRIVACY.md uses: a `#` title, `##` section headings,
         * paragraphs, `- ` bullets (continued by indented lines), `**bold**`, `` `code` `` and
         * `[text](url)` links.
         */
        fun parse(markdown: String): PrivacyPolicyDocument {
            var title = ""
            val lead = mutableListOf<PolicyBlock>()
            val sections = mutableListOf<PolicySection>()
            var sectionTitle: String? = null
            var blocks = lead
            val paragraph = mutableListOf<String>()
            val bullets = mutableListOf<MutableList<String>>()

            fun flush() {
                if (paragraph.isNotEmpty()) {
                    blocks += PolicyBlock.Paragraph(inline(paragraph.joinToString(" ")))
                    paragraph.clear()
                }
                if (bullets.isNotEmpty()) {
                    blocks += PolicyBlock.Bullets(bullets.map { inline(it.joinToString(" ")) })
                    bullets.clear()
                }
            }

            fun closeSection() {
                flush()
                sectionTitle?.let { sections += PolicySection(it, blocks.toList()) }
            }

            markdown.lineSequence().forEach { raw ->
                val line = raw.trimEnd()
                when {
                    line.startsWith("## ") -> {
                        closeSection()
                        sectionTitle = line.removePrefix("## ").trim()
                        blocks = mutableListOf()
                    }
                    line.startsWith("# ") -> {
                        flush()
                        title = line.removePrefix("# ").trim()
                    }
                    line.isBlank() -> flush()
                    line.startsWith("- ") -> {
                        if (paragraph.isNotEmpty()) flush()
                        bullets += mutableListOf(line.removePrefix("- ").trim())
                    }
                    bullets.isNotEmpty() && raw.startsWith("  ") -> bullets.last() += line.trim()
                    else -> {
                        if (bullets.isNotEmpty()) flush()
                        paragraph += line.trim()
                    }
                }
            }
            closeSection()
            return PrivacyPolicyDocument(title, lead.toList(), sections.toList())
        }

        private val InlinePattern = Regex("""\*\*(.+?)\*\*|`([^`]+)`|\[([^\]]+)]\(([^)]+)\)""")
        private val Bold = SpanStyle(fontWeight = FontWeight.Bold)

        private fun inline(text: String): AnnotatedString = buildAnnotatedString {
            var index = 0
            InlinePattern.findAll(text).forEach { match ->
                append(text.substring(index, match.range.first))
                val (bold, code, linkText, linkTarget) = match.destructured
                when {
                    bold.isNotEmpty() -> withStyle(Bold) { append(bold) }
                    code.isNotEmpty() -> append(code)
                    else -> append(plainLink(linkText, linkTarget))
                }
                index = match.range.last + 1
            }
            append(text.substring(index))
        }

        /** "hello@keval.io" stays as is; "Google's Privacy Policy" gains "(policies.google.com/privacy)". */
        private fun plainLink(text: String, target: String): String {
            val address = target.removePrefix("mailto:").removePrefix("https://").removePrefix("http://")
                .removePrefix("www.").trimEnd('/')
            return if (text.trimEnd('/') == address) text else "$text ($address)"
        }
    }
}

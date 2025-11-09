package com.example.blogapp.util
import android.content.Context
import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import io.noties.markwon.Markwon
import io.noties.markwon.html.HtmlPlugin

object MarkdownRenderer {

    fun create(context: Context): Markwon {
        return Markwon.builder(context)
            .usePlugin(HtmlPlugin.create())
            .build()
    }

    fun toPlainText(markdown: String): String {
        return markdown
            .replace(Regex("#+ "), "")
            .replace(Regex("\\*\\*([^*]+)\\*\\*"), "$1")
            .replace(Regex("\\*([^*]+)\\*"), "$1")
            .replace(Regex("__([^_]+)__"), "$1")
            .replace(Regex("_([^_]+)_"), "$1")
            .replace(Regex("~~([^~]+)~~"), "$1")
            .replace(Regex("`([^`]+)`"), "$1")
            .replace(Regex("\\[([^]]+)\\]\\([^)]+\\)"), "$1")
            .replace(Regex("!\\[([^]]*)]\\([^)]+\\)"), "")
            .replace(Regex("^>+ "), "")
            .replace(Regex("^[-*+] "), "")
            .replace(Regex("^\\d+\\. "), "")
            .trim()
    }

    fun extractExcerpt(markdown: String, maxLength: Int = 150): String {
        val plainText = toPlainText(markdown)
        return if (plainText.length > maxLength) {
            plainText.take(maxLength).trim() + "..."
        } else {
            plainText
        }
    }

    fun countWords(markdown: String): Int {
        val plainText = toPlainText(markdown)
        return plainText.split("\\s+".toRegex())
            .filter { it.isNotEmpty() }
            .size
    }

    fun calculateReadingTime(markdown: String, wordsPerMinute: Int = Constants.WORDS_PER_MINUTE): Int {
        val wordCount = countWords(markdown)
        return (wordCount / wordsPerMinute).coerceAtLeast(1)
    }
}

@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier,
    context: Context
) {
    val markwon = remember(context) {
        MarkdownRenderer.create(context)
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            TextView(ctx).apply {
                textSize = 16f
                setTextColor(android.graphics.Color.BLACK)
            }
        },
        update = { textView ->
            markwon.setMarkdown(textView, markdown)
        }
    )
}

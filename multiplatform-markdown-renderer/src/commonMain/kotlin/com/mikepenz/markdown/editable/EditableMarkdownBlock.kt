package com.mikepenz.markdown.editable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import org.intellij.markdown.ast.ASTNode

/**
 * A composable wrapper around a markdown block that can switch between rendered and editable modes.
 *
 * When not focused, it renders [renderedContent] and requests focus on click.
 * When focused, it shows a [BasicTextField] initialized with the raw markdown source for [node].
 */
@Composable
fun EditableMarkdownBlock(
    node: ASTNode,
    fullContent: String,
    isFocused: Boolean,
    onFocus: () -> Unit,
    onEdit: (String) -> Unit,
    onUnfocus: () -> Unit,
    renderedContent: @Composable () -> Unit,
) {
    if (!isFocused) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onFocus)
        ) {
            renderedContent()
        }
        return
    }

    var text by remember(node.startOffset, node.endOffset, fullContent) {
        mutableStateOf(extractNodeContent(node, fullContent))
    }

    LaunchedEffect(node.startOffset, node.endOffset, fullContent) {
        val source = extractNodeContent(node, fullContent)
        if (source != text) text = source
    }

    BasicTextField(
        value = text,
        onValueChange = { newText ->
            text = newText
            onEdit(newText)
        },
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF2F4F8))
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .onFocusChanged { focusState ->
                if (!focusState.isFocused) onUnfocus()
            },
        textStyle = TextStyle(
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF1F2937),
        ),
    )
}

private fun extractNodeContent(node: ASTNode, fullContent: String): String {
    val contentLength = fullContent.length
    val start = node.startOffset.coerceIn(0, contentLength)
    val end = node.endOffset.coerceIn(0, contentLength)
    if (start >= end) return ""
    return fullContent.substring(start, end)
}

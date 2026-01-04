package com.github.dprai.jumpyplugin

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.VisualPosition
import java.awt.Point

/**
 * Data class representing a jump position in the editor.
 *
 * @property offset The character offset in the document
 * @property visualPosition The visual position (line, column) in the editor
 * @property point The pixel coordinates for rendering the label
 */
data class JumpPosition(
    val offset: Int,
    val visualPosition: VisualPosition,
    val point: Point
)

/**
 * Finds word start positions in the visible portion of the editor.
 */
object WordPositionFinder {
    private val WORD_START_PATTERN = Regex("""(?<!\w)\w""")

    /**
     * Finds all word start positions in the visible viewport.
     *
     * @param editor The editor instance
     * @return List of jump positions at word starts
     */
    fun findWordPositions(editor: Editor): List<JumpPosition> {
        val positions = mutableListOf<JumpPosition>()
        val visibleArea = editor.scrollingModel.visibleArea
        val document = editor.document

        // Get the visible line range
        val startPosition = editor.xyToVisualPosition(Point(visibleArea.x, visibleArea.y))
        val endPosition = editor.xyToVisualPosition(
            Point(visibleArea.x + visibleArea.width, visibleArea.y + visibleArea.height)
        )

        val startLine = startPosition.line
        val endLine = minOf(endPosition.line, document.lineCount - 1)

        // Process each visible line
        for (line in startLine..endLine) {
            if (line >= document.lineCount) break

            val lineStartOffset = document.getLineStartOffset(line)
            val lineEndOffset = document.getLineEndOffset(line)
            val lineText = document.getText(com.intellij.openapi.util.TextRange(lineStartOffset, lineEndOffset))

            // Find all word starts in this line
            WORD_START_PATTERN.findAll(lineText).forEach { matchResult ->
                val columnInLine = matchResult.range.first
                val offset = lineStartOffset + columnInLine
                val visualPosition = editor.offsetToVisualPosition(offset)
                val point = editor.visualPositionToXY(visualPosition)

                // Only include positions that are actually visible
                if (visibleArea.contains(point)) {
                    positions.add(JumpPosition(offset, visualPosition, point))
                }
            }
        }

        return positions
    }

    /**
     * Finds all word start positions including CamelCase boundaries.
     * This is an enhanced version that splits on CamelCase transitions.
     *
     * @param editor The editor instance
     * @return List of jump positions at word starts and CamelCase boundaries
     */
    fun findWordPositionsWithCamelCase(editor: Editor): List<JumpPosition> {
        val positions = mutableListOf<JumpPosition>()
        val visibleArea = editor.scrollingModel.visibleArea
        val document = editor.document

        val startPosition = editor.xyToVisualPosition(Point(visibleArea.x, visibleArea.y))
        val endPosition = editor.xyToVisualPosition(
            Point(visibleArea.x + visibleArea.width, visibleArea.y + visibleArea.height)
        )

        val startLine = startPosition.line
        val endLine = minOf(endPosition.line, document.lineCount - 1)

        for (line in startLine..endLine) {
            if (line >= document.lineCount) break

            val lineStartOffset = document.getLineStartOffset(line)
            val lineEndOffset = document.getLineEndOffset(line)
            val lineText = document.getText(com.intellij.openapi.util.TextRange(lineStartOffset, lineEndOffset))

            var i = 0
            while (i < lineText.length) {
                val char = lineText[i]
                val prevChar = if (i > 0) lineText[i - 1] else null

                // Word start conditions:
                // 1. Start of a word (non-word char followed by word char)
                // 2. Lowercase followed by uppercase (CamelCase)
                // 3. Multiple uppercase followed by uppercase then lowercase (e.g., "XMLParser" -> "XML", "Parser")
                val isWordStart = when {
                    prevChar == null && char.isLetterOrDigit() -> true
                    prevChar?.isLetterOrDigit() == false && char.isLetterOrDigit() -> true
                    prevChar?.isLowerCase() == true && char.isUpperCase() -> true
                    i > 1 && lineText[i - 2].isUpperCase() && prevChar?.isUpperCase() == true &&
                        char.isLowerCase() -> true
                    else -> false
                }

                if (isWordStart) {
                    val offset = lineStartOffset + i
                    val visualPosition = editor.offsetToVisualPosition(offset)
                    val point = editor.visualPositionToXY(visualPosition)

                    if (visibleArea.contains(point)) {
                        positions.add(JumpPosition(offset, visualPosition, point))
                    }
                }

                i++
            }
        }

        return positions
    }
}

package com.github.dprai.jumpyplugin.ui

import com.github.dprai.jumpyplugin.JumpPosition
import java.awt.*
import javax.swing.JPanel

/**
 * Overlay panel that renders jump labels over the editor.
 * This panel is transparent and displays two-letter labels at word positions.
 */
class JumpyOverlayPanel : JPanel() {
    private var labelPositions: Map<String, JumpPosition> = emptyMap()
    private var highlightedPrefix: String = ""

    init {
        isOpaque = false
        background = Color(0, 0, 0, 0)
        layout = null
    }

    /**
     * Updates the labels to display.
     *
     * @param positions Map of label strings to their positions
     */
    fun setLabels(positions: Map<String, JumpPosition>) {
        this.labelPositions = positions
        this.highlightedPrefix = ""
        repaint()
    }

    /**
     * Highlights labels that start with the given prefix.
     *
     * @param prefix The prefix to highlight (e.g., "a" to highlight all labels starting with 'a')
     */
    fun setHighlightedPrefix(prefix: String) {
        this.highlightedPrefix = prefix
        repaint()
    }

    /**
     * Clears all labels from the overlay.
     */
    fun clear() {
        this.labelPositions = emptyMap()
        this.highlightedPrefix = ""
        repaint()
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)

        val g2d = g as Graphics2D
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)

        // Font settings
        val font = Font("Monospaced", Font.BOLD, 12)
        g2d.font = font
        val fontMetrics = g2d.fontMetrics

        labelPositions.forEach { (label, position) ->
            val shouldHighlight = highlightedPrefix.isNotEmpty() && label.startsWith(highlightedPrefix)

            // Calculate label dimensions
            val labelWidth = fontMetrics.stringWidth(label)
            val labelHeight = fontMetrics.height
            val padding = 2

            val x = position.point.x
            val y = position.point.y

            // Background rectangle
            val bgColor = if (shouldHighlight) {
                Color(255, 165, 0, 220) // Orange with transparency
            } else {
                Color(255, 255, 100, 200) // Yellow with transparency
            }

            g2d.color = bgColor
            g2d.fillRoundRect(
                x - padding,
                y - labelHeight + fontMetrics.descent,
                labelWidth + padding * 2,
                labelHeight,
                4,
                4
            )

            // Border
            g2d.color = Color.DARK_GRAY
            g2d.drawRoundRect(
                x - padding,
                y - labelHeight + fontMetrics.descent,
                labelWidth + padding * 2,
                labelHeight,
                4,
                4
            )

            // Text
            g2d.color = Color.BLACK
            g2d.drawString(label, x, y)

            // If we have a prefix, highlight the matching part differently
            if (highlightedPrefix.isNotEmpty() && shouldHighlight) {
                // Draw the prefix in a different color
                g2d.color = Color.RED
                val prefixWidth = fontMetrics.stringWidth(highlightedPrefix)
                g2d.fillRect(
                    x - 1,
                    y - labelHeight + fontMetrics.descent + 1,
                    prefixWidth + 2,
                    labelHeight - 2
                )

                // Redraw the prefix text in white
                g2d.color = Color.WHITE
                g2d.drawString(highlightedPrefix, x, y)

                // Draw the rest of the label in black
                val remainingLabel = label.substring(highlightedPrefix.length)
                g2d.color = Color.BLACK
                g2d.drawString(remainingLabel, x + prefixWidth, y)
            }
        }
    }

    override fun getPreferredSize(): Dimension {
        return parent?.size ?: Dimension(0, 0)
    }
}

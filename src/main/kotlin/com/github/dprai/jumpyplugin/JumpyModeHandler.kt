package com.github.dprai.jumpyplugin

import com.github.dprai.jumpyplugin.ui.JumpyOverlayPanel
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.TypedAction
import com.intellij.openapi.editor.actionSystem.TypedActionHandler
import com.intellij.openapi.editor.actionSystem.TypedActionHandlerEx
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.util.Disposer
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.JLayeredPane
import javax.swing.SwingUtilities

/**
 * Manages the jumpy mode state and handles user input during jumpy mode.
 * This is a singleton service that coordinates label display, keyboard input,
 * and cursor movement.
 */
class JumpyModeHandler : Disposable {
    companion object {
        @Volatile
        private var instance: JumpyModeHandler? = null

        fun getInstance(): JumpyModeHandler {
            return instance ?: synchronized(this) {
                instance ?: JumpyModeHandler().also { instance = it }
            }
        }
    }

    private var isActive = false
    private var currentEditor: Editor? = null
    private var overlayPanel: JumpyOverlayPanel? = null
    private var labelMap: Map<String, JumpPosition> = emptyMap()
    private var currentInput = StringBuilder()
    private var keyListener: KeyAdapter? = null
    private var originalTypedHandler: TypedActionHandler? = null
    private var jumpyTypedHandler: JumpyTypedActionHandler? = null

    /**
     * Activates jumpy mode for the given editor.
     */
    fun activate(editor: Editor) {
        if (isActive) {
            deactivate()
        }

        isActive = true
        currentEditor = editor
        currentInput.clear()

        // Find word positions
        val positions = WordPositionFinder.findWordPositions(editor)

        // Generate labels
        val labels = LabelGenerator.generateLabels(positions.size)

        // Create label to position mapping
        labelMap = labels.zip(positions).toMap()

        // Create and show overlay panel
        showOverlay(editor, labelMap)

        // Install keyboard handler
        installKeyboardHandler(editor)
    }

    /**
     * Deactivates jumpy mode.
     */
    fun deactivate() {
        if (!isActive) return

        isActive = false
        currentInput.clear()

        // Remove overlay
        removeOverlay()

        // Remove keyboard handler
        removeKeyboardHandler()

        labelMap = emptyMap()
        currentEditor = null
    }

    /**
     * Handles a typed character during jumpy mode.
     */
    private fun handleCharTyped(char: Char) {
        if (!isActive) return

        // Only accept lowercase letters
        if (char !in 'a'..'z') {
            deactivate()
            return
        }

        currentInput.append(char)

        // Update overlay to highlight matching labels
        overlayPanel?.setHighlightedPrefix(currentInput.toString())

        // If we have two characters, try to jump
        if (currentInput.length == 2) {
            val label = currentInput.toString()
            val position = labelMap[label]

            if (position != null) {
                jumpToPosition(position)
            } else {
                // Invalid label - just deactivate
                java.awt.Toolkit.getDefaultToolkit().beep()
            }

            deactivate()
        }
    }

    /**
     * Handles escape key to cancel jumpy mode.
     */
    private fun handleEscape() {
        if (isActive) {
            deactivate()
        }
    }

    /**
     * Moves the cursor to the specified position.
     */
    private fun jumpToPosition(position: JumpPosition) {
        val editor = currentEditor ?: return

        // Move caret to the position
        editor.caretModel.moveToOffset(position.offset)

        // Scroll to ensure the position is visible
        editor.scrollingModel.scrollToCaret(com.intellij.openapi.editor.ScrollType.CENTER)
    }

    /**
     * Shows the overlay panel with labels.
     */
    private fun showOverlay(editor: Editor, labels: Map<String, JumpPosition>) {
        val editorComponent = editor.contentComponent

        // Add to layered pane
        SwingUtilities.invokeLater {
            val rootPane = SwingUtilities.getRootPane(editorComponent)
            if (rootPane != null) {
                // Convert editor component bounds to layered pane coordinates
                val editorLocationInLayeredPane = SwingUtilities.convertPoint(
                    editorComponent.parent,
                    editorComponent.location,
                    rootPane.layeredPane
                )

                overlayPanel = JumpyOverlayPanel().apply {
                    setLabels(labels)
                    // Set bounds in layered pane coordinates to cover the editor
                    setBounds(
                        editorLocationInLayeredPane.x,
                        editorLocationInLayeredPane.y,
                        editorComponent.width,
                        editorComponent.height
                    )
                }

                rootPane.layeredPane.add(overlayPanel, JLayeredPane.POPUP_LAYER)
                rootPane.layeredPane.revalidate()
                rootPane.layeredPane.repaint()
            }
        }
    }

    /**
     * Removes the overlay panel.
     */
    private fun removeOverlay() {
        SwingUtilities.invokeLater {
            overlayPanel?.let { panel ->
                panel.parent?.remove(panel)
                panel.parent?.revalidate()
                panel.parent?.repaint()
            }
            overlayPanel = null
        }
    }

    /**
     * Installs a typed action handler to intercept input during jumpy mode.
     */
    private fun installKeyboardHandler(editor: Editor) {
        val editorComponent = editor.contentComponent

        // Install escape key handler using KeyListener
        keyListener = object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {
                if (isActive) {
                    when (e.keyCode) {
                        KeyEvent.VK_ESCAPE -> {
                            e.consume()
                            handleEscape()
                        }
                        KeyEvent.VK_ENTER, KeyEvent.VK_SPACE, KeyEvent.VK_BACK_SPACE -> {
                            e.consume()
                            deactivate()
                        }
                    }
                }
            }
        }.also {
            editorComponent.addKeyListener(it)
            editorComponent.requestFocusInWindow()
        }

        // Install typed action handler to intercept character typing
        val typedAction = TypedAction.getInstance()
        val original = typedAction.rawHandler
        originalTypedHandler = original
        jumpyTypedHandler = JumpyTypedActionHandler(original, this)
        typedAction.setupRawHandler(jumpyTypedHandler!!)
    }

    /**
     * Removes the keyboard handler and restores original typed action handler.
     */
    private fun removeKeyboardHandler() {
        // Restore original typed action handler
        originalTypedHandler?.let { handler ->
            TypedAction.getInstance().setupRawHandler(handler)
        }
        originalTypedHandler = null
        jumpyTypedHandler = null

        // Remove key listener
        keyListener?.let { listener ->
            currentEditor?.contentComponent?.removeKeyListener(listener)
        }
        keyListener = null
    }

    /**
     * Custom typed action handler that intercepts character input during jumpy mode.
     */
    private class JumpyTypedActionHandler(
        private val originalHandler: TypedActionHandler,
        private val modeHandler: JumpyModeHandler
    ) : TypedActionHandler {
        override fun execute(editor: Editor, charTyped: Char, dataContext: DataContext) {
            if (modeHandler.isActive && editor == modeHandler.currentEditor) {
                // Handle the character in jumpy mode - don't pass to original handler
                modeHandler.handleCharTyped(charTyped)
            } else {
                // Not in jumpy mode or different editor - pass through to original handler
                originalHandler.execute(editor, charTyped, dataContext)
            }
        }
    }

    override fun dispose() {
        deactivate()
        instance = null
    }

    fun isJumpyModeActive(): Boolean = isActive
}

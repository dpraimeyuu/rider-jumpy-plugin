package com.github.dprai.jumpyplugin

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.diagnostic.logger

/**
 * Action that activates jumpy mode when triggered.
 * This action is registered in plugin.xml and bound to a keyboard shortcut (default: Shift+Enter).
 */
class JumpyAction : AnAction() {
    private val log = logger<JumpyAction>()

    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR) ?: run {
            log.warn("No editor available")
            return
        }

        val project = e.project
        if (project == null) {
            log.warn("No project available")
            return
        }

        // Toggle jumpy mode
        val handler = JumpyModeHandler.getInstance()
        if (handler.isJumpyModeActive()) {
            // If already active, deactivate
            handler.deactivate()
        } else {
            // Activate jumpy mode
            try {
                handler.activate(editor)
            } catch (ex: Exception) {
                log.error("Failed to activate jumpy mode", ex)
                handler.deactivate()
            }
        }
    }

    override fun update(e: AnActionEvent) {
        // Enable the action only when an editor is available
        val editor = e.getData(CommonDataKeys.EDITOR)
        e.presentation.isEnabled = editor != null && e.project != null
    }
}

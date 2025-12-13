package com.github.mmileticc.pluginintellij1st.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.wm.ToolWindowId
import com.intellij.openapi.wm.ToolWindowManager

class FocusKeeper9000OpenAction : AnAction(), DumbAware {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val twManager = ToolWindowManager.getInstance(project)
        val toolWindow = twManager.getToolWindow("FocusKeeper9000")
        if (toolWindow != null) {
            toolWindow.show(null)
            toolWindow.activate(null, true)
        } else {
            // In case the tool window isn't created yet, try to register by id (IDE should create it lazily)
            twManager.invokeLater {
                twManager.getToolWindow("FocusKeeper9000")?.apply {
                    show(null)
                    activate(null, true)
                }
            }
        }
    }
}

package com.github.mmileticc.pluginintellij1st

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.WindowManager
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.ui.awt.RelativePoint
import com.intellij.util.ui.JBUI
import java.awt.Point
import javax.swing.JLabel

object CornerPopup {
    data class Handle(val popup: com.intellij.openapi.ui.popup.JBPopup)

    fun showBottomRight(project: Project, content: JLabel): Handle? {
        val frame = WindowManager.getInstance().getIdeFrame(project) ?: return null
        val root = frame.component

        // Build a lightweight popup
        val popup = JBPopupFactory.getInstance()
            .createComponentPopupBuilder(content, null)
            .setFocusable(false)
            .setRequestFocus(false)
            .setBelongsToGlobalPopupStack(false)
            .setCancelOnClickOutside(false)
            .setCancelOnWindowDeactivation(false)
            .setMovable(false)
            .setResizable(false)
            .createPopup()

        // Pack to get its preferred size before computing the position
        val size = content.preferredSize
        val insets = JBUI.insets(1)

        val rootSize = root.size
        val x = rootSize.width - size.width - insets.right
        val y = rootSize.height - size.height - insets.bottom

        popup.show(RelativePoint(root, Point(x.coerceAtLeast(insets.left), y.coerceAtLeast(insets.top))))
        return Handle(popup)
    }

    fun close(handle: Handle?) {
        handle?.popup?.cancel()
    }
}
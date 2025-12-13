package com.github.mmileticc.pluginintellij1st

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopup
import com.intellij.openapi.wm.WindowManager
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.ui.awt.RelativePoint
import com.intellij.util.ui.JBUI
import java.awt.Point
import javax.swing.JLabel

import javax.swing.JComponent

object CornerPopup {

    data class Handle(val popup: JBPopup)

    fun showBottomRight(project: Project, content: JComponent): Handle? {
        val frame = WindowManager.getInstance().getIdeFrame(project) ?: return null
        val root = frame.component

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

        val size = content.preferredSize
        val insets = JBUI.insets(1)

        val x = root.width - size.width - insets.right - 50
        val y = root.height - size.height - insets.bottom - 50

        popup.show(RelativePoint(root, Point(x, y)))
        return Handle(popup)
    }

    fun close(handle: Handle?) {
        handle?.popup?.cancel()
    }

}


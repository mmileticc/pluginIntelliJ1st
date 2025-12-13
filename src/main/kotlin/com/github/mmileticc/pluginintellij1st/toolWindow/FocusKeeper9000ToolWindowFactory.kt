package com.github.mmileticc.pluginintellij1st.toolWindow

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.intellij.util.ui.JBUI
import com.intellij.ui.jcef.JBCefApp
import com.intellij.ui.jcef.JBCefBrowser
import java.awt.BorderLayout
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JToolBar

class FocusKeeper9000ToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val contentFactory = ContentFactory.getInstance()
        val panel = createContentPanel()
        val content = contentFactory.createContent(panel, null, false)
        toolWindow.contentManager.addContent(content)
    }

    private fun createContentPanel(): JPanel {
        val root = JPanel(BorderLayout())

        if (!JBCefApp.isSupported()) {
            // Fallback simple panel with a message
            val fallback = JPanel(BorderLayout())
            fallback.add(javax.swing.JLabel("JCEF is not supported on this platform."), BorderLayout.CENTER)
            root.add(fallback, BorderLayout.CENTER)
            return root
        }

        val browser = JBCefBrowser("https://open.spotify.com/")

        // Simple toolbar with Back/Forward/Reload
        val toolbar = JToolBar().apply {
            isFloatable = false
            border = JBUI.Borders.empty(4)
        }

        val backBtn = JButton("◀")
        backBtn.toolTipText = "Back"
        backBtn.addActionListener {
            val cef = browser.cefBrowser
            if (cef != null && cef.canGoBack()) cef.goBack()
        }

        val forwardBtn = JButton("▶")
        forwardBtn.toolTipText = "Forward"
        forwardBtn.addActionListener {
            val cef = browser.cefBrowser
            if (cef != null && cef.canGoForward()) cef.goForward()
        }

        val reloadBtn = JButton("⟳")
        reloadBtn.toolTipText = "Reload"
        reloadBtn.addActionListener { browser.cefBrowser?.reload() }

        toolbar.add(backBtn)
        toolbar.add(forwardBtn)
        toolbar.add(reloadBtn)

        root.add(toolbar, BorderLayout.NORTH)
        root.add(browser.component, BorderLayout.CENTER)

        return root
    }

    companion object {
        private val LOG = Logger.getInstance(FocusKeeper9000ToolWindowFactory::class.java)
    }
}

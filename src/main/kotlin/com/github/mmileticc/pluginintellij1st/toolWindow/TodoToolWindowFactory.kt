package com.github.mmileticc.pluginintellij1st.toolWindow

import com.github.mmileticc.pluginintellij1st.services.TodoScannerService
import com.github.mmileticc.pluginintellij1st.services.TodoScannerService.Status
import com.intellij.icons.AllIcons
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.*
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.JBColor
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.event.ActionListener
import javax.swing.*

class TodoToolWindowFactory : ToolWindowFactory {

    override fun shouldBeAvailable(project: Project): Boolean = true

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val contentFactory = ContentFactory.getInstance()
        val panel = TodoPanel(project)
        val content = contentFactory.createContent(panel, null, false)
        toolWindow.contentManager.addContent(content)
    }

    private class TodoPanel(private val project: Project) : JBPanel<TodoPanel>(BorderLayout()) {
        private val service = TodoScannerService.getInstance(project)

        private val progressLabel = JBLabel("0% solved (0/0)")
        private val progressBar = JProgressBar(0, 100).apply {
            value = 0
            foreground = JBColor.CYAN
            isStringPainted = false
            border = JBUI.Borders.empty(0, 0, 0, 0)
        }
        private val inProgressToggle = JBRadioButton("In progress", true)
        private val completedToggle = JBRadioButton("Completed")
        private val refreshButton = JButton(AllIcons.Actions.Refresh)
        private val listModel = DefaultListModel<TodoScannerService.TaskItem>()
        private val list = JBList(listModel)

        private var allItems: List<TodoScannerService.TaskItem> = emptyList()

        init {
            border = JBUI.Borders.empty(8)

            // Header with 3 rows: percentage (with refresh at right), progress bar, then filter buttons
            val header = JBPanel<JBPanel<*>>().apply {
                layout = BoxLayout(this, BoxLayout.Y_AXIS)
                border = JBUI.Borders.emptyBottom(8)
            }

            // Row 1: percentage label (left) + Refresh (right)
            val row1 = JBPanel<JBPanel<*>>(BorderLayout()).apply {
                border = JBUI.Borders.emptyBottom(6)
            }
            row1.add(progressLabel, BorderLayout.WEST)
            refreshButton.toolTipText = "Refresh"
            row1.add(refreshButton, BorderLayout.EAST)

            // Row 2: percentage bar (cyan fill)
            val row2 = JBPanel<JBPanel<*>>(BorderLayout()).apply {
                border = JBUI.Borders.emptyBottom(6)
            }
            row2.add(progressBar, BorderLayout.CENTER)

            // Row 3: view toggles
            val row3 = JBPanel<JBPanel<*>>()
            row3.layout = BoxLayout(row3, BoxLayout.X_AXIS)
            row3.add(JBLabel("View:"))
            row3.add(Box.createHorizontalStrut(6))
            val group = ButtonGroup()
            group.add(inProgressToggle)
            group.add(completedToggle)
            row3.add(inProgressToggle)
            row3.add(Box.createHorizontalStrut(6))
            row3.add(completedToggle)

            header.add(row1)
            header.add(row2)
            header.add(row3)

            add(header, BorderLayout.NORTH)

            // Center list
            list.cellRenderer = object : ListCellRenderer<TodoScannerService.TaskItem> {
                private val panel = JBPanel<JBPanel<*>>(BorderLayout()).apply { border = JBUI.Borders.empty(4) }
                private val title = JBLabel()
                private val sub = JBLabel().apply { foreground = JBColor.GRAY }
                override fun getListCellRendererComponent(
                    list: JList<out TodoScannerService.TaskItem>, value: TodoScannerService.TaskItem, index: Int,
                    isSelected: Boolean, cellHasFocus: Boolean
                ): java.awt.Component {
                    panel.removeAll()
                    title.text = value.presentableText()
                    val fileLine = "${value.file.name}:${value.line}"
                    sub.text = fileLine
                    val inner = JBPanel<JBPanel<*>>()
                    inner.layout = BoxLayout(inner, BoxLayout.Y_AXIS)
                    inner.add(title)
                    inner.add(sub)
                    panel.add(inner, BorderLayout.CENTER)
                    if (isSelected) {
                        panel.background = list.selectionBackground
                        title.foreground = list.selectionForeground
                        sub.foreground = list.selectionForeground
                    } else {
                        panel.background = list.background
                        title.foreground = list.foreground
                        sub.foreground = JBColor.GRAY
                    }
                    return panel
                }
            }
            add(JBScrollPane(list), BorderLayout.CENTER)

            // List navigation on double-click/enter
            list.addMouseListener(object : java.awt.event.MouseAdapter() {
                override fun mouseClicked(e: java.awt.event.MouseEvent) {
                    if (e.clickCount == 2) navigateToSelected()
                }
            })
            list.inputMap.put(KeyStroke.getKeyStroke("ENTER"), "navigate")
            list.actionMap.put("navigate", object : AbstractAction() {
                override fun actionPerformed(e: java.awt.event.ActionEvent?) = navigateToSelected()
            })

            // Actions
            val filterListener = ActionListener { applyFilterAndUpdate() }
            inProgressToggle.addActionListener(filterListener)
            completedToggle.addActionListener(filterListener)
            refreshButton.addActionListener { refresh() }

            // Initial load
            refresh()
        }

        private fun navigateToSelected() {
            val item = list.selectedValue ?: return
            val vf = item.file
            ApplicationManager.getApplication().invokeLater {
                com.intellij.openapi.fileEditor.OpenFileDescriptor(project, vf, item.offset).navigate(true)
            }
        }

        private fun refresh() {
            // Disable refresh while scanning and show transient status
            refreshButton.isEnabled = false
            progressLabel.text = "Scanning..."

            ReadAction
                .nonBlocking<List<TodoScannerService.TaskItem>> { service.scan() }
                .inSmartMode(project)
                .expireWith(project)
                .coalesceBy(this, "CodeTasksScan")
                .finishOnUiThread(ModalityState.any()) { items ->
                    allItems = items
                    applyFilterAndUpdate()
                    refreshButton.isEnabled = true
                }
                .submit(TodoScannerService.executor)
        }


        private fun applyFilterAndUpdate() {
            val showingDone = completedToggle.isSelected
            val filtered = allItems.filter { (it.status == Status.DONE) == showingDone }
            listModel.clear()
            filtered.forEach { listModel.addElement(it) }

            val total = allItems.size
            val done = allItems.count { it.status == Status.DONE }
            val percent = if (total == 0) 0 else (done * 100 / total)
            progressLabel.text = "$percent% solved ($done/$total)"
            progressBar.value = percent
        }
    }
}

package com.github.mmileticc.pluginintellij1st

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.ui.Messages

class MyAction2 : AnAction("Dodaj Komentar Svim Funkcijama") {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val file = e.getData(CommonDataKeys.PSI_FILE) ?: return

        // Ovde pozivaš svoju logiku
        MyPluginLogic2.run(project, file)
    }
}
package com.github.mmileticc.pluginintellij1st.roasting

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.application.ApplicationManager

abstract class BaseRoastLanguageAction(text: String) : ToggleAction(text) {
    abstract val target: RoastSettings.Language

    private fun settings(): RoastSettings = ApplicationManager.getApplication().getService(RoastSettings::class.java)

    override fun isSelected(e: AnActionEvent): Boolean {
        return settings().getLanguage() == target
    }

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        if (state) settings().setLanguage(target)
    }
}

class RoastLanguageEnglishAction : BaseRoastLanguageAction("Roast language: English") {
    override val target: RoastSettings.Language = RoastSettings.Language.ENGLISH
}

class RoastLanguageSerbianAction : BaseRoastLanguageAction("Roast language: Serbian") {
    override val target: RoastSettings.Language = RoastSettings.Language.SERBIAN
}

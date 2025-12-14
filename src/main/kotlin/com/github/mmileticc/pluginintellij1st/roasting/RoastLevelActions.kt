package com.github.mmileticc.pluginintellij1st.roasting

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.application.ApplicationManager

abstract class BaseRoastLevelAction(text: String) : ToggleAction(text) {
    abstract val target: RoastSettings.Level

    private fun settings(): RoastSettings = ApplicationManager.getApplication().getService(RoastSettings::class.java)

    override fun isSelected(e: AnActionEvent): Boolean {
        return settings().getLevel() == target
    }

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        if (state) settings().setLevel(target)
    }
}

class RoastLevelLowAction : BaseRoastLevelAction("Roast level: Low") {
    override val target: RoastSettings.Level = RoastSettings.Level.LOW
}

class RoastLevelMediumAction : BaseRoastLevelAction("Roast level: Medium") {
    override val target: RoastSettings.Level = RoastSettings.Level.MEDIUM
}

class RoastLevelHighAction : BaseRoastLevelAction("Roast level: High") {
    override val target: RoastSettings.Level = RoastSettings.Level.HIGH
}

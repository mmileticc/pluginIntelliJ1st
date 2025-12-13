package com.github.mmileticc.pluginintellij1st

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@State(name = "RoastSettings", storages = [Storage("roast_settings.xml")])
@Service(Service.Level.APP)
class RoastSettings : PersistentStateComponent<RoastSettings.State> {

    enum class Level { LOW, MEDIUM, HIGH }
    enum class Language { ENGLISH, SERBIAN }

    class State {
        var level: Level = Level.MEDIUM
        var language: Language = Language.ENGLISH
    }

    private var state = State()

    override fun getState(): State = state

    override fun loadState(state: State) {
        this.state = state
    }

    fun getLevel(): Level = state.level

    fun setLevel(level: Level) {
        state.level = level
    }

    fun getLanguage(): Language = state.language

    fun setLanguage(language: Language) {
        state.language = language
    }
}

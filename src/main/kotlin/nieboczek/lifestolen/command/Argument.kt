package nieboczek.lifestolen.command

import nieboczek.lifestolen.Lifestolen

class Argument(val name: String, val type: Type, val value: String = "") {
    enum class Type {
        STRING, GREEDY_STRING, MODULE, CONFIG_KEY, FRIENDS;
    }

    internal fun suggest(builder: CommandExecutor.FilteredSuggestionBuilder) {
        when (type) {
            Type.MODULE -> builder.suggest(Lifestolen.modules.map { it.id })
            Type.CONFIG_KEY -> builder.suggest("RenderClientBrandText", "CommandPrefix", "CorrectYaw")
            Type.FRIENDS -> builder.suggest(Lifestolen.cfg.friends)
            else -> {}
        }
    }
}

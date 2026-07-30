package nieboczek.lifestolen.command

import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import nieboczek.lifestolen.Lifestolen
import java.util.concurrent.CompletableFuture

object CommandExecutor {
    fun execute(command: String) {
        if (command.isBlank()) return
        val noPrefix = command.trim().removePrefix(Lifestolen.cfg.commandPrefix)
        val parts = noPrefix.split(" ")
        val root = Commands.commands.find { it.name == parts[0] } ?: return

        var current = root
        var i = 1
        while (i < parts.size) {
            val sub = current.subcommands.find { it.name == parts[i] }
            if (sub != null) {
                current = sub
                i++
            } else {
                break
            }
        }

        val remaining = parts.drop(i)
        val resolvedArgs = mutableListOf<Argument>()
        var argIdx = 0
        var tokenIdx = 0
        while (argIdx < current.arguments.size && tokenIdx < remaining.size) {
            val def = current.arguments[argIdx]
            val value = when (def.type) {
                Argument.Type.GREEDY_STRING -> remaining.drop(tokenIdx).joinToString(" ")
                else -> remaining[tokenIdx]
            }
            resolvedArgs.add(Argument(def.name, def.type, value))
            tokenIdx += if (def.type == Argument.Type.GREEDY_STRING) remaining.size - tokenIdx else 1
            argIdx++
        }

        if (tokenIdx < remaining.size) error("Too many arguments")

        current.executeFn?.invoke(Command.Ctx(resolvedArgs))
    }

    fun autocomplete(incomplete: String, cursor: Int): CompletableFuture<Suggestions> {
        val text = incomplete.substring(0, cursor.coerceAtMost(incomplete.length))
        val prefixLength = Lifestolen.cfg.commandPrefix.length
        val stripped = text.removePrefix(Lifestolen.cfg.commandPrefix)

        if (stripped.isBlank()) {
            val builder = FilteredSuggestionBuilder(text, prefixLength)
            Commands.commands.forEach { builder.suggest(it.name) }
            return builder.buildFuture()
        }

        val tokens = stripped.split(" ")
        val tailIsWhitespace = stripped.endsWith(" ")
        val completeTokens = (if (tailIsWhitespace) tokens else tokens.dropLast(1)).filter { it.isNotEmpty() }
        val start = stripped.lastIndexOf(' ') + 1

        if (completeTokens.isEmpty()) {
            val builder = FilteredSuggestionBuilder(text, prefixLength + start)
            Commands.commands.forEach { builder.suggest(it.name) }
            return builder.buildFuture()
        }

        var current = Commands.commands.find { it.name == completeTokens[0] }
            ?: return Suggestions.empty()

        var i = 1
        while (i < completeTokens.size) {
            val sub = current.subcommands.find { it.name == completeTokens[i] }
            if (sub != null) {
                current = sub
                i++
            } else {
                return Suggestions.empty()
            }
        }

        val builder = FilteredSuggestionBuilder(text, prefixLength + start)
        current.subcommands.forEach { builder.suggest(it.name) }

        if (current.arguments.isNotEmpty()) {
            when (current.arguments[0].type) {
                Argument.Type.MODULE -> Lifestolen.modules.forEach { builder.suggest(it.id) }
                else -> {}
            }
        }

        return builder.buildFuture()
    }

    private class FilteredSuggestionBuilder(input: String, start: Int) : SuggestionsBuilder(input, start) {
        override fun suggest(text: String): SuggestionsBuilder {
            if (text.lowercase().startsWith(remainingLowerCase)) super.suggest(text)
            return this
        }
    }
}

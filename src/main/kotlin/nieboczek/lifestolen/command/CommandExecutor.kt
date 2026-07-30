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

        val (current, i) = traverseSubcommands(root, parts)
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

        if (tokenIdx < remaining.size) throw CommandError("Too many arguments")
        current.executeFn?.invoke(Command.Ctx(resolvedArgs))
    }

    fun getUsageInfo(text: String): String? {
        val prefix = Lifestolen.cfg.commandPrefix
        if (!text.startsWith(prefix)) return null
        val stripped = text.removePrefix(prefix)
        if (stripped.isBlank()) return null

        val tokens = stripped.split(" ")
        val tailIsWhitespace = stripped.endsWith(" ")
        val completeTokens = (if (tailIsWhitespace) tokens else tokens.dropLast(1)).filter { it.isNotEmpty() }

        var current = Commands.commands.find { it.name == completeTokens.getOrNull(0) } ?: return null
        val (resolved, idx) = traverseSubcommands(current, completeTokens)
        current = resolved

        val remaining = completeTokens.drop(idx)
        var tokenIdx = 0
        var argIdx = 0
        while (argIdx < current.arguments.size && tokenIdx < remaining.size) {
            val def = current.arguments[argIdx]
            tokenIdx += if (def.type == Argument.Type.GREEDY_STRING) remaining.size - tokenIdx else 1
            argIdx++
        }

        val arg = current.arguments[argIdx]
        return "<${arg.name}>"
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

        val resolved = Commands.commands.find { it.name == completeTokens[0] }
            ?: return Suggestions.empty()

        val (traversed, i) = traverseSubcommands(resolved, completeTokens)
        if (i < completeTokens.size) return Suggestions.empty()
        var current = traversed

        val builder = FilteredSuggestionBuilder(text, prefixLength + start)
        current.subcommands.forEach { builder.suggest(it.name) }

        if (current.arguments.isNotEmpty()) {
            when (current.arguments[0].type) {
                Argument.Type.MODULE -> Lifestolen.modules.forEach { builder.suggest(it.id) }
                Argument.Type.CONFIG_KEY -> {
                    builder.suggest("RenderClientBrandText")
                    builder.suggest("CommandPrefix")
                }
                else -> {}
            }
        }

        return builder.buildFuture()
    }

    private fun traverseSubcommands(current: Command, tokens: List<String>, startIdx: Int = 1): Pair<Command, Int> {
        var node = current
        var i = startIdx
        while (i < tokens.size) {
            val sub = node.subcommands.find { it.name == tokens[i] }
            if (sub != null) {
                node = sub
                i++
            } else {
                break
            }
        }
        return node to i
    }

    private class FilteredSuggestionBuilder(input: String, start: Int) : SuggestionsBuilder(input, start) {
        override fun suggest(text: String): SuggestionsBuilder {
            if (text.lowercase().startsWith(remainingLowerCase)) super.suggest(text)
            return this
        }
    }
}

package nieboczek.lifestolen.command

import nieboczek.lifestolen.Lifestolen
import nieboczek.lifestolen.module.Module

class Command(val name: String) {
    internal var executeFn: ((Ctx) -> Unit)? = null
    internal var subcommands = mutableListOf<Command>()
    internal var arguments = mutableListOf<Argument>()

    fun executes(fn: (Ctx) -> Unit): Command {
        executeFn = fn
        return this
    }

    fun subcommand(command: Command): Command {
        subcommands.add(command)
        return this
    }

    fun argument(argument: Argument): Command {
        arguments.add(argument)
        return this
    }

    fun register() {
        Commands.commands.add(this)
    }

    class Ctx(val arguments: List<Argument> = listOf()) {

        private fun getArg(name: String): Argument {
            val arg = arguments.find { it.name == name }
            if (arg == null) error("Argument $name was not supplied")
            return arg
        }

        fun getString(name: String): String = getArg(name).value

        fun getModule(name: String): Module {
            val argVal = getArg(name).value.lowercase()
            val module = Lifestolen.modules.find { it.id.lowercase() == argVal }
            if (module == null) error("No module with ID: $argVal")
            return module
        }
    }
}

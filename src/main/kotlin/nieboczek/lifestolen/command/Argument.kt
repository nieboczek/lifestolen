package nieboczek.lifestolen.command

class Argument(val name: String, val type: Type, val value: String = "") {
    enum class Type {
        STRING, GREEDY_STRING, MODULE, CONFIG_KEY;
    }
}

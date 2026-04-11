package nieboczek.lifestolen.serializer.lang

class Token(val type: TokenType, val text: String?) {
    constructor(type: TokenType) : this(type, null)
}

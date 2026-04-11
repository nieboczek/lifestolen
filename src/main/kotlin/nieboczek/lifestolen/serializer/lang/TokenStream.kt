package nieboczek.lifestolen.serializer.lang

import nieboczek.lifestolen.serializer.SerializerError

class TokenStream(private val src: String) {
    private var i = 0
    private var peekResult: Token? = null

    fun expect(type: TokenType) {
        val actualType = next().type
        if (actualType != type) throw SerializerError("[TokenStream::expect] Expected token $type but got $actualType")
    }

    fun nextTokenText(type: TokenType): String {
        val token = next()
        if (token.type != type) throw SerializerError("[TokenStream::nextTokenText] Expected token $type but got ${token.type}")
        return token.text!!
    }

    /** Returns false on consumption, true otherwise. Useful for while loops. */
    fun continueIfNot(type: TokenType?): Boolean {
        if (peek().type == type) {
            next()
            return false
        }
        return true
    }

    fun peek(): Token {
        peekResult?.let { return it }
        return next().let {
            peekResult = it
            it
        }
    }

    fun next(): Token {
        peekResult?.let {
            peekResult = null
            return it
        }

        skipWhitespace()

        if (i >= src.length) {
            return Token(TokenType.EOF)
        }

        val c = src[i]

        when (c) {
            '{' -> {
                i++
                return Token(TokenType.L_BRACE)
            }

            '}' -> {
                i++
                return Token(TokenType.R_BRACE)
            }

            '[' -> {
                i++
                return Token(TokenType.L_BRACKET)
            }

            ']' -> {
                i++
                return Token(TokenType.R_BRACKET)
            }

            '=' -> {
                i++
                return Token(TokenType.EQUAL)
            }

            ';' -> {
                i++
                return Token(TokenType.SEMICOLON)
            }

            '"' -> return readString()
        }

        if (c == '.') {
            return readDotOrRange()
        }

        if (isNumberStart(c)) {
            return readNumber()
        }

        if (isIdentifierStart(c)) {
            return readIdentifier()
        }

        throw SerializerError("[TokenStream::next] Unexpected character: $c")
    }

    private fun readString(): Token {
        val sb = StringBuilder()
        i++ // skip "

        while (i < src.length) {
            val c = src[i]

            if (c == '\\') {
                i++
                if (i < src.length) {
                    when (val ch = src[i]) {
                        '\\', '"' -> sb.append(ch)
                        'n' -> sb.append("\n")
                        else -> throw SerializerError("[TokenStream::readString] Invalid escape code: \\$ch")
                    }
                    i++
                }
                continue
            }

            if (c == '"') {
                i++ // consume "
                break
            }

            sb.append(c)
            i++
        }

        return Token(TokenType.STRING, sb.toString())
    }

    private fun readDotOrRange(): Token {
        i++ // consume first '.'
        
        if (i < src.length && src[i] == '.') {
            i++ // consume second '.'
            return Token(TokenType.RANGE_INFIX)
        }

        if (i < src.length && Character.isDigit(src[i])) {
            return readNumberStartingWithDot()
        }

        throw SerializerError("[TokenStream::readDotOrRange] Unexpected '.' character")
    }

    private fun readNumberStartingWithDot(): Token {
        val start = i - 1 // include the leading '.'
        while (i < src.length && isNumberPart(src[i])) {
            i++
        }
        return Token(TokenType.NUMBER, src.substring(start, i))
    }

    private fun readNumber(): Token {
        val start = i
        while (i < src.length && isNumberPart(src[i])) {
            if (src[i] == '.' && i + 1 < src.length && src[i + 1] == '.') {
                break
            }
            i++
        }
        return Token(TokenType.NUMBER, src.substring(start, i))
    }

    private fun isNumberStart(c: Char): Boolean {
        return Character.isDigit(c)
    }

    private fun isNumberPart(c: Char): Boolean {
        return isNumberStart(c) || c == '.'
    }

    private fun readIdentifier(): Token {
        val start = i
        while (i < src.length && isIdentifierPart(src[i])) {
            i++
        }
        return Token(TokenType.IDENTIFIER, src.substring(start, i))
    }

    private fun isIdentifierStart(c: Char): Boolean {
        return Character.isLetter(c) || c == '_'
    }

    private fun isIdentifierPart(c: Char): Boolean {
        return isIdentifierStart(c) || Character.isDigit(c)
    }

    private fun skipWhitespace() {
        while (i < src.length && Character.isWhitespace(src[i])) {
            i++
        }
    }
}

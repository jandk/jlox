package be.tjoener.jlox.parser

import be.tjoener.jlox.parser.TokenType.EOF

class Scanner(val source: String) {

    private val tokens = mutableListOf<Token>()

    private var start: Int = 0
    private var current: Int = 0
    private var line: Int = 1

    fun scanTokens(): List<Token> {
        while (!isAtEnd()) {
            // We are at the beginning of the next lexeme.
            start = current
            scanToken()
        }

        tokens.add(Token(EOF, "", null, line))
        return tokens
    }

    private fun scanToken() {
        TODO()
    }

    private fun isAtEnd(): Boolean {
        return current >= source.length
    }

}

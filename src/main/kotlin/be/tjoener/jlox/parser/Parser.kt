package be.tjoener.jlox.parser

import be.tjoener.jlox.JLox
import be.tjoener.jlox.ast.*
import be.tjoener.jlox.parser.TokenType.*

class Parser(private val tokens: List<Token>) {

    class ParseError : RuntimeException()

    private var current = 0

    fun parse(): Expr? {
        return try {
            expression()
        } catch (e: ParseError) {
            null
        }
    }

    private fun expression(): Expr {
        return equality()
    }

    private fun equality(): Expr {
        var expr = comparison()

        while (match(EQUAL_EQUAL, BANG_EQUAL)) {
            val operator = previous()
            val right = comparison()
            expr = Binary(expr, operator, right)
        }

        return expr
    }

    private fun comparison(): Expr {
        var expr = addition()

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            val operator = previous()
            val right = addition()
            expr = Binary(expr, operator, right)
        }

        return expr
    }

    private fun addition(): Expr {
        var expr = multiplication()

        while (match(PLUS, MINUS)) {
            val operator = previous()
            val right = multiplication()
            expr = Binary(expr, operator, right)
        }

        return expr
    }

    private fun multiplication(): Expr {
        var expr = unary()

        while (match(SLASH, STAR)) {
            val operator = previous()
            val right = unary()
            expr = Binary(expr, operator, right)
        }

        return expr
    }

    private fun unary(): Expr {
        if (match(BANG, MINUS)) {
            val operator = previous()
            val right = unary()
            return Unary(operator, right)
        }

        return primary()
    }

    private fun primary(): Expr {
        if (match(FALSE)) return Literal(false)
        if (match(TRUE)) return Literal(true)
        if (match(NIL)) return Literal(null)

        if (match(NUMBER, STRING)) {
            return Literal(previous().literal)
        }

        if (match(LEFT_PAREN)) {
            val expr = expression()
            consume(RIGHT_PAREN, "Expect ')' after expression")
            return Grouping(expr)
        }

        throw error(peek(), "Expect expression")
    }

    private fun match(vararg types: TokenType): Boolean {
        val matches = types.any { check(it) }
        if (matches) advance()
        return matches
    }

    private fun check(tokenType: TokenType): Boolean {
        return if (isAtEnd()) false else peek().type == tokenType
    }

    private fun advance(): Token {
        if (!isAtEnd()) current++
        return previous()
    }

    private fun isAtEnd(): Boolean {
        return peek().type == EOF
    }

    private fun peek(): Token {
        return tokens[current]
    }

    private fun previous(): Token {
        return tokens[current - 1]
    }

    private fun consume(type: TokenType, message: String): Token {
        if (check(type)) return advance()

        throw error(peek(), message)
    }

    private fun error(token: Token, message: String): ParseError {
        JLox.error(token, message)
        throw ParseError()
    }

    private fun synchronize() {
        val boundaries = listOf(CLASS, FUN, VAR, FOR, IF, WHILE, PRINT, RETURN)

        advance()
        while (!isAtEnd()) {
            if (previous().type == SEMICOLON) return
            if (peek().type in boundaries) return
            advance()
        }
    }

}

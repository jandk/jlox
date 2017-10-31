package be.tjoener.jlox

import be.tjoener.jlox.ast.LoxValue
import be.tjoener.jlox.parser.Token

class Environment(val enclosing: Environment? = null) {

    private val values: MutableMap<String, LoxValue> = hashMapOf()

    fun define(name: String, value: LoxValue) {
        values.put(name, value)
    }

    fun get(name: Token): LoxValue {
        val value = values[name.lexeme]
        if (value != null) {
            return value
        }

        if (enclosing != null) {
            return enclosing.get(name)
        }

        throw RuntimeError(name, "Undefined variable '${name.lexeme}'")
    }

    fun assign(name: Token, value: LoxValue) {
        if (values.containsKey(name.lexeme)) {
            values.put(name.lexeme, value)
            return
        }

        if (enclosing != null) {
            enclosing.assign(name, value)
            return
        }

        throw RuntimeError(name, "Undefined variable '${name.lexeme}'")
    }

}

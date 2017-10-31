package be.tjoener.jlox

import be.tjoener.jlox.ast.LoxValue
import be.tjoener.jlox.parser.Token

class Environment {

    private val values: MutableMap<String, LoxValue> = hashMapOf()

    fun define(name: String, value: LoxValue) {
        values.put(name, value)
    }

    fun get(name: Token): LoxValue {
        return values[name.lexeme]
            ?: throw RuntimeError(name, "Undefined variable '${name.lexeme}'")
    }

}

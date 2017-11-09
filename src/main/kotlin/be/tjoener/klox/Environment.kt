package be.tjoener.klox

import be.tjoener.klox.ast.LoxNil
import be.tjoener.klox.ast.LoxValue
import be.tjoener.klox.parser.Token

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

    fun getAt(distance: Int, name: String): LoxValue {
        return ancestor(distance).values.getOrDefault(name, LoxNil)
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

    fun assignAt(distance: Int, name: Token, value: LoxValue) {
        ancestor(distance).values.put(name.lexeme, value)
    }

    fun ancestor(distance: Int): Environment {
        var environment: Environment = this
        for (i in 0 until distance) {
            environment = environment.enclosing!!
        }

        return environment
    }

}

package be.tjoener.jlox.ast

import be.tjoener.jlox.Environment
import be.tjoener.jlox.Interpreter
import be.tjoener.jlox.Interpreter.ReturnValue

sealed class LoxValue {

    open fun asDouble(): Double = throw ex("Double")

    private fun ex(type: String): Nothing =
        throw IllegalStateException("Not of type $type")
}


object LoxNil : LoxValue() {
    override fun toString() = "nil"
}

data class LoxBool(val value: Boolean) : LoxValue() {
    override fun toString() = value.toString()
}

data class LoxDouble(val value: Double) : LoxValue() {
    override fun asDouble() = value
    override fun toString() = value.toString()
}

data class LoxString(val value: String) : LoxValue() {
    override fun toString() = value
}

abstract class LoxCallable : LoxValue() {
    abstract val arity: Int
    abstract fun call(interpreter: Interpreter, arguments: List<LoxValue>): LoxValue
}

class LoxFunction(private val declaration: Function, private val closure: Environment) : LoxCallable() {
    override val arity: Int
        get() = declaration.parameters.size

    override fun call(interpreter: Interpreter, arguments: List<LoxValue>): LoxValue {
        val environment = Environment(closure)
        for (i in 0 until declaration.parameters.size) {
            environment.define(declaration.parameters[i].lexeme, arguments[i])
        }

        try {
            interpreter.executeBlock(declaration.body, environment)
        } catch (returnValue: ReturnValue) {
            return returnValue.value
        }
        return LoxNil
    }

    override fun toString(): String {
        return "<fn ${declaration.name.lexeme}>"
    }
}

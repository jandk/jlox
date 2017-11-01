package be.tjoener.jlox.ast

import be.tjoener.jlox.Environment
import be.tjoener.jlox.Interpreter
import be.tjoener.jlox.Interpreter.ReturnValue
import be.tjoener.jlox.RuntimeError
import be.tjoener.jlox.parser.Token

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

    fun bind(instance: LoxInstance): LoxFunction {
        val environment = Environment(closure)
        environment.define("this", instance)
        return LoxFunction(declaration, environment)
    }

    override fun toString(): String {
        return "<fn ${declaration.name.lexeme}>"
    }
}

class LoxClass(val name: String, val methods: Map<String, LoxFunction>) : LoxCallable() {
    override val arity: Int
        get() = 0

    override fun call(interpreter: Interpreter, arguments: List<LoxValue>): LoxValue {
        val instance = LoxInstance(this)
        return instance
    }

    fun findMethod(instance: LoxInstance, name: String): LoxFunction? {
        return methods[name]?.bind(instance)
    }

    override fun toString() = name
}

class LoxInstance(val klass: LoxClass) : LoxValue() {
    private val fields = mutableMapOf<String, LoxValue>()

    fun get(name: Token): LoxValue {
        val value = fields[name.lexeme]
        if (value != null) {
            return value
        }

        val method = klass.findMethod(this, name.lexeme)
        if (method != null) {
            return method
        }

        throw RuntimeError(name, "Undefined property '${name.lexeme}'")
    }

    fun set(name: Token, value: LoxValue) {
        fields.put(name.lexeme, value)
    }

    override fun toString() = "${klass.name} instance"
}

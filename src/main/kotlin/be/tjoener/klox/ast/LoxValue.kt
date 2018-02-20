package be.tjoener.klox.ast

import be.tjoener.klox.Environment
import be.tjoener.klox.Interpreter
import be.tjoener.klox.Interpreter.ReturnValue
import be.tjoener.klox.RuntimeError
import be.tjoener.klox.parser.Token

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

class LoxFunction(
    val declaration: Function,
    val closure: Environment,
    val isInitializer: Boolean
) : LoxCallable() {
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

        if (isInitializer) return closure.getAt(0, "this")
        return LoxNil
    }

    fun bind(instance: LoxInstance): LoxFunction {
        val environment = Environment(closure)
        environment.define("this", instance)
        return LoxFunction(declaration, environment, isInitializer)
    }

    override fun toString(): String {
        return "<fn ${declaration.name.lexeme}>"
    }
}

class LoxClass(val name: String, val superclass: LoxClass?, val methods: Map<String, LoxFunction>) : LoxCallable() {
    override val arity: Int
        get() = methods["init"]?.arity ?: 0

    override fun call(interpreter: Interpreter, arguments: List<LoxValue>): LoxValue {
        val instance = LoxInstance(this)
        methods["init"]
            ?.bind(instance)
            ?.call(interpreter, arguments)
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
        fields[name.lexeme] = value
    }

    override fun toString() = "${klass.name} instance"
}

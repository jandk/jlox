package be.tjoener.jlox.ast

sealed class LoxValue {
    open fun isNil() = false
    open fun isBool() = false
    open fun isDouble() = false
    open fun isString() = false

    open fun asBool(): Boolean = throw ex("Bool")
    open fun asDouble(): Double = throw ex("Double")
    open fun asString(): String = throw ex("String")

    private fun ex(type: String): Nothing =
        throw IllegalStateException("Not of type $type")
}


object LoxNil : LoxValue() {
    override fun isNil() = true
    override fun toString() = "nil"
}

data class LoxBool(val value: Boolean) : LoxValue() {
    override fun isBool() = true
    override fun asBool() = value
    override fun toString() = value.toString()
}

data class LoxDouble(val value: Double) : LoxValue() {
    override fun isDouble() = true
    override fun asDouble() = value
    override fun toString() = value.toString()
}

data class LoxString(val value: String) : LoxValue() {
    override fun isString() = true
    override fun asString() = value
    override fun toString() = value
}

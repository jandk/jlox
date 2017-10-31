package be.tjoener.jlox.ast

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

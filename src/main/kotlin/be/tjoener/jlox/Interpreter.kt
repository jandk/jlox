package be.tjoener.jlox

import be.tjoener.jlox.ast.*
import be.tjoener.jlox.parser.TokenType.*
import java.util.*

class Interpreter : Visitor<LoxValue> {

    fun evaluate(expr: Expr): LoxValue {
        return expr.accept(this)
    }

    override fun visitLiteralExpr(expr: Literal): LoxValue {
        return expr.value
    }

    override fun visitGroupingExpr(expr: Grouping): LoxValue {
        return evaluate(expr.expression)
    }

    override fun visitUnaryExpr(expr: Unary): LoxValue {
        val right = evaluate(expr.right)

        return when (expr.operator.type) {
            BANG -> LoxBool(!isTruthy(right))
            MINUS -> LoxDouble(-right.asDouble())
            else -> throw NotImplementedError()
        }
    }

    override fun visitBinaryExpr(expr: Binary): LoxValue {
        val left = evaluate(expr.left)
        val right = evaluate(expr.right)

        return when (expr.operator.type) {
            PLUS -> {
                when {
                    left.isDouble() && right.isDouble() -> LoxDouble(left.asDouble() + right.asDouble())
                    left.isString() && right.isString() -> LoxString(left.asString() + right.asString())
                    else -> throw NotImplementedError()
                }
            }
            MINUS -> LoxDouble(left.asDouble() - right.asDouble())
            STAR -> LoxDouble(left.asDouble() * right.asDouble())
            SLASH -> LoxDouble(left.asDouble() / right.asDouble())

            GREATER -> return LoxBool(left.asDouble() > right.asDouble())
            GREATER_EQUAL -> return LoxBool(left.asDouble() >= right.asDouble())
            LESS -> return LoxBool(left.asDouble() < right.asDouble())
            LESS_EQUAL -> return LoxBool(left.asDouble() <= right.asDouble())

            BANG_EQUAL -> return LoxBool(!isEqual(left, right))
            EQUAL_EQUAL -> return LoxBool(isEqual(left, right))

            else -> throw NotImplementedError()
        }
    }

    private fun isTruthy(value: LoxValue): Boolean {
        if (value.isNil()) return false
        if (value.isBool()) return value.asBool()
        return true
    }

    private fun isEqual(left: LoxValue, right: LoxValue): Boolean {
        return Objects.equals(left, right)
    }

}

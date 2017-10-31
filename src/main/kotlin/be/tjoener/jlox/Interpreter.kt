package be.tjoener.jlox

import be.tjoener.jlox.ast.*
import be.tjoener.jlox.parser.Token
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
            MINUS -> {
                checkNumberOperand(expr.operator, right)
                LoxDouble(-right.asDouble())
            }
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
                    else -> throw RuntimeError(expr.operator, "Operands must be two numbers or two strings")
                }
            }


            MINUS -> {
                checkNumberOperands(expr.operator, left, right)
                LoxDouble(left.asDouble() - right.asDouble())
            }
            STAR -> {
                checkNumberOperands(expr.operator, left, right)
                LoxDouble(left.asDouble() * right.asDouble())
            }
            SLASH -> {
                checkNumberOperands(expr.operator, left, right)
                LoxDouble(left.asDouble() / right.asDouble())
            }


            GREATER -> {
                checkNumberOperands(expr.operator, left, right)
                return LoxBool(left.asDouble() > right.asDouble())
            }
            GREATER_EQUAL -> {
                checkNumberOperands(expr.operator, left, right)
                return LoxBool(left.asDouble() >= right.asDouble())
            }
            LESS -> {
                checkNumberOperands(expr.operator, left, right)
                return LoxBool(left.asDouble() < right.asDouble())
            }
            LESS_EQUAL -> {
                checkNumberOperands(expr.operator, left, right)
                return LoxBool(left.asDouble() <= right.asDouble())
            }


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

    private fun checkNumberOperand(operator: Token, operand: LoxValue) {
        if (operand.isDouble()) return
        throw RuntimeError(operator, "Operand must be a number")
    }

    private fun checkNumberOperands(operator: Token, left: LoxValue, right: LoxValue) {
        if (left.isDouble() && right.isDouble()) return
        throw RuntimeError(operator, "Operands must be numbers")
    }

}

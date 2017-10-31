package be.tjoener.jlox

import be.tjoener.jlox.ast.*
import be.tjoener.jlox.parser.Token
import be.tjoener.jlox.parser.TokenType.*
import java.util.*

class Interpreter : Expr.Visitor<LoxValue>, Stmt.Visitor<Unit> {

    private var environment = Environment()

    fun interpret(statements: List<Stmt>) {
        try {
            for (statement in statements) {
                execute(statement)
            }
        } catch (error: RuntimeError) {
            JLox.runtimeError(error)
        }
    }

    private fun stringify(value: LoxValue): String {
        if (value is LoxDouble) {
            return value.toString().removeSuffix(".0")
        }
        return value.toString()
    }


    private fun evaluate(expr: Expr): LoxValue {
        return expr.accept(this)
    }

    private fun execute(stmt: Stmt) {
        stmt.accept(this)
    }

    private fun executeBlock(statements: List<Stmt>, environment: Environment) {
        val previous = this.environment
        try {
            this.environment = environment

            for (statement in statements) {
                execute(statement)
            }
        } finally {
            this.environment = previous
        }
    }


    override fun visitExpressionStmt(stmt: Expression) {
        evaluate(stmt.expression)
    }

    override fun visitPrintStmt(stmt: Print) {
        val value = evaluate(stmt.expression)
        println(stringify(value))
    }

    override fun visitVarStmt(stmt: Var) {
        var value: LoxValue = LoxNil
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer)
        }

        environment.define(stmt.name.lexeme, value)
    }

    override fun visitAssignExpr(expr: Assign): LoxValue {
        val value = evaluate(expr.value)

        environment.assign(expr.name, value)
        return value
    }

    override fun visitBlockStmt(stmt: Block) {
        executeBlock(stmt.statements, Environment(environment))
    }

    override fun visitIfStmt(stmt: If) {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch)
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch)
        }
    }

    override fun visitWhileStmt(stmt: While) {
        while (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.body)
        }
    }

    override fun visitCallExpr(expr: Call): LoxValue {
        val callee = evaluate(expr.callee)
        val arguments = expr.arguments.map { evaluate(it) }

        val function = callee as? LoxCallable
            ?: throw RuntimeError(expr.paren, "Can only call functions and classes")

        if (arguments.size != function.arity) {
            throw RuntimeError(expr.paren, "Expected ${function.arity} arguments but got ${arguments.size}")
        }

        return function.call(this, arguments)
    }


    override fun visitLogicalExpr(expr: Logical): LoxValue {
        val left = evaluate(expr.left)

        if (expr.operator.type == OR) {
            if (isTruthy(left)) return left
        } else {
            if (!isTruthy(left)) return left
        }

        return evaluate(expr.right)
    }

    override fun visitVariableExpr(expr: Variable): LoxValue {
        return environment.get(expr.name)
    }

    override fun visitLiteralExpr(expr: Literal): LoxValue {
        return when (expr.value) {
            null -> LoxNil
            is Boolean -> LoxBool(expr.value)
            is Double -> LoxDouble(expr.value)
            is String -> LoxString(expr.value)
            else -> error("Invalid literal type")
        }
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

        when (expr.operator.type) {
            PLUS -> {
                return when {
                    left is LoxDouble && right is LoxDouble -> LoxDouble(left.value + right.value)
                    left is LoxString && right is LoxString -> LoxString(left.value + right.value)
                    else -> throw RuntimeError(expr.operator, "Operands must be two numbers or two strings")
                }
            }


            MINUS -> {
                checkNumberOperands(expr.operator, left, right)
                return LoxDouble(left.asDouble() - right.asDouble())
            }
            STAR -> {
                checkNumberOperands(expr.operator, left, right)
                return LoxDouble(left.asDouble() * right.asDouble())
            }
            SLASH -> {
                checkNumberOperands(expr.operator, left, right)
                return LoxDouble(left.asDouble() / right.asDouble())
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
        if (value is LoxNil) return false
        if (value is LoxBool) return value.value
        return true
    }

    private fun isEqual(left: LoxValue, right: LoxValue): Boolean {
        return Objects.equals(left, right)
    }

    private fun checkNumberOperand(operator: Token, operand: LoxValue) {
        if (operand is LoxDouble) return
        throw RuntimeError(operator, "Operand must be a number")
    }

    private fun checkNumberOperands(operator: Token, left: LoxValue, right: LoxValue) {
        if (left is LoxDouble && right is LoxDouble) return
        throw RuntimeError(operator, "Operands must be numbers")
    }

}

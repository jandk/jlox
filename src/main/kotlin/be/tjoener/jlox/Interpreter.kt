package be.tjoener.jlox

import be.tjoener.jlox.ast.*
import be.tjoener.jlox.ast.Function
import be.tjoener.jlox.parser.Token
import be.tjoener.jlox.parser.TokenType.*
import java.util.*

class Interpreter : Expr.Visitor<LoxValue>, Stmt.Visitor<Unit> {

    class ReturnValue(val value: LoxValue) : RuntimeException(null, null, false, false)

    private val globals = Environment()
    private val locals = mutableMapOf<Expr, Int>()

    private var environment = globals

    init {
        globals.define("clock", object : LoxCallable() {
            override val arity: Int
                get() = 0

            override fun call(interpreter: Interpreter, arguments: List<LoxValue>): LoxValue {
                return LoxDouble(System.currentTimeMillis() / 1000.0)
            }
        })
    }


    fun interpret(statements: List<Stmt>) {
        try {
            for (statement in statements) {
                execute(statement)
            }
        } catch (error: RuntimeError) {
            JLox.runtimeError(error)
        }
    }


    override fun visitAssignExpr(expr: Assign): LoxValue {
        val value = evaluate(expr.value)

        val distance = locals.get(expr)
        if (distance != null) {
            environment.assignAt(distance, expr.name, value)
        } else {
            globals.assign(expr.name, value)
        }

        environment.assign(expr.name, value)
        return value
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

    override fun visitGroupingExpr(expr: Grouping): LoxValue {
        return evaluate(expr.expression)
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

    override fun visitLogicalExpr(expr: Logical): LoxValue {
        val left = evaluate(expr.left)

        if (expr.operator.type == OR) {
            if (isTruthy(left)) return left
        } else {
            if (!isTruthy(left)) return left
        }

        return evaluate(expr.right)
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

    override fun visitVariableExpr(expr: Variable): LoxValue {
        return lookupVariable(expr.name, expr)
    }


    override fun visitBlockStmt(stmt: Block) {
        executeBlock(stmt.statements, Environment(environment))
    }

    override fun visitClassStmt(stmt: Class) {
        environment.define(stmt.name.lexeme, LoxNil)
        val klass = LoxClass(stmt.name.lexeme)
        environment.assign(stmt.name, klass)
    }

    override fun visitExpressionStmt(stmt: Expression) {
        evaluate(stmt.expression)
    }

    override fun visitFunctionStmt(stmt: Function) {
        val function = LoxFunction(stmt, environment)
        environment.define(stmt.name.lexeme, function)
    }

    override fun visitIfStmt(stmt: If) {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch)
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch)
        }
    }

    override fun visitPrintStmt(stmt: Print) {
        val value = evaluate(stmt.expression)
        println(stringify(value))
    }

    override fun visitReturnStmt(stmt: Return) {
        val value = if (stmt.value == null) LoxNil else evaluate(stmt.value)
        throw ReturnValue(value)
    }

    override fun visitVarStmt(stmt: Var) {
        var value: LoxValue = LoxNil
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer)
        }

        environment.define(stmt.name.lexeme, value)
    }

    override fun visitWhileStmt(stmt: While) {
        while (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.body)
        }
    }


    internal fun executeBlock(statements: List<Stmt>, environment: Environment) {
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

    internal fun resolve(expr: Expr, depth: Int) {
        locals.put(expr, depth)
    }


    private fun evaluate(expr: Expr): LoxValue {
        return expr.accept(this)
    }

    private fun execute(stmt: Stmt) {
        stmt.accept(this)
    }

    private fun lookupVariable(name: Token, expr: Expr): LoxValue {
        val distance = locals[expr]
        return if (distance != null) {
            environment.getAt(distance, name.lexeme)
        } else {
            globals.get(name)
        }
    }

    private fun stringify(value: LoxValue): String {
        if (value is LoxDouble) {
            return value.toString().removeSuffix(".0")
        }
        return value.toString()
    }

    private fun isEqual(left: LoxValue, right: LoxValue): Boolean {
        return Objects.equals(left, right)
    }

    private fun isTruthy(value: LoxValue): Boolean {
        if (value is LoxNil) return false
        if (value is LoxBool) return value.value
        return true
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

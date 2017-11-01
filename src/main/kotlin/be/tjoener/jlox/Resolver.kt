package be.tjoener.jlox

import be.tjoener.jlox.ast.*
import be.tjoener.jlox.ast.Function
import be.tjoener.jlox.parser.Token
import java.util.*

class Resolver(val interpreter: Interpreter) : Expr.Visitor<Unit>, Stmt.Visitor<Unit> {

    private val scopes: Stack<MutableMap<String, Boolean>> = Stack()

    override fun visitBlockStmt(stmt: Block) {
        beginScope()
        resolve(stmt.statements)
        endScope()
    }

    override fun visitVarStmt(stmt: Var) {
        declare(stmt.name)
        if (stmt.initializer != null) {
            resolve(stmt.initializer)
        }
        define(stmt.name)
    }

    override fun visitVariableExpr(expr: Variable) {
        if (!scopes.isEmpty() && scopes.peek()[expr.name.lexeme] == false) {
            JLox.error(expr.name, "Cannot read local variable in its own initializer")
        }

        resolveLocal(expr, expr.name)
    }

    override fun visitAssignExpr(expr: Assign) {
        resolve(expr.value)
        resolveLocal(expr, expr.name)
    }

    override fun visitFunctionStmt(stmt: Function) {
        declare(stmt.name)
        define(stmt.name)

        resolveFunction(stmt)
    }

    private fun resolveFunction(function: Function) {
        beginScope()
        for (parameter in function.parameters) {
            declare(parameter)
            define(parameter)
        }
        resolve(function.body)
        endScope()
    }


    override fun visitExpressionStmt(stmt: Expression) {
        resolve(stmt.expression)
    }

    override fun visitIfStmt(stmt: If) {
        resolve(stmt.condition)
        resolve(stmt.thenBranch)
        if (stmt.elseBranch != null) {
            resolve(stmt.elseBranch)
        }
    }

    override fun visitPrintStmt(stmt: Print) {
        resolve(stmt.expression)
    }

    override fun visitReturnStmt(stmt: Return) {
        if (stmt.value != null) {
            resolve(stmt.value)
        }
    }

    override fun visitWhileStmt(stmt: While) {
        resolve(stmt.condition)
        resolve(stmt.body)
    }


    override fun visitBinaryExpr(expr: Binary) {
        resolve(expr.left)
        resolve(expr.right)
    }

    override fun visitCallExpr(expr: Call) {
        resolve(expr.callee)
        for (argument in expr.arguments) {
            resolve(argument)
        }
    }

    override fun visitGroupingExpr(expr: Grouping) {
        resolve(expr.expression)
    }

    override fun visitLiteralExpr(expr: Literal) {
    }

    override fun visitLogicalExpr(expr: Logical) {
        resolve(expr.left)
        resolve(expr.right)
    }

    override fun visitUnaryExpr(expr: Unary) {
        resolve(expr.right)
    }


    private fun resolveLocal(expr: Expr, name: Token) {
        for (i in scopes.size - 1 downTo 0) {
            if (scopes[i].containsKey(name.lexeme)) {
                interpreter.resolve(expr, scopes.size - 1 - i)
                return
            }
        }
    }

    private fun resolve(statements: List<Stmt>) {
        for (statement in statements) {
            resolve(statement)
        }
    }

    private fun resolve(stmt: Stmt) {
        stmt.accept(this)
    }

    private fun resolve(expr: Expr) {
        expr.accept(this)
    }


    private fun beginScope() {
        scopes.push(mutableMapOf())
    }

    private fun endScope() {
        scopes.pop()
    }


    private fun declare(name: Token) {
        if (scopes.isEmpty()) return
        scopes.peek().put(name.lexeme, false)
    }

    private fun define(name: Token) {
        if (scopes.isEmpty()) return
        scopes.peek().put(name.lexeme, true)
    }


}

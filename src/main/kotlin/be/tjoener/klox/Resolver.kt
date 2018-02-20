package be.tjoener.klox

import be.tjoener.klox.ast.*
import be.tjoener.klox.ast.Function
import be.tjoener.klox.ast.Set
import be.tjoener.klox.parser.Token
import java.util.*

class Resolver(private val interpreter: Interpreter) : Expr.Visitor<Unit>, Stmt.Visitor<Unit> {

    private enum class FunctionType {
        NONE, FUNCTION, INITIALIZER, METHOD
    }

    private enum class ClassType {
        NONE, CLASS
    }

    private val scopes = Stack<MutableMap<String, Boolean>>()
    private var currentFunction = FunctionType.NONE
    private var currentClass = ClassType.NONE

    fun resolve(statements: List<Stmt>) {
        for (statement in statements) {
            resolve(statement)
        }
    }


    override fun visitBlockStmt(stmt: Block) {
        beginScope()
        resolve(stmt.statements)
        endScope()
    }

    override fun visitClassStmt(stmt: Class) {
        declare(stmt.name)
        define(stmt.name)

        val enclosingClass = currentClass
        currentClass = ClassType.CLASS

        if (stmt.superclass != null) {
            resolve(stmt.superclass)
        }

        beginScope()

        scopes.peek()["this"] = true
        for (method in stmt.methods) {
            val declaration =
                if (method.name.lexeme == "init") FunctionType.INITIALIZER
                else FunctionType.METHOD

            resolveFunction(method, declaration)
        }

        endScope()
        currentClass = enclosingClass
    }

    override fun visitExpressionStmt(stmt: Expression) {
        resolve(stmt.expression)
    }

    override fun visitGetExpr(expr: Get) {
        resolve(expr.obj)
    }

    override fun visitFunctionStmt(stmt: Function) {
        declare(stmt.name)
        define(stmt.name)

        resolveFunction(stmt, FunctionType.FUNCTION)
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
        if (currentFunction == FunctionType.NONE) {
            KLox.error(stmt.keyword, "Cannot return from top-level code.")
        }

        if (stmt.value != null) {
            if (currentFunction == FunctionType.INITIALIZER) {
                KLox.error(stmt.keyword, "Cannot return a value from an initializer")
            }
            resolve(stmt.value)
        }
    }

    override fun visitVarStmt(stmt: Var) {
        declare(stmt.name)
        if (stmt.initializer != null) {
            resolve(stmt.initializer)
        }
        define(stmt.name)
    }

    override fun visitWhileStmt(stmt: While) {
        resolve(stmt.condition)
        resolve(stmt.body)
    }


    override fun visitAssignExpr(expr: Assign) {
        resolve(expr.value)
        resolveLocal(expr, expr.name)
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

    override fun visitSetExpr(expr: Set) {
        resolve(expr.value)
        resolve(expr.obj)
    }

    override fun visitThisExpr(expr: This) {
        if (currentClass == ClassType.NONE) {
            KLox.error(expr.keyword, "Cannot use 'this' outside of a class")
        }
        resolveLocal(expr, expr.keyword)
    }

    override fun visitUnaryExpr(expr: Unary) {
        resolve(expr.right)
    }

    override fun visitVariableExpr(expr: Variable) {
        if (!scopes.isEmpty() && scopes.peek()[expr.name.lexeme] == false) {
            KLox.error(expr.name, "Cannot read local variable in its own initializer")
        }

        resolveLocal(expr, expr.name)
    }


    private fun resolve(stmt: Stmt) {
        stmt.accept(this)
    }

    private fun resolve(expr: Expr) {
        expr.accept(this)
    }

    private fun resolveLocal(expr: Expr, name: Token) {
        for (i in scopes.size - 1 downTo 0) {
            if (scopes[i].containsKey(name.lexeme)) {
                interpreter.resolve(expr, scopes.size - 1 - i)
                return
            }
        }
    }

    private fun resolveFunction(function: Function, type: FunctionType) {
        val enclosingFunction = currentFunction
        currentFunction = type
        beginScope()
        for (parameter in function.parameters) {
            declare(parameter)
            define(parameter)
        }
        resolve(function.body)
        endScope()
        currentFunction = enclosingFunction
    }


    private fun beginScope() {
        scopes.push(mutableMapOf())
    }

    private fun endScope() {
        scopes.pop()
    }

    private fun declare(name: Token) {
        if (scopes.isEmpty()) return
        val scope = scopes.peek()
        if (scope.containsKey(name.lexeme)) {
            KLox.error(name, "Variable with this name already declared in this scope")
        }
        scope[name.lexeme] = false
    }

    private fun define(name: Token) {
        if (scopes.isEmpty()) return
        scopes.peek()[name.lexeme] = true
    }

}

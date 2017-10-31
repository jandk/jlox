package be.tjoener.jlox.ast

import be.tjoener.jlox.parser.Token

sealed class Stmt {
    abstract fun <R> accept(visitor: Visitor<R>): R

    interface Visitor<out R> {
        fun visitExpressionStmt(stmt: Expression): R
        fun visitPrintStmt(stmt: Print): R
    }
}

class Expression(val expression: Expr) : Stmt() {
    override fun <R> accept(visitor: Visitor<R>): R {
        return visitor.visitExpressionStmt(this)
    }
}

class Print(val expression: Expr) : Stmt() {
    override fun <R> accept(visitor: Visitor<R>): R {
        return visitor.visitPrintStmt(this)
    }
}

package be.tjoener.jlox.ast

import be.tjoener.jlox.parser.Token

sealed class Expr {
    abstract fun <R> accept(visitor: Visitor<R>): R
}

class Unary(operator: Token, right: Expr) : Expr() {
    override fun <R> accept(visitor: Visitor<R>): R {
        return visitor.visitUnaryExpr(this)
    }
}

class Binary(left: Expr, operator: Token, right: Expr) : Expr() {
    override fun <R> accept(visitor: Visitor<R>): R {
        return visitor.visitBinaryExpr(this)
    }
}

class Literal(value: Any) : Expr() {
    override fun <R> accept(visitor: Visitor<R>): R {
        return visitor.visitLiteralExpr(this)
    }
}

class Grouping(expression: Expr) : Expr() {
    override fun <R> accept(visitor: Visitor<R>): R {
        return visitor.visitGroupingExpr(this)
    }
}

interface Visitor<out R> {
    fun visitUnaryExpr(expr: Unary): R
    fun visitBinaryExpr(expr: Binary): R
    fun visitLiteralExpr(expr: Literal): R
    fun visitGroupingExpr(expr: Grouping): R
}

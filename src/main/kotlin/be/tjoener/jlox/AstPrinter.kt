package be.tjoener.jlox

import be.tjoener.jlox.ast.*

class AstPrinter : Visitor<String> {
    fun print(expr: Expr): String {
        return expr.accept(this)
    }

    override fun visitUnaryExpr(expr: Unary): String {
        return parenthesize(expr.operator.lexeme, expr.right)
    }

    override fun visitBinaryExpr(expr: Binary): String {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right)
    }

    override fun visitLiteralExpr(expr: Literal): String {
        if (expr.value == null) return "nil"
        return expr.value.toString()
    }

    override fun visitGroupingExpr(expr: Grouping): String {
        return parenthesize("group", expr.expression)
    }

    private fun parenthesize(name: String, vararg exprs: Expr): String {
        val builder = StringBuilder().append('(').append(name)
        for (expr in exprs) {
            builder.append(' ').append(expr.accept(this))
        }
        return builder.append(')').toString()
    }

}

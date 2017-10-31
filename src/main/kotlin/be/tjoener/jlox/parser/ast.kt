package be.tjoener.jlox.parser

sealed class Expr
class Binary(val left: Expr, operator: Token, right: Expr) : Expr()
class Unary(val operator: Token, expr: Expr) : Expr()
class Grouping(val expression: Expr) : Expr()
class Literal(val value: Any) : Expr()

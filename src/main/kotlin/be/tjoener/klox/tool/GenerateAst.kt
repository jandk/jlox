package be.tjoener.klox.tool

import java.io.File
import java.io.PrintWriter
import kotlin.system.exitProcess


object GenerateAst {

    @JvmStatic
    fun main(args: Array<String>) {
        if (args.size != 1) {
            System.err.println("Usage: generate_ast <output directory>")
            exitProcess(1)
        }
        val outputDir = args[0]

        defineAst(
            outputDir, "Expr", listOf(
                "Assign   : Token name, Expr value",
                "Binary   : Expr left, Token operator, Expr right",
                "Call     : Expr callee, Token paren, List<Expr> arguments",
                "Get      : Expr obj, Token name",
                "Grouping : Expr expression",
                "Literal  : Any? value",
                "Logical  : Expr left, Token operator, Expr right",
                "Set      : Expr obj, Token name, Expr value",
                "Super    : Token keyword, Token method",
                "This     : Token keyword",
                "Unary    : Token operator, Expr right",
                "Variable : Token name"
            )
        )

        defineAst(
            outputDir, "Stmt", listOf(
                "Block      : List<Stmt> statements",
                "Class      : Token name, Expr? superclass, List<Function> methods",
                "Expression : Expr expression",
                "Function   : Token name, List<Token> parameters, List<Stmt> body",
                "If         : Expr condition, Stmt thenBranch, Stmt? elseBranch",
                "Print      : Expr expression",
                "Return     : Token keyword, Expr? value",
                "Var        : Token name, Expr? initializer",
                "While      : Expr condition, Stmt body"
            )
        )
    }

    private fun defineAst(outputDir: String, baseName: String, types: List<String>) {
        File("$outputDir/$baseName.kt").printWriter().use {
            it.println("package be.tjoener.klox.ast")
            it.println()
            it.println("import be.tjoener.klox.parser.Token")
            it.println()
            it.println("sealed class $baseName {")
            it.println("    abstract fun <R> accept(visitor: Visitor<R>): R")

            it.println()
            defineVisitor(it, baseName, types)

            it.println("}")

            // The AST classes
            for (type in types) {
                val className = type.split(':')[0].trim()
                val fields = type.split(':')[1].trim()
                defineType(it, baseName, className, fields)
            }
        }
    }

    private fun defineType(writer: PrintWriter, baseName: String, className: String, fieldList: String) {
        // Store parameters in fields.
        val fields = fieldList
            .split(", ")
            .joinToString { "val " + it.split(' ')[1] + ": " + it.split(' ')[0] }

        writer.println()
        writer.println("class $className($fields) : $baseName() {")

        // Visitor pattern.
        writer.println("    override fun <R> accept(visitor: Visitor<R>): R {")
        writer.println("        return visitor.visit$className$baseName(this)")
        writer.println("    }")

        writer.println("}")
    }

    private fun defineVisitor(writer: PrintWriter, baseName: String, types: List<String>) {
        writer.println("    interface Visitor<out R> {")

        types
            .map { it.split(':')[0].trim() }
            .forEach { writer.println("        fun visit$it$baseName(${baseName.toLowerCase()}: $it): R") }

        writer.println("    }")
    }

}

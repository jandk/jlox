package be.tjoener.jlox.tool

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

        defineAst(outputDir, "Expr", listOf(
            "Assign   : Token name, Expr value",
            "Unary    : Token operator, Expr right",
            "Binary   : Expr left, Token operator, Expr right",
            "Literal  : Any? value",
            "Grouping : Expr expression",
            "Variable : Token name"
        ))

        defineAst(outputDir, "Stmt", listOf(
            "Expression : Expr expression",
            "Print      : Expr expression",
            "Var        : Token name, Expr? initializer"
        ))
    }

    private fun defineAst(outputDir: String, baseName: String, types: List<String>) {
        File("$outputDir/$baseName.kt").printWriter().use {
            it.println("package be.tjoener.jlox.ast")
            it.println()
            it.println("import be.tjoener.jlox.parser.Token")
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

        for (type in types) {
            val typeName = type.split(':')[0].trim()
            writer.println("        fun visit$typeName$baseName(${baseName.toLowerCase()}: $typeName): R")
        }

        writer.println("    }")
    }

}

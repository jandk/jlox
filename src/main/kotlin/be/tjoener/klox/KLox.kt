package be.tjoener.klox

import be.tjoener.klox.parser.Parser
import be.tjoener.klox.parser.Scanner
import be.tjoener.klox.parser.Token
import be.tjoener.klox.parser.TokenType
import java.io.File
import kotlin.system.exitProcess

object KLox {

    private val interpreter: Interpreter = Interpreter()

    @JvmStatic
    fun main(args: Array<String>) {
        if (args.size > 1) {
            println("Usage: klox [script]")
        } else if (args.size == 1) {
            runFile(args[0])
        } else {
            runPrompt()
        }
    }

    private fun runFile(path: String) {
        val source = File(path).readText()
        run(source)

        // Indicate an error in the exit code
        if (hadError) exitProcess(65)
        if (hadRuntimeError) exitProcess(70)
    }

    private fun runPrompt() {
        val reader = System.`in`.bufferedReader()
        while (true) {
            print("> ")
            run(reader.readLine())
            hadError = false
        }
    }

    private fun run(source: String) {
        val scanner = Scanner(source)
        val tokens = scanner.scanTokens()
        val parser = Parser(tokens)
        val statements = parser.parse()

        // Stop if there was a syntax error
        if (hadError) return

        val resolver = Resolver(interpreter)
        resolver.resolve(statements)

        // Stop if there was a resolution error
        if (hadError) return

        interpreter.interpret(statements)
    }

    fun error(line: Int, message: String) {
        report(line, "", message)
    }

    fun error(token: Token, message: String) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message)
        } else {
            report(token.line, " at '${token.lexeme}'", message)
        }
    }

    var hadRuntimeError = false
    internal fun runtimeError(error: RuntimeError) {
        System.err.println("${error.message}\n[line ${error.token.line}]")
        hadRuntimeError = true
    }


    var hadError = false
    fun report(line: Int, where: String, message: String) {
        System.err.println("[line $line] Error$where: $message")
        hadError = true
    }

}

package be.tjoener.jlox

import be.tjoener.jlox.parser.Scanner
import be.tjoener.jlox.parser.Token
import be.tjoener.jlox.parser.TokenType
import java.io.File
import kotlin.system.exitProcess

object JLox {

    @JvmStatic
    fun main(args: Array<String>) {
        if (args.size > 1) {
            println("Usage: jlox [script]")
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
        if (hadError) {
            exitProcess(65)
        }
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

        // For now, just print the tokens
        for (token in tokens) {
            println(token)
        }
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

    var hadError = false
    fun report(line: Int, where: String, message: String) {
        System.err.println("[line $line] Error$where: $message")
        hadError = true
    }

}

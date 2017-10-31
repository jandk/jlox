package be.tjoener.jlox

import be.tjoener.jlox.parser.Token

internal class RuntimeError(val token: Token, message: String) : RuntimeException(message)

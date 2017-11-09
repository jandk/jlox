package be.tjoener.klox

import be.tjoener.klox.parser.Token

internal class RuntimeError(val token: Token, message: String) : RuntimeException(message)

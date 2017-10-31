package be.tjoener.jlox

import be.tjoener.jlox.parser.Token

class RuntimeError(val token: Token, message: String) : RuntimeException(message)

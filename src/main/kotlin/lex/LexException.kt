package fr.ancyr.jcc.lex

import fr.ancyr.jcc.commons.Position

class LexException(message: String, private val position: Position) : RuntimeException(message) {
  override fun toString(): String {
    return "LexException: $message at line ${position.line}, column ${position.column}"
  }
}
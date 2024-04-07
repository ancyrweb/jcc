package fr.ancyr.jcc.ast.sem

import fr.ancyr.jcc.lex.TokenType

enum class SymbolType {
  INT,
  FLOAT,
  CHAR,
  SHORT,
  LONG,
  DOUBLE;

  companion object {
    fun fromString(type: TokenType): SymbolType {
      return when (type) {
        TokenType.TYPE_INT -> INT
        TokenType.TYPE_FLOAT -> FLOAT
        TokenType.TYPE_DOUBLE -> DOUBLE
        TokenType.TYPE_CHAR -> CHAR
        TokenType.TYPE_SHORT -> SHORT
        TokenType.TYPE_LONG -> LONG
        else -> throw IllegalArgumentException("Unknown type: $type")
      }
    }
  }
}
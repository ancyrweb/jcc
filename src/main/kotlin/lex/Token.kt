package fr.ancyr.jcc.lex

import fr.ancyr.jcc.commons.Position

data class Token(
  val type: TokenType,
  val position: Position,
  val value: Any?
) {
  companion object {
    var varTypes = setOf(
      TokenType.TYPE_INT,
      TokenType.TYPE_CHAR,
      TokenType.TYPE_VOID,
      TokenType.TYPE_FLOAT,
      TokenType.TYPE_DOUBLE,
      TokenType.TYPE_LONG,
      TokenType.TYPE_SHORT,
      TokenType.TYPE_LONG_LONG,
      TokenType.TYPE_LONG_DOUBLE
    )
  }

  fun isVarType(): Boolean {
    return varTypes.contains(type)
  }

  fun isTokenType(type: TokenType): Boolean {
    return this.type == type
  }

  fun isAnyType(vararg types: TokenType): Boolean {
    return types.contains(this.type)
  }

  fun isConstant(): Boolean {
    return type == TokenType.LIT_INT || type == TokenType.LIT_STRING
  }

  fun isIdentifier(): Boolean {
    return type == TokenType.IDENTIFIER
  }

  fun asString(): String {
    if (value is String) {
      return value
    }

    throw Exception("Token value is not a string")
  }

  override fun toString(): String {
    return "Token($type, $value)"
  }
}
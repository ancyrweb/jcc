package fr.ancyr.jcc.lex

import fr.ancyr.jcc.commons.Position

data class Token(
  public val type: TokenType,
  public val position: Position,
  public val value: Any
) {
  companion object {
    var types = setOf(
      "int", "float", "double", "char", "void"
    )

    var keywords = setOf(
      "if",
      "else",
      "while",
      "for",
      "return",
      "break",
      "continue",
      "int",
      "float",
      "double",
      "char",
      "void",
      "struct",
      "typedef",
      "enum",
      "union",
      "const",
      "static",
      "extern",
      "register",
      "auto",
      "volatile",
      "inline",
      "restrict",
      "sizeof",
      "alignof",
      "offsetof",
      "asm",
      "typeof",
      "typeof__",
      "va_arg",
      "va_list",
      "va_start",
      "va_end",
      "va_copy",
      "va_arg_pack",
      "va_arg_pack_len",
      "va_arg_pack_ptr",
      "va_arg_pack_end",
      "va_arg_pack_next",
    )
  }

  fun isType(): Boolean {
    return types.contains(value)
  }

  fun isSymbol(value: Char): Boolean {
    return type == TokenType.SYMBOL && this.value == value
  }

  fun isConstant(): Boolean {
    return type == TokenType.NUMBER || type == TokenType.STRING
  }

  fun isNumber(): Boolean {
    return type == TokenType.NUMBER
  }

  fun isIdentifier(): Boolean {
    return type == TokenType.IDENTIFIER
  }

  fun isOperator(value: String): Boolean {
    return type == TokenType.OPERATOR && this.value == value
  }

  fun isAnyOperator(vararg values: String): Boolean {
    return type == TokenType.OPERATOR && values.contains(this.value)
  }

  fun isKeyword(value: String): Boolean {
    return type == TokenType.KEYWORD && this.value == value
  }

  fun isAnyKeyword(vararg values: String): Boolean {
    return type == TokenType.KEYWORD && values.contains(this.value)
  }

  fun asString(): String {
    if (value is String) {
      return value
    }

    throw Exception("Token value is not a string")
  }
}
package fr.ancyr.jcc.lex

enum class TokenType {
  NUMBER,
  KEYWORD,
  OPERATOR,
  SYMBOL,
  IDENTIFIER,
  COMMENT;

  public val line: Int = 0
  public val column: Int = 0
  public val value: Any = 0
}
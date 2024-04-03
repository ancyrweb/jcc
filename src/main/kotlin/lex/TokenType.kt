package fr.ancyr.jcc.lex

enum class TokenType {
  IDENTIFIER,
  COMMENT,

  TYPE_INT,
  TYPE_CHAR,
  TYPE_VOID,
  TYPE_FLOAT,
  TYPE_DOUBLE,
  TYPE_LONG,
  TYPE_SHORT,

  TYPE_LONG_LONG, // TODO
  TYPE_LONG_DOUBLE, // TODO

  LIT_INT,
  LIT_STRING,
  LIT_REAL, // TODO
  LIT_CHAR, // TODO

  KEYWORD_IF,
  KEYWORD_ELSE,
  KEYWORD_WHILE,
  KEYWORD_FOR,
  KEYWORD_DO,
  KEYWORD_SWITCH,
  KEYWORD_CASE,
  KEYWORD_DEFAULT,
  KEYWORD_BREAK,
  KEYWORD_CONTINUE,
  KEYWORD_RETURN,
  KEYWORD_GOTO,
  KEYWORD_TYPEDEF,
  KEYWORD_EXTERN,
  KEYWORD_STATIC,
  KEYWORD_AUTO,
  KEYWORD_REGISTER,
  KEYWORD_CONST,
  KEYWORD_VOLATILE,
  KEYWORD_RESTRICT,
  KEYWORD_SIZEOF,

  OP_PLUS,
  OP_PLUS_PLUS,
  OP_PLUS_EQUAL,
  OP_MINUS,
  OP_MINUS_MINUS,
  OP_MINUS_EQUAL,
  OP_MUL,
  OP_MUL_EQUAL,
  OP_DIV,
  OP_DIV_EQUAL,
  OP_MOD,
  OP_MOD_EQUAL,
  OP_AND,
  OP_AND_EQUAL,
  OP_OR,
  OP_OR_EQUAL,
  OP_XOR,
  OP_XOR_EQUAL,
  OP_NOT,
  OP_NOT_EQUAL,
  OP_EQUAL,
  OP_EQUAL_EQUAL,
  OP_LESS,
  OP_LESS_EQUAL,
  OP_GREATER,
  OP_GREATER_EQUAL,
  OP_LEFT_SHIFT,
  OP_LEFT_SHIFT_EQUAL,
  OP_RIGHT_SHIFT,
  OP_RIGHT_SHIFT_EQUAL,
  OP_BITWISE_AND,
  OP_BITWISE_INCLUSIVE_OR,
  OP_BITWISE_EXCLUSIVE_OR,

  SYMBOL_LEFT_PAREN,
  SYMBOL_RIGHT_PAREN,
  SYMBOL_LEFT_BRACE,
  SYMBOL_RIGHT_BRACE,
  SYMBOL_LEFT_BRACKET,
  SYMBOL_RIGHT_BRACKET,
  SYMBOL_COMMA,
  SYMBOL_SEMICOLON,
  SYMBOL_COLON,
  SYMBOL_DOT,
  SYMBOL_ARROW,
  SYMBOL_QUESTION;

  public val line: Int = 0
  public val column: Int = 0
  public val value: Any = 0
}
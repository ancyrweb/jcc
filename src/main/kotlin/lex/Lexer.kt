package fr.ancyr.jcc.lex

import fr.ancyr.jcc.commons.Position

class Lexer(private val content: StringBuffer) {
  private val tokens = mutableListOf<Token>()
  private var cur = 0
  private var currentPosition = Position()
  private var startPosition = Position()


  private fun peek() = content[cur]
  private fun advance(offset: Int = 1): Char {
    val c = content[cur]

    currentPosition.advance(offset)
    cur += offset

    return c
  }

  private fun eof() = cur >= content.length
  private fun longPeek(offset: Int): Char? {
    if (cur + offset >= content.length) {
      return null
    }

    return content[cur + offset]
  }

  private fun createToken(type: TokenType, value: Any? = null): Token {
    return Token(type, startPosition.clone(), value)
  }

  private fun addToken(token: Token) {
    tokens.add(token)
  }

  private fun nextLine() {
    currentPosition.nextLine()
  }

  fun parse(): List<Token> {
    while (!eof()) {
      startPosition.copyFrom(currentPosition)

      when (peek()) {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
          addToken(anyNumber())
        }

        'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' -> {
          addToken(parseKeywordOrIdentifier())
        }

        '"' -> {
          addToken(parseString())
        }

        '=', '+', '-', '%', '!', '<', '>', '&', '|', '^', '~', '?', '.' -> {
          addToken(parseOperator())
        }

        '(', ')', '{', '}', '[', ']', ',', ';', ':' -> {
          addToken(parseSymbol())
        }

        '/' -> {
          if (longPeek(1) == '/') {
            addToken(singleLineComment())
          } else if (longPeek(1) == '*') {
            addToken(multiLineComment())
          } else {
            addToken(parseOperator())
          }
        }

        '*' -> {
          addToken(parseOperator())
        }

        '\n' -> {
          advance()
          nextLine()
        }

        ' ' -> {
          advance()
        }

        else -> {
          println("Unrecognized : " + peek().code)
        }
      }
    }

    return tokens
  }

  private fun anyNumber(): Token {
    if (peek() == '0') {
      if (longPeek(1) == 'x') {
        return parseHexNumber()
      } else if (longPeek(1) == 'b') {
        return parseBinaryNumber()
      }
    }

    var number: Long = 0L
    while (!eof() && peek().isDigit()) {
      number = number * 10 + (advance() - '0')
    }

    return createToken(TokenType.LIT_INT, number)
  }

  private fun parseHexNumber(): Token {
    val str = StringBuilder()
    advance(2) // Skip 0x

    while (!eof() && (peek().isDigit() || (peek() in 'a'..'f') || (peek() in 'A'..'F'))) {
      str.append(advance())
    }

    return createToken(TokenType.LIT_INT, str.toString().toLong(16))
  }

  private fun parseBinaryNumber(): Token {
    val str = StringBuilder()
    advance(2) // Skip 0b

    while (!eof() && (peek() == '0' || peek() == '1')) {
      str.append(advance())
    }

    return createToken(TokenType.LIT_INT, str.toString().toLong(2))
  }

  private fun singleLineComment(): Token {
    advance(2) // Skip //

    val str = StringBuilder()
    while (!eof() && peek() != '\n') {
      str.append(advance())
    }


    return createToken(TokenType.COMMENT, str.toString().trim())
  }

  private fun multiLineComment(): Token {
    advance(2) // Skip /*
    val str = StringBuilder()

    while (!eof() && (peek() != '*' || longPeek(1) != '/')) {
      if (peek() == '\n') {
        nextLine()
      }

      str.append(advance())
    }

    advance(2) // Skip */

    if (str.last() == '\n') {
      str.deleteAt(str.length - 1)
    }

    return createToken(TokenType.COMMENT, str.toString().trim())
  }

  private fun parseKeywordOrIdentifier(): Token {
    val str = StringBuilder()
    while (!eof() && (peek().isLetterOrDigit() || peek() == '_')) {
      str.append(advance())
    }

    return when (val value = str.toString().lowercase()) {
      "int" -> createToken(TokenType.TYPE_INT)
      "float" -> createToken(TokenType.TYPE_FLOAT)
      "double" -> createToken(TokenType.TYPE_DOUBLE)
      "char" -> createToken(TokenType.TYPE_CHAR)
      "void" -> createToken(TokenType.TYPE_VOID)
      "long" -> createToken(TokenType.TYPE_LONG)
      "short" -> createToken(TokenType.TYPE_SHORT)
      "if" -> createToken(TokenType.KEYWORD_IF)
      "else" -> createToken(TokenType.KEYWORD_ELSE)
      "while" -> createToken(TokenType.KEYWORD_WHILE)
      "for" -> createToken(TokenType.KEYWORD_FOR)
      "do" -> createToken(TokenType.KEYWORD_DO)
      "switch" -> createToken(TokenType.KEYWORD_SWITCH)
      "case" -> createToken(TokenType.KEYWORD_CASE)
      "default" -> createToken(TokenType.KEYWORD_DEFAULT)
      "break" -> createToken(TokenType.KEYWORD_BREAK)
      "continue" -> createToken(TokenType.KEYWORD_CONTINUE)
      "return" -> createToken(TokenType.KEYWORD_RETURN)
      "goto" -> createToken(TokenType.KEYWORD_GOTO)
      "typedef" -> createToken(TokenType.KEYWORD_TYPEDEF)
      "extern" -> createToken(TokenType.KEYWORD_EXTERN)
      "static" -> createToken(TokenType.KEYWORD_STATIC)
      "auto" -> createToken(TokenType.KEYWORD_AUTO)
      "register" -> createToken(TokenType.KEYWORD_REGISTER)
      "const" -> createToken(TokenType.KEYWORD_CONST)
      "volatile" -> createToken(TokenType.KEYWORD_VOLATILE)
      "restrict" -> createToken(TokenType.KEYWORD_RESTRICT)
      "sizeof" -> createToken(TokenType.KEYWORD_SIZEOF)
      "signed" -> createToken(TokenType.KEYWORD_SIGNED)
      "unsigned" -> createToken(TokenType.KEYWORD_UNSIGNED)
      else -> createToken(TokenType.IDENTIFIER, value)
    }
  }

  private fun parseString(): Token {
    advance() // Skip "
    val str = StringBuilder()

    while (!eof() && peek() != '"') {
      if (peek() == '\n') {
        throw LexException("Unexpected newline in string", currentPosition)
      }

      str.append(advance())
    }

    advance() // Skip "

    return createToken(TokenType.LIT_STRING, str.toString())
  }

  private fun parseSymbol(): Token {
    val tokenType = when (peek()) {
      '(' -> TokenType.SYMBOL_LEFT_PAREN
      ')' -> TokenType.SYMBOL_RIGHT_PAREN
      '{' -> TokenType.SYMBOL_LEFT_BRACE
      '}' -> TokenType.SYMBOL_RIGHT_BRACE
      '[' -> TokenType.SYMBOL_LEFT_BRACKET
      ']' -> TokenType.SYMBOL_RIGHT_BRACKET
      ',' -> TokenType.SYMBOL_COMMA
      ';' -> TokenType.SYMBOL_SEMICOLON
      ':' -> TokenType.SYMBOL_COLON
      else -> throw LexException("Unrecognized symbol", currentPosition)
    }

    advance()
    return createToken(tokenType, null)
  }

  private fun makeOp(type: TokenType, len: Int): Token {
    val token = createToken(type, null)
    advance(len)
    return token
  }

  private fun parseOperator(): Token {
    if (peek() == '+') {
      if (longPeek(1) == '+') {
        return makeOp(TokenType.OP_PLUS_PLUS, 2)
      } else if (longPeek(1) == '=') {
        return makeOp(TokenType.OP_PLUS_EQUAL, 2)
      }

      return makeOp(TokenType.OP_PLUS, 1)
    } else if (peek() == '-') {
      if (longPeek(1) == '-') {
        return makeOp(TokenType.OP_MINUS_MINUS, 2)
      } else if (longPeek(1) == '=') {
        return makeOp(TokenType.OP_MINUS_EQUAL, 2)
      }

      return makeOp(TokenType.OP_MINUS, 1)
    } else if (peek() == '*') {
      if (longPeek(1) == '=') {
        return makeOp(TokenType.OP_MUL_EQUAL, 2)
      }

      return makeOp(TokenType.OP_MUL, 1)
    } else if (peek() == '/') {
      if (longPeek(1) == '=') {
        return makeOp(TokenType.OP_DIV_EQUAL, 2)
      }

      return makeOp(TokenType.OP_DIV, 1)
    } else if (peek() == '%') {
      if (longPeek(1) == '=') {
        return makeOp(TokenType.OP_MOD_EQUAL, 2)
      }

      return makeOp(TokenType.OP_MOD, 1)
    } else if (peek() == '&') {
      if (longPeek(1) == '&') {
        return makeOp(TokenType.OP_AND, 2)
      } else if (longPeek(1) == '=') {
        return makeOp(TokenType.OP_AND_EQUAL, 2)
      }

      return makeOp(TokenType.OP_BITWISE_AND, 1)
    } else if (peek() == '|') {
      if (longPeek(1) == '|') {
        return makeOp(TokenType.OP_OR, 2)
      } else if (longPeek(1) == '=') {
        return makeOp(TokenType.OP_OR_EQUAL, 2)
      }

      return makeOp(TokenType.OP_BITWISE_INCLUSIVE_OR, 1)
    } else if (peek() == '^') {
      if (longPeek(1) == '=') {
        return makeOp(TokenType.OP_XOR_EQUAL, 2)
      }

      return makeOp(TokenType.OP_BITWISE_EXCLUSIVE_OR, 1)
    } else if (peek() == '<') {
      if (longPeek(1) == '<') {
        return makeOp(TokenType.OP_LEFT_SHIFT, 2)
      } else if (longPeek(1) == '=') {
        return makeOp(TokenType.OP_LESS_EQUAL, 2)
      }

      return makeOp(TokenType.OP_LESS, 1)
    } else if (peek() == '>') {
      if (longPeek(1) == '>') {
        return makeOp(TokenType.OP_RIGHT_SHIFT, 2)
      } else if (longPeek(1) == '=') {
        return makeOp(TokenType.OP_GREATER_EQUAL, 2)
      }

      return makeOp(TokenType.OP_GREATER, 1)
    } else if (peek() == '=') {
      if (longPeek(1) == '=') {
        return makeOp(TokenType.OP_EQUAL_EQUAL, 2)
      }

      return makeOp(TokenType.OP_EQUAL, 1)
    } else if (peek() == '!') {
      if (longPeek(1) == '=') {
        return makeOp(TokenType.OP_NOT_EQUAL, 2)
      }

      return makeOp(TokenType.OP_NOT, 1)
    }

    throw LexException("Unrecognized operator", currentPosition)
  }
}
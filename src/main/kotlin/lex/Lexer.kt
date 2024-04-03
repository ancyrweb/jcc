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

  private fun createToken(type: TokenType, value: Any): Token {
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
          addToken(createToken(TokenType.SYMBOL, advance()))
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

    return createToken(TokenType.NUMBER, number)
  }

  private fun parseHexNumber(): Token {
    val str = StringBuilder()
    advance(2) // Skip 0x

    while (!eof() && (peek().isDigit() || (peek() in 'a'..'f') || (peek() in 'A'..'F'))) {
      str.append(advance())
    }

    return createToken(TokenType.NUMBER, str.toString().toLong(16))
  }

  private fun parseBinaryNumber(): Token {
    val str = StringBuilder()
    advance(2) // Skip 0b

    while (!eof() && (peek() == '0' || peek() == '1')) {
      str.append(advance())
    }

    return createToken(TokenType.NUMBER, str.toString().toLong(2))
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

    val value = str.toString().lowercase();

    return if (Token.keywords.contains(value)) {
      createToken(TokenType.KEYWORD, value)
    } else {
      createToken(TokenType.IDENTIFIER, value)
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

    return createToken(TokenType.STRING, str.toString())
  }

  private fun makeOp(value: String): Token {
    val token = createToken(TokenType.OPERATOR, value)
    advance(value.length)
    return token
  }

  private fun parseOperator(): Token {
    if (peek() == '+') {
      if (longPeek(1) == '+') {
        return makeOp("++")
      } else if (longPeek(1) == '=') {
        return makeOp("+=")
      } else {
        return makeOp("+")
      }
    } else if (peek() == '-') {
      if (longPeek(1) == '-') {
        return makeOp("--")
      } else if (longPeek(1) == '=') {
        return makeOp("-=")
      } else {
        return makeOp("-")
      }
    } else if (peek() == '*') {
      if (longPeek(1) == '=') {
        return makeOp("*=")
      } else {
        return makeOp("*")
      }
    } else if (peek() == '/') {
      if (longPeek(1) == '=') {
        return makeOp("/=")
      } else {
        return makeOp("/")
      }
    } else if (peek() == '%') {
      if (longPeek(1) == '=') {
        return makeOp("%=")
      } else {
        return makeOp("%")
      }
    } else if (peek() == '!') {
      if (longPeek(1) == '=') {
        return makeOp("!=")
      } else {
        return makeOp("!")
      }
    } else if (peek() == '<') {
      if (longPeek(1) == '=') {
        return makeOp("<=")
      } else if (longPeek(1) == '<') {
        if (longPeek(2) == '=') {
          return makeOp("<<=")
        } else {
          return makeOp("<<")
        }
      } else {
        return makeOp("<")
      }
    } else if (peek() == '>') {
      if (longPeek(1) == '=') {
        return makeOp(">=")
      } else if (longPeek(1) == '>') {
        if (longPeek(2) == '=') {
          return makeOp(">>=")
        } else {
          return makeOp(">>")
        }
      } else {
        return makeOp(">")
      }
    } else if (peek() == '&') {
      if (longPeek(1) == '&') {
        return makeOp("&&")
      } else if (longPeek(1) == '=') {
        return makeOp("&=")
      } else {
        return makeOp("&")
      }
    } else if (peek() == '|') {
      if (longPeek(1) == '|') {
        return makeOp("||")
      } else if (longPeek(1) == '=') {
        return makeOp("|=")
      } else {
        return makeOp("|")
      }
    } else if (peek() == '^') {
      if (longPeek(1) == '=') {
        return makeOp("^=")
      } else {
        return makeOp("^")
      }
    } else if (peek() == '~') {
      return makeOp("~")
    } else if (peek() == '?') {
      return makeOp("?")
    } else if (peek() == '.') {
      return makeOp(".")
    } else if (peek() == '=') {
      if (longPeek(1) == '=') {
        return makeOp("==")
      } else {
        return makeOp("=")
      }
    }

    throw LexException("Unrecognized operator", currentPosition)
  }
}
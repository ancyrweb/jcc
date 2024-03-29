package fr.ancyr.jcc.ast

import fr.ancyr.jcc.lex.Token
import fr.ancyr.jcc.lex.TokenType

/**
 * () ] . -> and ++/-- (postfix) (15)
 * ++/-- + - ! ~ (type) * & sizeof  (note: right to left) (14)
 * * / % (13)
 * + - (12)
 * << >> (11)
 * < <= > >= (10)
 * == != (9)
 * & (8)
 * ^ (7)
 * | (6)
 * && (5)
 * || (4)
 * ?: (note: right to left) (3)
 * = += -= *= /= %= &= ^= |= <<= >>= (note: right to left) (2)
 * , (1)
 */
class Parser (private val tokens: List<Token>) {
  private var index = 0

  private fun peek() = tokens[index]
  private fun eof() = index >= tokens.size

  private fun consumeSymbol(symbol: String) {
    if (peek().type != TokenType.SYMBOL || peek().value != symbol) {
      throw Exception("Expected symbol $symbol at ${peek().position}")
    }

    index++
  }


  fun parse(): Any? {
    while (!eof()) {
      val token = peek()
      when (token) {
        else -> {
          parseExpr()
        }
      }
    }
    return null
  }

  private fun parseExpr(): Expr {
    return expr1()
  }

  private fun expr1(): Expr = expr2()
  private fun expr2(): Expr = expr3()
  private fun expr3(): Expr = expr4()
  private fun expr4(): Expr = expr5()
  private fun expr5(): Expr = expr6()
  private fun expr6(): Expr = expr7()
  private fun expr7(): Expr = expr8()
  private fun expr8(): Expr = expr9()
  private fun expr9(): Expr = expr10()
  private fun expr10(): Expr = expr11()
  private fun expr11(): Expr = expr12()
  private fun expr12(): Expr = expr13()
  private fun expr13(): Expr = expr14()
  private fun expr14(): Expr {

  }

  private fun expr15(): Expr {
    val p = peek()

    if (peek().isSymbol("(")) {
      consumeSymbol("(")
      val expr = parseExpr()
      consumeSymbol(")")
      return GroupExpr(expr)
    } else if (peek().isSymbol("[")) {
      consumeSymbol("[")
      val value = exprTerminal()
      consumeSymbol("]")
      return ArrayAccessExpr(value)
    }

    val expr = exprTerminal()

    // Postfix increments
    if (peek().isOperator("--") || peek().isOperator("++")) {
      if (expr is IdentifierExpr) {
        val operator = peek()
        index++
        return PostfixOp(expr, operator)
      }

      throw RuntimeException("Expected identifier before postfix operator")
    }

    return expr
  }

  private fun exprTerminal(): Expr {
    if (peek().isConstant()) {
      val token = peek()
      index++
      return ConstantExpr(token)
    } else if (peek().isIdentifier()) {
      val token = peek()
      index++
      return IdentifierExpr(token)
    }

    throw RuntimeException("Unexpected token: ${peek()}")
  }

  private fun matchIdentifier(): IdentifierExpr {
    val token = peek()
    if (!token.isIdentifier()) {
      throw Exception("Expected identifier at ${token.position}")
    }

    index++
    return IdentifierExpr(token)
  }

}
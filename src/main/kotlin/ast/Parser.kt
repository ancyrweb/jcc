package fr.ancyr.jcc.ast

import fr.ancyr.jcc.lex.Token
import fr.ancyr.jcc.lex.TokenType

/**
 * () ] . -> and ++/-- (postfix) (15)
 * ++/-- (prefix) + - ! ~ (type) * & sizeof  (note: right to left) (14)
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

  private var nodes = mutableListOf<Node>()

  private fun peek() = tokens[index]
  private fun eof() = index >= tokens.size

  private fun consumeSymbol(symbol: Char) {
    if (peek().type != TokenType.SYMBOL || peek().value != symbol) {
      throw Exception("Expected symbol $symbol at ${peek().position}, got ${peek()}")
    }

    index++
  }

  private fun advance(): Token {
    val operator = peek()
    index++

    return operator
  }

  fun parse(): List<Node> {
    while (!eof()) {
      val token = peek()
      when (token) {
        else -> {
          val node = parseExprStatement()
          nodes.add(node)
        }
      }
    }
    return nodes
  }

  private fun parseExprStatement(): Node {
    val expr = expr()
    consumeSymbol(';')
    return ExprStatement(expr)
  }

  private fun expr(): Expr = exprCommas()
  private fun exprCommas(): Expr = exprAssignments() // TODO
  private fun exprAssignments(): Expr = exprTernary() // TODO
  private fun exprTernary(): Expr = exprLogicalOr() // TODO
  private fun exprLogicalOr(): Expr {
    var left = exprLogicalAnd()
    while (peek().isOperator("||")) {
      val operator = advance()
      val right = exprLogicalAnd()
      left = BinOp(operator, left, right)
    }

    return left
  }
  private fun exprLogicalAnd(): Expr {
    var left = exprBitwiseInclusiveOr()
    while (peek().isOperator("&&")) {
      val operator = advance()
      val right = exprBitwiseInclusiveOr()
      left = BinOp(operator, left, right)
    }

    return left
  }

  private fun exprBitwiseInclusiveOr(): Expr {
    var left = exprBitwiseExclusiveOr()
    while (peek().isOperator("|")) {
      val operator = advance()
      val right = exprBitwiseExclusiveOr()
      left = BinOp(operator, left, right)
    }

    return left
  }

  private fun exprBitwiseExclusiveOr(): Expr {
    var left = exprBitwiseAnd()
    while (peek().isOperator("^")) {
      val operator = advance()
      val right = exprBitwiseAnd()
      left = BinOp(operator, left, right)
    }

    return left
  }

  private fun exprBitwiseAnd(): Expr {
    var left = exprRelationalEquality()
    while (peek().isOperator("&")) {
      val operator = advance()
      val right = exprRelationalEquality()
      left = BinOp(operator, left, right)
    }

    return left
  }

  private fun exprRelationalEquality(): Expr {
    var left = exprRelationalComparison()
    while (peek().isOperator("==", "!=")) {
      val operator = advance()
      val right = exprRelationalComparison()
      left = BinOp(operator, left, right)
    }

    return left
  }

  private fun exprRelationalComparison(): Expr {
    var left = exprBitwiseShifts()
    while (peek().isOperator("<", ">", "<=", ">=")) {
      val operator = advance()
      val right = exprBitwiseShifts()
      left = BinOp(operator, left, right)
    }

    return left
  }

  private fun exprBitwiseShifts(): Expr {
    var left = exprTerms()
    while (peek().isOperator("<<", ">>")) {
      val operator = advance()
      val right = exprTerms()
      left = BinOp(operator, left, right)
    }

    return left
  }
  private fun exprTerms(): Expr {
    var left = exprFactors()
    while (peek().isOperator("-", "+")) {
      val operator = advance()
      val right = exprFactors()
      left = BinOp(operator, left, right)
    }

    return left
  }
  private fun exprFactors(): Expr {
    var left = exprUnaryOrPrefix()
    while (peek().isOperator("*", "/", "%")) {
      val operator = advance()
      val right = exprUnaryOrPrefix()
      left = BinOp(operator, left, right)
    }

    return left
  }

  private fun exprUnaryOrPrefix(): Expr {
    if (peek().isOperator("--", "++")) {
      val operator = advance()

      if (peek().isIdentifier()) {
        val expr = matchIdentifier()
        return PrefixOp(expr, operator)
      }

        throw RuntimeException("Expected identifier after prefix operator")
    } else if (peek().isOperator("-") || peek().isOperator("+")) {
      val operator = advance()
      val expr = exprGroupArrayOrPostfix()

      return UnaryOp(expr, operator)
    }

    return exprGroupArrayOrPostfix()
  }

  private fun exprGroupArrayOrPostfix(): Expr {
    if (peek().isSymbol('(')) {
      consumeSymbol('(')
      val expr = expr()
      consumeSymbol(')')
      return GroupExpr(expr)
    } else if (peek().isSymbol('[')) {
      consumeSymbol('[')
      val value = exprTerminal()
      consumeSymbol(']')
      return ArrayAccessExpr(value)
    }

    val expr = exprTerminal()

    // Postfix increments
    if (peek().isOperator("--", "++")) {
      if (expr is IdentifierExpr) {
        val operator = advance()
        return PostfixOp(expr, operator)
      }

      throw RuntimeException("Expected identifier before postfix operator")
    }

    return expr
  }

  private fun exprTerminal(): Expr {
    if (peek().isConstant()) {
      val token = advance()
      return ConstantExpr(token)
    } else if (peek().isIdentifier()) {
      val token = advance()
      return IdentifierExpr(token)
    }

    throw RuntimeException("Unexpected token: ${peek()} at ${peek().position}")
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
package fr.ancyr.jcc.ast

import fr.ancyr.jcc.ast.nodes.*
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
      nodes.add(parseNextNode())
    }

    return nodes
  }

  private fun parseNextNode(): Node {
    return if (peek().isType()) {
      parseDeclaration()
    } else if (peek().isKeyword("if")) {
      parseIfStatement()
    } else if (peek().isKeyword("while")) {
      parseWhileStatement()
    } else if (peek().isKeyword("for")) {
      parseForLoopStatement()
    } else if (peek().isAnyKeyword("break", "continue", "return", "goto")) {
      parseKeywordDeclaration()
    } else {
      parseExprStatement()
    }
  }

  private fun parseDeclaration(): Node {
    val type = advance()
    val identifier = matchIdentifier()

    if (peek().isOperator("=")) {
      advance()
      val expr = expr()
      consumeSymbol(';')
      return DeclarationNode(type, identifier, expr)
    }

    consumeSymbol(';')
    return DeclarationNode(type, identifier, null)
  }



  private fun parseIfStatement(): Node {
    advance()

    val ifStmt = IfNode(condExpr(), blockNode(), null, null)
    var currentBlock = ifStmt

    while (!eof() && peek().isKeyword("else")) {
      advance();
      if (peek().isKeyword("if")) {
        advance()
        currentBlock.elseIf = IfNode(condExpr(), blockNode(), null, null)
        currentBlock = currentBlock.elseIf!!
      } else {
        currentBlock.elseBlock = blockNode()
      }
    }

    return ifStmt
  }

  private fun parseWhileStatement(): Node {
    advance()

    val condition = condExpr()
    val block = blockNode()

    return WhileNode(condition, block)
  }

  private fun parseForLoopStatement(): Node {
    advance()
    consumeSymbol('(')

    val init = parseNextNode()
    consumeSymbol(';')

    val condition = expr()
    consumeSymbol(';')

    val increment = parseNextNode()
    consumeSymbol(')')

    val block = blockNode()

    return ForLoopNode(init, condition, increment, block)
  }

  private fun parseKeywordDeclaration(): Node {
    val keyword = advance()

    if (peek().isSymbol(';')) {
      advance()
      return KeywordDeclarationNode(keyword)
    }

    val expr = expr()
    consumeSymbol(';')
    return KeywordDeclarationNode(keyword, expr)
  }

  private fun condExpr(): Expr {
    consumeSymbol('(')
    val expr = expr()
    consumeSymbol(')')
    return expr
  }

  private fun blockNode(): BlockNode {
    consumeSymbol('{')
    val body = mutableListOf<Node>()
    while (!peek().isSymbol('}')) {
      body.add(parseNextNode())
    }
    consumeSymbol('}')
    return BlockNode(body)
  }


  private fun parseExprStatement(): Node {
    val expr = expr()
    consumeSymbol(';')
    return ExprStatement(expr)
  }

  private fun expr(): Expr = exprCommas()

  private fun exprCommas(): Expr = exprAssignments() // TODO

  private fun exprAssignments(): Expr {
    val left = exprTernary()
    if (peek().isAnyOperator("=", "+=", "-=", "*=", "/=", "%=", "&=", "^=", "|=", "<<=", ">>=")) {
      if (!isLValue(left)) {
        throw RuntimeException("Expected lvalue before assignment operator")
      }

      val operator = advance()
      val right = exprTernary()
      return AssignOpExpr(operator, left, right)
    }

    return left
  }

  private fun exprTernary(): Expr = exprLogicalOr() // TODO

  private fun exprLogicalOr(): Expr {
    var left = exprLogicalAnd()
    while (peek().isOperator("||")) {
      val operator = advance()
      val right = exprLogicalAnd()
      left = BinOpExpr(operator, left, right)
    }

    return left
  }

  private fun exprLogicalAnd(): Expr {
    var left = exprBitwiseInclusiveOr()
    while (peek().isOperator("&&")) {
      val operator = advance()
      val right = exprBitwiseInclusiveOr()
      left = BinOpExpr(operator, left, right)
    }

    return left
  }

  private fun exprBitwiseInclusiveOr(): Expr {
    var left = exprBitwiseExclusiveOr()
    while (peek().isOperator("|")) {
      val operator = advance()
      val right = exprBitwiseExclusiveOr()
      left = BinOpExpr(operator, left, right)
    }

    return left
  }

  private fun exprBitwiseExclusiveOr(): Expr {
    var left = exprBitwiseAnd()
    while (peek().isOperator("^")) {
      val operator = advance()
      val right = exprBitwiseAnd()
      left = BinOpExpr(operator, left, right)
    }

    return left
  }

  private fun exprBitwiseAnd(): Expr {
    var left = exprRelationalEquality()
    while (peek().isOperator("&")) {
      val operator = advance()
      val right = exprRelationalEquality()
      left = BinOpExpr(operator, left, right)
    }

    return left
  }

  private fun exprRelationalEquality(): Expr {
    var left = exprRelationalComparison()
    while (peek().isAnyOperator("==", "!=")) {
      val operator = advance()
      val right = exprRelationalComparison()
      left = BinOpExpr(operator, left, right)
    }

    return left
  }

  private fun exprRelationalComparison(): Expr {
    var left = exprBitwiseShifts()
    while (peek().isAnyOperator("<", ">", "<=", ">=")) {
      val operator = advance()
      val right = exprBitwiseShifts()
      left = BinOpExpr(operator, left, right)
    }

    return left
  }

  private fun exprBitwiseShifts(): Expr {
    var left = exprTerms()
    while (peek().isAnyOperator("<<", ">>")) {
      val operator = advance()
      val right = exprTerms()
      left = BinOpExpr(operator, left, right)
    }

    return left
  }

  private fun exprTerms(): Expr {
    var left = exprFactors()
    while (peek().isAnyOperator("-", "+")) {
      val operator = advance()
      val right = exprFactors()
      left = BinOpExpr(operator, left, right)
    }

    return left
  }

  private fun exprFactors(): Expr {
    var left = exprUnaryOrPrefix()
    while (peek().isAnyOperator("*", "/", "%")) {
      val operator = advance()
      val right = exprUnaryOrPrefix()
      left = BinOpExpr(operator, left, right)
    }

    return left
  }

  private fun exprUnaryOrPrefix(): Expr {
    if (peek().isAnyOperator("--", "++")) {
      val operator = advance()

      if (peek().isIdentifier()) {
        val identifier = matchRValue()
        return PrefixOpExpr(identifier, operator)
      }

        throw RuntimeException("Expected identifier after prefix operator")
    } else if (peek().isOperator("-") || peek().isOperator("+")) {
      val operator = advance()
      val expr = exprGroupArrayOrPostfix()

      return UnaryOpExpr(expr, operator)
    }

    return exprGroupArrayOrPostfix()
  }

  private fun exprGroupArrayOrPostfix(): Expr {
    if (peek().isSymbol('(')) {
      consumeSymbol('(')
      val expr = expr()
      consumeSymbol(')')
      return GroupExpr(expr)
    }

    val expr = exprTerminal()

    // Postfix increments
    if (peek().isAnyOperator("--", "++")) {
      if (expr is IdentifierExpr || expr is ArrayAccessExpr) {
        val operator = advance()
        return PostfixOpExpr(expr, operator)
      }

      throw RuntimeException("Expected identifier before postfix operator")
    }

    return expr
  }

  private fun exprTerminal(): Expr {
    if (peek().isConstant()) {
      val token = advance()
      return ConstantExpr(token)
    }

    return matchRValue()
  }

  private fun matchRValue(): Expr {
    if (!peek().isIdentifier()) {
      throw Exception("Expected identifier at ${peek().position}")
    }

    val token = advance()
    if (peek().isSymbol('[')) {
      consumeSymbol('[')
      val index = expr()
      consumeSymbol(']')
      return ArrayAccessExpr(token, index)
    }

    return IdentifierExpr(token)
  }

  private fun matchIdentifier(): Token {
    if (!peek().isIdentifier()) {
      throw Exception("Expected identifier at ${peek().position}")
    }

    val token = advance()
    return token
  }

  private fun isLValue(expr: Expr): Boolean {
    return expr is IdentifierExpr || expr is ArrayAccessExpr
  }

}
package fr.ancyr.jcc.ast

import fr.ancyr.jcc.ast.nodes.*
import fr.ancyr.jcc.ast.sem.Scope
import fr.ancyr.jcc.ast.sem.Symbol
import fr.ancyr.jcc.ast.sem.SymbolType
import fr.ancyr.jcc.lex.Token
import fr.ancyr.jcc.lex.TokenType

class Parser(private val tokens: List<Token>) {
  private var index = 0
  private var nodes = mutableListOf<Node>()
  private var currentScope = Scope()

  private fun peek() = tokens[index]
  private fun eof() = index >= tokens.size

  private fun consume(type: TokenType): Token {
    if (peek().type != type) {
      throw Exception("Expected token of type $type at ${peek().position}, got ${peek()}")
    }

    return advance()

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

    return if (peek().isBeginningOfTypeDeclaration()) {
      parseVarOrFunctionDeclaration()
    } else if (peek().isTokenType(TokenType.KEYWORD_IF)) {
      parseIfStatement()
    } else if (peek().isTokenType(TokenType.KEYWORD_WHILE)) {
      parseWhileStatement()
    } else if (peek().isTokenType(TokenType.KEYWORD_FOR)) {
      parseForLoopStatement()
    } else if (peek().isTokenType(TokenType.KEYWORD_RETURN)) {
      parseReturnStatement()
    } else {
      parseExprStatement()
    }
  }

  private fun parseVarOrFunctionDeclaration(): Node {
    val typedSymbol = matchTypedSymbol()
    println(typedSymbol)

    var expr: Expr? = null

    if (peek().isTokenType(TokenType.SYMBOL_LEFT_PAREN)) {
      // Function declaration
      addSymbolToScope(
        Symbol.Function(
          typedSymbol.identifier,
          typedSymbol.type,
          typedSymbol.signed,
          typedSymbol.pointer
        )
      )

      consume(TokenType.SYMBOL_LEFT_PAREN)
      val params = parseFormalParameters()
      var block: BlockNode? = null
      if (peek().isTokenType(TokenType.SYMBOL_LEFT_BRACE)) {
        block = blockNode()
      }

      return FunctionNode(typedSymbol, params, block)
    }

    if (peek().isTokenType(TokenType.OP_EQUAL)) {
      advance()
      expr = expr()
    }

    consume(TokenType.SYMBOL_SEMICOLON)

    addSymbolToScope(
      Symbol.Variable(
        typedSymbol.identifier,
        typedSymbol.type,
        typedSymbol.signed,
        typedSymbol.pointer
      )
    )
    return VariableDeclarationNode(typedSymbol, expr)
  }

  private fun parseFormalParameters(): List<TypedSymbol> {
    val params = mutableListOf<TypedSymbol>()

    while (!peek().isTokenType(TokenType.SYMBOL_RIGHT_PAREN)) {
      val typedSymbol = matchTypedSymbol()

      params.add(typedSymbol)

      if (peek().isTokenType(TokenType.SYMBOL_COMMA)) {
        advance()
      }
    }

    consume(TokenType.SYMBOL_RIGHT_PAREN)
    return params
  }

  private fun parseIfStatement(): Node {
    advance()

    val ifStmt = IfNode(condExpr(), blockNode(), null, null)
    var currentBlock = ifStmt

    while (!eof() && peek().isTokenType(TokenType.KEYWORD_ELSE)) {
      advance();
      if (peek().isTokenType(TokenType.KEYWORD_IF)) {
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
    consume(TokenType.SYMBOL_LEFT_PAREN)

    val init = parseNextNode()
    consume(TokenType.SYMBOL_SEMICOLON)

    val condition = expr()
    consume(TokenType.SYMBOL_SEMICOLON)

    val increment = parseNextNode()
    consume(TokenType.SYMBOL_RIGHT_PAREN)

    val block = blockNode()

    return ForLoopNode(init, condition, increment, block)
  }

  private fun parseReturnStatement(): Node {
    advance()

    if (peek().isTokenType(TokenType.SYMBOL_SEMICOLON)) {
      advance()
      return ReturnNode()
    }

    val expr = expr()
    consume(TokenType.SYMBOL_SEMICOLON)
    return ReturnNode(expr)
  }

  private fun condExpr(): Expr {
    consume(TokenType.SYMBOL_LEFT_PAREN)
    val expr = expr()
    consume(TokenType.SYMBOL_RIGHT_PAREN)
    return expr
  }

  private fun blockNode(): BlockNode {
    // Create lexical scope
    val scope = Scope(currentScope)
    currentScope = scope

    consume(TokenType.SYMBOL_LEFT_BRACE)

    val body = mutableListOf<Node>()
    while (!peek().isTokenType(TokenType.SYMBOL_RIGHT_BRACE)) {
      body.add(parseNextNode())
    }

    consume(TokenType.SYMBOL_RIGHT_BRACE)

    // Restore scope
    currentScope = scope.parent!!

    return BlockNode(body, scope)
  }


  private fun parseExprStatement(): Node {
    val expr = expr()
    consume(TokenType.SYMBOL_SEMICOLON)
    return ExprStatement(expr)
  }

  private fun expr(): Expr = exprCommas()

  private fun exprCommas(): Expr = exprAssignments() // TODO

  private fun exprAssignments(): Expr {
    val left = exprTernary()

    if (peek().isAnyType(
        TokenType.OP_EQUAL,
        TokenType.OP_PLUS_EQUAL,
        TokenType.OP_MINUS_EQUAL,
        TokenType.OP_MUL_EQUAL,
        TokenType.OP_DIV_EQUAL,
        TokenType.OP_MOD_EQUAL,
        TokenType.OP_AND_EQUAL,
        TokenType.OP_XOR_EQUAL,
        TokenType.OP_OR_EQUAL,
        TokenType.OP_LEFT_SHIFT_EQUAL,
        TokenType.OP_RIGHT_SHIFT_EQUAL
      )
    ) {
      if (!isLValue(left)) {
        throw RuntimeException("Expected lvalue before assignment operator")
      }

      val operator = advance()
      val right = exprTernary()
      return AssignOpExpr(operator.type, left, right)
    }

    return left
  }

  private fun exprTernary(): Expr = exprLogicalOr() // TODO

  private fun exprLogicalOr(): Expr {
    var left = exprLogicalAnd()
    while (peek().isTokenType(TokenType.OP_OR)) {
      advance()
      val right = exprLogicalAnd()
      left = OrExpr(left, right)
    }

    return left
  }

  private fun exprLogicalAnd(): Expr {
    var left = exprBitwiseInclusiveOr()
    while (peek().isTokenType(TokenType.OP_AND)) {
      advance()
      val right = exprBitwiseInclusiveOr()
      left = AndExpr(left, right)
    }

    return left
  }

  private fun exprBitwiseInclusiveOr(): Expr {
    var left = exprBitwiseExclusiveOr()
    while (peek().isTokenType(TokenType.OP_BITWISE_INCLUSIVE_OR)) {
      val operator = advance()
      val right = exprBitwiseExclusiveOr()
      left = BinOpExpr(operator.type, left, right)
    }

    return left
  }

  private fun exprBitwiseExclusiveOr(): Expr {
    var left = exprBitwiseAnd()
    while (peek().isTokenType(TokenType.OP_BITWISE_EXCLUSIVE_OR)) {
      val operator = advance()
      val right = exprBitwiseAnd()
      left = BinOpExpr(operator.type, left, right)
    }

    return left
  }

  private fun exprBitwiseAnd(): Expr {
    var left = exprRelationalEquality()
    while (peek().isTokenType(TokenType.OP_BITWISE_AND)) {
      val operator = advance()
      val right = exprRelationalEquality()
      left = BinOpExpr(operator.type, left, right)
    }

    return left
  }

  private fun exprRelationalEquality(): Expr {
    var left = exprRelationalComparison()
    while (peek().isAnyType(TokenType.OP_EQUAL_EQUAL, TokenType.OP_NOT_EQUAL)) {
      val operator = advance()
      val right = exprRelationalComparison()
      left = ComparisonExpr(operator.type, left, right)
    }

    return left
  }

  private fun exprRelationalComparison(): Expr {
    var left = exprBitwiseShifts()
    while (peek().isAnyType(
        TokenType.OP_LESS,
        TokenType.OP_LESS_EQUAL,
        TokenType.OP_GREATER,
        TokenType.OP_GREATER_EQUAL
      )
    ) {
      val operator = advance()
      val right = exprBitwiseShifts()
      left = ComparisonExpr(operator.type, left, right)
    }

    return left
  }

  private fun exprBitwiseShifts(): Expr {
    var left = exprTerms()
    while (peek().isAnyType(
        TokenType.OP_LEFT_SHIFT,
        TokenType.OP_RIGHT_SHIFT
      )
    ) {
      val operator = advance()
      val right = exprTerms()
      left = BinOpExpr(operator.type, left, right)
    }

    return left
  }

  private fun exprTerms(): Expr {
    var left = exprFactors()
    while (peek().isAnyType(TokenType.OP_PLUS, TokenType.OP_MINUS)) {
      val operator = advance()
      val right = exprFactors()
      left = BinOpExpr(operator.type, left, right)
    }

    return left
  }

  private fun exprFactors(): Expr {
    var left = exprUnaryOrPrefix()
    while (peek().isAnyType(
        TokenType.OP_MUL,
        TokenType.OP_DIV,
        TokenType.OP_MOD
      )
    ) {
      val operator = advance()
      val right = exprUnaryOrPrefix()
      left = BinOpExpr(operator.type, left, right)
    }

    return left
  }

  private fun exprUnaryOrPrefix(): Expr {
    if (peek().isAnyType(TokenType.OP_PLUS_PLUS, TokenType.OP_MINUS_MINUS)) {
      val operator = advance()

      if (peek().isIdentifier()) {
        val identifier = matchRValue()
        return PrefixOpExpr(identifier, operator.type)
      }

      throw RuntimeException("Expected identifier after prefix operator")
    } else if (peek().isAnyType(TokenType.OP_PLUS, TokenType.OP_MINUS)) {
      val operator = advance()
      val expr = exprGroupArrayOrPostfix()

      return UnaryOpExpr(expr, operator.type)
    }

    return exprGroupArrayOrPostfix()
  }

  private fun exprGroupArrayOrPostfix(): Expr {
    if (peek().isTokenType(TokenType.SYMBOL_LEFT_PAREN)) {
      consume(TokenType.SYMBOL_LEFT_PAREN)
      val expr = expr()
      consume(TokenType.SYMBOL_RIGHT_PAREN)
      return GroupExpr(expr)
    }

    val expr = exprTerminal()

    // Postfix increments
    if (peek().isAnyType(TokenType.OP_PLUS_PLUS, TokenType.OP_MINUS_MINUS)) {
      if (expr is IdentifierExpr || expr is ArrayAccessExpr) {
        val operator = advance()
        return PostfixOpExpr(expr, operator.type)
      }

      throw RuntimeException("Expected identifier before postfix operator")
    }

    return expr
  }

  private fun exprTerminal(): Expr {
    if (peek().isConstant()) {
      val token = advance()
      return ConstantExpr(token.value)
    }

    return matchRValue()
  }

  private fun matchRValue(): Expr {
    if (!peek().isIdentifier()) {
      throw Exception("Expected identifier at ${peek().position}")
    }

    val token = advance()
    if (peek().isTokenType(TokenType.SYMBOL_LEFT_BRACKET)) {
      consume(TokenType.SYMBOL_LEFT_BRACKET)
      val index = expr()
      consume(TokenType.SYMBOL_RIGHT_BRACKET)
      return ArrayAccessExpr(token.asString(), index)
    }

    return IdentifierExpr(token.asString())
  }

  private fun matchIdentifier(): Token {
    if (!peek().isTokenType(TokenType.IDENTIFIER)) {
      throw Exception("Expected identifier at ${peek().position}")
    }

    val token = advance()
    return token
  }

  private fun matchTypedSymbol(): TypedSymbol {
    if (!peek().isBeginningOfTypeDeclaration()) {
      throw Exception("Expected type at ${peek().position}")
    }

    var signed = true
    var pointer = false

    if (peek().isSignedOrUnsigned()) {
      signed = peek().isTokenType(TokenType.KEYWORD_SIGNED)
      advance()
    }

    val varType = advance()
    if (!varType.isVarType()) {
      throw Exception("Expected type at ${varType.position}")
    }

    if (peek().isTokenType(TokenType.OP_MUL)) {
      advance()
      pointer = true
    }

    val identifier = matchIdentifier()

    return TypedSymbol(
      identifier = identifier.asString(),
      SymbolType.fromString(varType.type),
      signed,
      pointer
    )
  }

  private fun isLValue(expr: Expr): Boolean {
    return expr is IdentifierExpr || expr is ArrayAccessExpr
  }

  private fun addSymbolToScope(symbol: Symbol) {
    currentScope.addSymbol(
      symbol
    )
  }
}
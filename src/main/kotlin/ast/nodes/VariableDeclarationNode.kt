package fr.ancyr.jcc.ast.nodes

import fr.ancyr.jcc.lex.TokenType

data class VariableDeclarationNode(
  val type: TokenType,
  val identifier: String,
  val value: Expr?
) : Node()
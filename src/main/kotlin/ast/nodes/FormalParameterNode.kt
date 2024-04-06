package fr.ancyr.jcc.ast.nodes

import fr.ancyr.jcc.lex.TokenType

data class FormalParameterNode(
  val type: TokenType,
  val name: String
) : Node()


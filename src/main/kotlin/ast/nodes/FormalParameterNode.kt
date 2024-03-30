package fr.ancyr.jcc.ast.nodes

import fr.ancyr.jcc.lex.Token

data class FormalParameterNode(
  val type: Token,
  val identifier: Token
) : Node()


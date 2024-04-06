package fr.ancyr.jcc.ast.nodes

import fr.ancyr.jcc.lex.TokenType

data class FunctionNode(
  val returnType: TokenType,
  val identifier: String,
  val parameters: List<FormalParameterNode>,
  val block: BlockNode? = null // If null, it's a function declaration
) : Node()
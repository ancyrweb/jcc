package fr.ancyr.jcc.ast.nodes

import fr.ancyr.jcc.lex.Token

data class FunctionNode(
   val type: Token,
   val identifier: Token,
   val parameters: List<FormalParameterNode>,
   val block: BlockNode? = null // If null, it's a function declaration
) : Node()
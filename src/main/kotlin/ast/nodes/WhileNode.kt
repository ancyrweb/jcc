package fr.ancyr.jcc.ast.nodes

data class WhileNode (
  val condition: Expr,
  val block: BlockNode,
) : Node()
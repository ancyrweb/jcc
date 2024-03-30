package fr.ancyr.jcc.ast.nodes

data class IfNode (
  val condition: Expr,
  val thenBlock: BlockNode,
  var elseIf: IfNode?,
  var elseBlock: BlockNode?,
) : Node()
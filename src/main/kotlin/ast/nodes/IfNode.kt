package fr.ancyr.jcc.ast.nodes

data class IfNode(
  val condition: Expr,
  val thenBlock: BlockNode,
  var elseIf: IfNode?,
  var elseBlock: BlockNode?,
) : Node() {
  init {
    if (elseIf != null && elseBlock != null) {
      throw IllegalArgumentException("Either elseIf or elseBlock can be set, not both")
    }
  }
}
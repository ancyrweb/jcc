package fr.ancyr.jcc.ast.nodes

import fr.ancyr.jcc.lex.Token

data class BinOpExpr(
  val op: Token,
  val left: Node,
  val right: Node
) : Expr() {
  override fun toString(): String {
    return "BinOp(" + op.value + ", $left, $right)"
  }
}
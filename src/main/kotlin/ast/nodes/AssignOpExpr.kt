package fr.ancyr.jcc.ast.nodes

import fr.ancyr.jcc.lex.Token

data class AssignOpExpr(
  val op: Token,
  val left: Node,
  val right: Expr
) : Expr() {
  override fun toString(): String {
    return "AssignOp(" + op.value + ", $left, $right)"
  }
}
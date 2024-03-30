package fr.ancyr.jcc.ast.nodes

import fr.ancyr.jcc.lex.Token

data class UnaryOpExpr(
  val node: Expr,
  val op: Token
) : Expr() {
  override fun toString(): String {
    return "UnaryOp(" + op.value + ", $node)"
  }
}
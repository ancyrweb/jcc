package fr.ancyr.jcc.ast

import fr.ancyr.jcc.lex.Token

data class UnaryOp(
  val node: Expr,
  val op: Token
) : Expr() {
  override fun toString(): String {
    return "UnaryOp(" + op.value + ", $node)"
  }
}
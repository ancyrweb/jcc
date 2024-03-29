package fr.ancyr.jcc.ast

import fr.ancyr.jcc.lex.Token

data class BinOp(
  val op: Token,
  val left: Node,
  val right: Node
) : Expr() {
  override fun toString(): String {
    return "BinOp(" + op.value + ", $left, $right)"
  }
}
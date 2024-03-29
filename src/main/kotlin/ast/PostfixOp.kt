package fr.ancyr.jcc.ast

import fr.ancyr.jcc.lex.Token

data class PostfixOp(
  val expr: Expr,
  val op: Token
) : Expr() {
  override fun toString(): String {
    return "PostfixOp(${op.value} $expr)"
  }
}
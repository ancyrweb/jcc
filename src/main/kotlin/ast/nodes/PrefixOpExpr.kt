package fr.ancyr.jcc.ast.nodes

import fr.ancyr.jcc.lex.Token

data class PrefixOpExpr(
  val expr: Expr,
  val op: Token
) : Expr() {
  override fun toString(): String {
    return "PrefixOp(${op.value} $expr)"
  }
}
package fr.ancyr.jcc.ast.nodes

import fr.ancyr.jcc.lex.Token

data class PostfixOpExpr(
  val expr: Expr,
  val op: Token
) : Expr() {
  override fun toString(): String {
    return "PostfixOp(${op.value} $expr)"
  }
}
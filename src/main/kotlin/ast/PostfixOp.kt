package fr.ancyr.jcc.ast

import fr.ancyr.jcc.lex.Token

data class PostfixOp(
  val identifier: IdentifierExpr,
  val op: Token
) : Expr() {
  override fun toString(): String {
    return "PostfixOp(${op.value} $identifier)"
  }
}
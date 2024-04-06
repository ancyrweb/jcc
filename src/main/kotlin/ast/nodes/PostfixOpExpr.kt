package fr.ancyr.jcc.ast.nodes

import fr.ancyr.jcc.lex.TokenType

data class PostfixOpExpr(
  val expr: Expr,
  val op: TokenType
) : Expr() {
  override fun toString(): String {
    return "PostfixOp(${op} $expr)"
  }
}
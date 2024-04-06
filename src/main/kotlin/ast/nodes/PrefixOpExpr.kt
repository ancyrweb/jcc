package fr.ancyr.jcc.ast.nodes

import fr.ancyr.jcc.lex.TokenType

data class PrefixOpExpr(
  val expr: Expr,
  val op: TokenType
) : Expr() {
  override fun toString(): String {
    return "PrefixOp(${op} $expr)"
  }
}
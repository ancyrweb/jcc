package fr.ancyr.jcc.ast.nodes

import fr.ancyr.jcc.lex.TokenType

data class ComparisonExpr(
  val op: TokenType,
  val left: Expr,
  val right: Expr
) : Expr() {
  override fun toString(): String {
    return "ComparisonExpr($op, $left, $right)"
  }
}
package fr.ancyr.jcc.ast.nodes

import fr.ancyr.jcc.lex.TokenType

data class BinOpExpr(
  val op: TokenType,
  val left: Expr,
  val right: Expr
) : Expr() {
  override fun toString(): String {
    return "BinOp($op, $left, $right)"
  }
}
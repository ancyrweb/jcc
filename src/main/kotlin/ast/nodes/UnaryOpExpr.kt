package fr.ancyr.jcc.ast.nodes

import fr.ancyr.jcc.lex.TokenType

data class UnaryOpExpr(
  val expr: Expr,
  val op: TokenType
) : Expr() {
  override fun toString(): String {
    return "UnaryOp($op, $expr)"
  }
}
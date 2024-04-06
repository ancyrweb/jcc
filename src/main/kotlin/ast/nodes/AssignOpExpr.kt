package fr.ancyr.jcc.ast.nodes

import fr.ancyr.jcc.lex.TokenType

data class AssignOpExpr(
  val op: TokenType,
  val left: Node,
  val right: Expr
) : Expr() {
  override fun toString(): String {
    return "AssignOp($op, $left, $right)"
  }
}
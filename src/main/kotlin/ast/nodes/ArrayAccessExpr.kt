package fr.ancyr.jcc.ast.nodes

import fr.ancyr.jcc.lex.Token

data class ArrayAccessExpr(
  val identifier: Token,
  val expr: Expr,
) : Expr() {
  override fun toString(): String {
    return "ArrayAccessExpr(${identifier.value}, $expr)"
  }
}
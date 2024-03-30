package fr.ancyr.jcc.ast.nodes

import fr.ancyr.jcc.lex.Token

data class ConstantExpr(
  val token: Token
) : Expr() {
  override fun toString(): String {
    return "ConstantExpr(" + token.value + ")"
  }
}
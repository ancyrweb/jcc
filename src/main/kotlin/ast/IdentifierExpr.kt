package fr.ancyr.jcc.ast

import fr.ancyr.jcc.lex.Token

data class IdentifierExpr(
  val token: Token
) : Expr() {
  override fun toString(): String {
    return "IdExpr(" + token.value + ")"
  }
}
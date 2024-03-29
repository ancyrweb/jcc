package fr.ancyr.jcc.ast

import fr.ancyr.jcc.lex.Token

data class ConstantExpr(
  val token: Token
) : Expr()
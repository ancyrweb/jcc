package fr.ancyr.jcc.ast

import fr.ancyr.jcc.lex.Token

data class ArrayAccessExpr(
  val identifier: Token,
  val expr: Expr,
) : Expr()
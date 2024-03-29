package fr.ancyr.jcc.ast

import fr.ancyr.jcc.lex.Token

data class ArrayAccessExpr(
  val expr: Expr,
) : Expr()
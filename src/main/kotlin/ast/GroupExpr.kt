package fr.ancyr.jcc.ast

import fr.ancyr.jcc.lex.Token

data class GroupExpr(
  val expr: Expr,
) : Expr()
package fr.ancyr.jcc.ast

import fr.ancyr.jcc.lex.Token

data class UnaryOp(
  val node: Expr,
  val op: Token
) : Expr()
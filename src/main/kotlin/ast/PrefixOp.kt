package fr.ancyr.jcc.ast

import fr.ancyr.jcc.lex.Token

data class PrefixOp(
  val identifier: IdentifierExpr,
  val op: Token
) : Expr()
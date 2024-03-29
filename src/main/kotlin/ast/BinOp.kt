package fr.ancyr.jcc.ast

import fr.ancyr.jcc.lex.Token

data class BinOp(
  val left: Node,
  val op: Token,
  val right: Node
) : Expr()
package fr.ancyr.jcc.ast.nodes

data class GroupExpr(
  val expr: Expr,
) : Expr()
package fr.ancyr.jcc.ir.nodes

data class IRBinOpExpr(
  val op: IRBinOpOperand,
  val left: IRExpr,
  val right: IRExpr
) : IRExpr()
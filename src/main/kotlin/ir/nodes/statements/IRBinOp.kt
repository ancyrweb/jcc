package fr.ancyr.jcc.ir.nodes.statements

import fr.ancyr.jcc.ir.nodes.expr.IRExpr
import fr.ancyr.jcc.ir.nodes.expr.IRTempExpr

data class IRBinOp(
  val dest: IRTempExpr,
  val operand: IRBinOpOperand,
  val left: IRExpr,
  val right: IRExpr
) : IRStatement()
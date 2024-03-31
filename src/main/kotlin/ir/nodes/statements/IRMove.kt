package fr.ancyr.jcc.ir.nodes.statements

import fr.ancyr.jcc.ir.nodes.expr.IRExpr
import fr.ancyr.jcc.ir.nodes.expr.IRTempExpr

data class IRMove(val dest: IRTempExpr, val source: IRExpr) : IRStatement() {
  override fun toString(): String {
    return "IRMove($dest = $source)"
  }
}
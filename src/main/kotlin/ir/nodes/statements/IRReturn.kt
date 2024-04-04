package fr.ancyr.jcc.ir.nodes.statements

import fr.ancyr.jcc.ir.nodes.expr.IRExpr

data class IRReturn(val expr: IRExpr?) :
  IRStatement() {
  override fun toString(): String {
    return "IRReturn($expr)"
  }
}
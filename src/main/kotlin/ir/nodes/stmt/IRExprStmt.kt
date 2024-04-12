package fr.ancyr.jcc.ir.nodes.stmt

import fr.ancyr.jcc.ir.nodes.expr.IRExpr

data class IRExprStmt(val expr: IRExpr) : IRStmt() {
  override fun toString(): String {
    return "null <- $expr"
  }
}
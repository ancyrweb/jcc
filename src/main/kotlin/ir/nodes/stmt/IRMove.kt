package fr.ancyr.jcc.ir.nodes.stmt

import fr.ancyr.jcc.ir.nodes.expr.IRExpr
import fr.ancyr.jcc.ir.nodes.expr.IRSymbol

data class IRMove(val dest: IRSymbol, val src: IRExpr) : IRStmt() {
  override fun toString(): String {
    return "$dest <- $src"
  }
}
package fr.ancyr.jcc.ir.nodes.stmt

import fr.ancyr.jcc.ir.nodes.expr.IRSymbol

data class IRReturn(val sym: IRSymbol? = null) : IRStmt() {
  override fun toString(): String {
    return "Return $sym"
  }
}
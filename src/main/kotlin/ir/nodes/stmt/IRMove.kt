package fr.ancyr.jcc.ir.nodes.stmt

import fr.ancyr.jcc.ir.nodes.expr.IRExpr
import fr.ancyr.jcc.ir.nodes.expr.IRTemp
import fr.ancyr.jcc.ir.nodes.expr.IRVar

data class IRMove(val dest: IRExpr, val src: IRExpr) : IRStmt() {
  init {
    if (dest !is IRVar && dest !is IRTemp) {
      throw IllegalArgumentException("Invalid destination for move: $dest")
    }
  }

  override fun toString(): String {
    return "$dest <- $src"
  }
}
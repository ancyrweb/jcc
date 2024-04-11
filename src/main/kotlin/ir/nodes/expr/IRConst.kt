package fr.ancyr.jcc.ir.nodes.expr

data class IRConst(val value: Number) : IRExpr() {
  override fun toString(): String {
    return "IRConst(value=$value)"
  }
}
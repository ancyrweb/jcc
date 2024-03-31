package fr.ancyr.jcc.ir.nodes.expr

data class IRTempExpr(val name: String) : IRExpr() {
  override fun toString(): String {
    return "IRTempExpr($name)"
  }
}
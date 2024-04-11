package fr.ancyr.jcc.ir.nodes.expr

data class IRTemp(val name: String) : IRExpr() {
  override fun toString(): String {
    return "T($name)"
  }
}
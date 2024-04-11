package fr.ancyr.jcc.ir.nodes.expr

data class IRVar(val name: String) : IRExpr() {
  override fun toString(): String {
    return "Var($name)"
  }
}
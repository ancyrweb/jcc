package fr.ancyr.jcc.ir.nodes.expr

data class IRVar(val name: String) : IRSymbol() {
  override fun toString(): String {
    return "Var($name)"
  }
}
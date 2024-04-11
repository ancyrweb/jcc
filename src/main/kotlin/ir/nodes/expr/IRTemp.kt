package fr.ancyr.jcc.ir.nodes.expr

data class IRTemp(val name: String) : IRSymbol() {
  override fun toString(): String {
    return "T($name)"
  }
}
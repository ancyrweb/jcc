package fr.ancyr.jcc.ir.nodes.statements

data class IRLabel(val label: String) : IRStatement() {
  override fun toString(): String {
    return "IRLabel($label)"
  }
}
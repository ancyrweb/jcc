package fr.ancyr.jcc.ir.nodes.statements

data class IRLabel(val label: String, val global: Boolean = false) :
  IRStatement() {
  override fun toString(): String {
    return "IRLabel($label, global=$global)"
  }
}
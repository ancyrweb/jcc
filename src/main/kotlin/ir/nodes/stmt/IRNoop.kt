package fr.ancyr.jcc.ir.nodes.stmt

class IRNoop : IRStmt() {
  override fun toString(): String {
    return "noop"
  }
}
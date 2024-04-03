package fr.ancyr.jcc.ir.nodes.statements

import fr.ancyr.jcc.ir.nodes.expr.IRExpr

class IRJumpIf(
  val left: IRExpr,
  val right: IRExpr,
  val comparator: Comparator,
  val label: IRLabel
) : IRStatement() {
  enum class Comparator {
    EQUAL,
    NOT_EQUAL,
    LESS,
    GREATER,
    LESS_OR_EQUAL,
    GREATER_OR_EQUAL,
  }
}
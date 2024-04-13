package fr.ancyr.jcc.ir.nodes.expr

class IRBinop(val op: BinopOperator, val left: IRExpr, val right: IRExpr) :
  IRExpr() {
  init {
    if (left is IRBinop || right is IRBinop) {
      throw IllegalArgumentException("IRBinop cannot have a child of type IRBinop")
    }
  }

  override fun toString(): String {
    when (op) {
      BinopOperator.PLUS -> return "+($left, $right)"
      BinopOperator.MINUS -> return "-($left, $right)"
      BinopOperator.MUL -> return "*($left, $right)"
      BinopOperator.DIV -> return "/($left, $right)"
      BinopOperator.EQUAL -> return "==($left, $right)"
      BinopOperator.NOT_EQUAL -> return "!=($left, $right)"
      BinopOperator.LESS -> return "<($left, $right)"
      BinopOperator.LESS_EQUAL -> return "<=($left, $right)"
      BinopOperator.GREATER -> return ">($left, $right)"
      BinopOperator.GREATER_EQUAL -> return ">=($left, $right)"
      else -> throw IllegalArgumentException("Invalid operator: $op")
    }

  }

  enum class BinopOperator {
    PLUS, MINUS, MUL, DIV,
    EQUAL, NOT_EQUAL, LESS, LESS_EQUAL, GREATER, GREATER_EQUAL
  }
}
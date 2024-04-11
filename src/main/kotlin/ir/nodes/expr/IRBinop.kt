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
    }

  }

  enum class BinopOperator {
    PLUS, MINUS, MUL, DIV
  }
}
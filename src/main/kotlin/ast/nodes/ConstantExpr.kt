package fr.ancyr.jcc.ast.nodes

data class ConstantExpr(
  val value: Any?
) : Expr() {
  override fun toString(): String {
    return "ConstantExpr(" + value + ")"
  }
}
package fr.ancyr.jcc.ast.nodes

data class ArrayAccessExpr(
  val identifier: String,
  val expr: Expr,
) : Expr() {
  override fun toString(): String {
    return "ArrayAccessExpr(${identifier}, $expr)"
  }
}
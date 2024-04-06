package fr.ancyr.jcc.ast.nodes

data class OrExpr(
  val left: Expr,
  val right: Expr
) : Expr() {
  override fun toString(): String {
    return "OrExpr($left, $right)"
  }
}
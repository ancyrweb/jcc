package fr.ancyr.jcc.ast.nodes

data class AndExpr(
  val left: Expr,
  val right: Expr
) : Expr() {
  override fun toString(): String {
    return "AndExpr($left, $right)"
  }
}
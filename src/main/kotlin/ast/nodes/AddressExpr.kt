package fr.ancyr.jcc.ast.nodes

data class AddressExpr(
  val expr: Expr
) : Expr() {
  override fun toString(): String {
    return "AddressExpr($expr)"
  }
}
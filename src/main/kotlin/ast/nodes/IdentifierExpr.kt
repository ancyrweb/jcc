package fr.ancyr.jcc.ast.nodes

data class IdentifierExpr(
  val name: String
) : Expr() {
  override fun toString(): String {
    return "IdExpr($name)"
  }
}
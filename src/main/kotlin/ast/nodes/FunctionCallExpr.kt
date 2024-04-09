package fr.ancyr.jcc.ast.nodes

data class FunctionCallExpr(
  val identifier: String,
  val arguments: List<Expr>
) : Expr() {
  override fun toString(): String {
    return "Call($identifier(${arguments.joinToString(", ")}))"
  }
}
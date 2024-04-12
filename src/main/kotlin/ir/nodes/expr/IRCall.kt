package fr.ancyr.jcc.ir.nodes.expr

data class IRCall(
  val identifier: String,
  val arguments: List<IRSymbol>
) :
  IRExpr() {
  override fun toString(): String {
    return "$identifier(${arguments.joinToString(", ")})"
  }
}
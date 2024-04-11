package fr.ancyr.jcc.ir.nodes.expr

data class IRCall(
  val identifier: String,
  val dest: IRSymbol,
  val arguments: List<IRSymbol>
) :
  IRExpr() {
  override fun toString(): String {
    return "$dest <- $identifier(${arguments.joinToString(", ")})"
  }
}
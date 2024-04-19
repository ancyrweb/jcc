package fr.ancyr.jcc.ir.nodes.expr

data class IRAddress(val node: IRVar) : IRExpr() {

  override fun toString(): String {
    return "&$node"
  }
}
package fr.ancyr.jcc.ir.nodes.expr

data class IRAddress(val node: IRExpr) : IRExpr() {

  override fun toString(): String {
    return "&$node"
  }
}
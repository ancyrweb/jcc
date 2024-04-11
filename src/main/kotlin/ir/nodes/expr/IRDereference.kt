package fr.ancyr.jcc.ir.nodes.expr

data class IRDereference(val node: IRExpr) : IRExpr() {

  override fun toString(): String {
    return "*$node"
  }
}
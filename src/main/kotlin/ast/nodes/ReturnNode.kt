package fr.ancyr.jcc.ast.nodes

data class ReturnNode(val expr: Expr? = null) : Node() {
  override fun toString(): String {
    return "ReturnNode($expr)"
  }
}
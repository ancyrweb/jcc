package fr.ancyr.jcc.ast.nodes

class ExprStatement(var expr: Expr) : Node() {
  override fun toString(): String {
    return "ExprStatement($expr)"
  }
}
package fr.ancyr.jcc.ast

class ExprStatement(var expr: Expr) : Node() {
  override fun toString(): String {
    return "ExprStatement($expr)"
  }
}
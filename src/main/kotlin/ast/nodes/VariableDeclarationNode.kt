package fr.ancyr.jcc.ast.nodes

data class VariableDeclarationNode(
  val typedSymbol: TypedSymbol,
  val value: Expr?
) : Node()
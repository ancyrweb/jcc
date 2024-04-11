package fr.ancyr.jcc.ast.nodes

data class FunctionDefinition(
  val typedSymbol: TypedSymbol,
  val parameters: List<TypedSymbol>,
) : Node()
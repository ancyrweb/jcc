package fr.ancyr.jcc.ast.nodes

data class FunctionBody(
  val typedSymbol: TypedSymbol,
  val parameters: List<TypedSymbol>,
  val block: BlockNode
) : Node()
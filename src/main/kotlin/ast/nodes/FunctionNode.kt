package fr.ancyr.jcc.ast.nodes

data class FunctionNode(
  val typedSymbol: TypedSymbol,
  val parameters: List<TypedSymbol>,
  val block: BlockNode? = null // If null, it's a function declaration
) : Node()
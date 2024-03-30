package fr.ancyr.jcc.ast.nodes

data class BlockNode(
  val statements: List<Node>,
) : Node()
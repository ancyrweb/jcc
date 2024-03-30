package fr.ancyr.jcc.ast.nodes

data class ForLoopNode (
  val init: Node,
  val condition: Node,
  val update: Node,
  val block: BlockNode,
) : Node()
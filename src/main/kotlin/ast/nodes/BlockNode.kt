package fr.ancyr.jcc.ast.nodes

import fr.ancyr.jcc.ast.sem.Scope

data class BlockNode(
  val statements: List<Node>,
  val scope: Scope
) : Node()
package fr.ancyr.jcc.ast.nodes

import fr.ancyr.jcc.ast.sem.Scope

class Program(val nodes: List<Node>, val scope: Scope) : Node() {
  override fun toString(): String {
    return nodes.joinToString("\n")
  }
}
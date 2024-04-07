package fr.ancyr.jcc.ast.nodes

import fr.ancyr.jcc.ast.sem.SymbolType

data class TypedSymbol(
  val identifier: String,
  val type: SymbolType,
  val signed: Boolean,
  val pointer: Boolean,
)
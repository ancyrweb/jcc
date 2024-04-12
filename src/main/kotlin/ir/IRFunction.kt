package fr.ancyr.jcc.ir

import fr.ancyr.jcc.ast.nodes.TypedSymbol
import fr.ancyr.jcc.ast.sem.Scope
import fr.ancyr.jcc.ir.nodes.stmt.IRStmt

class IRFunction(
  val typedSymbol: TypedSymbol,
  val parameters: List<TypedSymbol>,
  val nodes: List<IRStmt>,
  val scope: Scope
)
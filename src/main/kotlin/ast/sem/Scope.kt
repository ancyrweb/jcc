package fr.ancyr.jcc.ast.sem

class Scope(val parent: Scope? = null) {
  val symbolTable = mutableMapOf<String, Symbol>()

  fun addSymbol(symbol: Symbol) {
    symbolTable[symbol.name] = symbol
  }

  override fun toString(): String {
    return "Scope(${symbolTable.toString()})"
  }
}
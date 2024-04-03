package fr.ancyr.jcc.ast.sem

class Scope(val parent: Scope? = null) {
  private val symbolTable = mutableMapOf<String, Symbol>()

  fun addSymbol(symbol: Symbol) {
    symbolTable[symbol.name] = symbol
  }

}
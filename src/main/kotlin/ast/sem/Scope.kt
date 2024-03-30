package fr.ancyr.jcc.ast.sem

class Scope(val parent: Scope? = null) {
  private val symbols = mutableMapOf<String, Symbol>()

  fun addSymbol(symbol: Symbol) {
    symbols[symbol.name] = symbol
  }

  fun getSymbol(name: String): Symbol? {
    return symbols[name] ?: parent?.getSymbol(name)
  }
}
package fr.ancyr.jcc.ast.sem


abstract class Symbol {
  abstract val name: String

  data class Variable(
    override val name: String,
    val type: SymbolType,
    val signed: Boolean,
    val pointer: Boolean,
    val array: ArrayInfo? = null
  ) : Symbol() {
    data class ArrayInfo(val size: Int)
  }

  data class Function(
    override val name: String,
    val type: SymbolType,
    val signed: Boolean,
    val pointer: Boolean,
  ) : Symbol()
}
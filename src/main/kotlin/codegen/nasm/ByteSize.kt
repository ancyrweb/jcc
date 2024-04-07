package fr.ancyr.jcc.codegen.nasm

import fr.ancyr.jcc.ast.nodes.TypedSymbol
import fr.ancyr.jcc.ast.sem.SymbolType

enum class ByteSize {
  BYTE, WORD, DWORD, QWORD;

  fun toInt(): Int {
    return when (this) {
      BYTE -> 1
      WORD -> 2
      DWORD -> 4
      QWORD -> 8
    }
  }

  companion object {
    fun fromInt(value: Int): ByteSize {
      return when (value) {
        1 -> BYTE
        2 -> WORD
        4 -> DWORD
        8 -> QWORD
        else -> throw Exception("Invalid byte size")
      }
    }

    fun fromType(typedSymbol: TypedSymbol): ByteSize {
      return when (typedSymbol.type) {
        SymbolType.INT -> DWORD
        SymbolType.FLOAT -> DWORD
        SymbolType.CHAR -> BYTE
        SymbolType.SHORT -> WORD
        SymbolType.LONG -> DWORD
        SymbolType.DOUBLE -> QWORD
        else -> throw Exception("Invalid byte size")
      }
    }
  }
}

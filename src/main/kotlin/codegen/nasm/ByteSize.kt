package fr.ancyr.jcc.codegen.nasm

import fr.ancyr.jcc.lex.TokenType

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

    fun fromType(type: TokenType): ByteSize {
      return when (type) {
        TokenType.TYPE_INT -> DWORD
        TokenType.TYPE_CHAR -> BYTE
        TokenType.TYPE_FLOAT -> DWORD
        TokenType.TYPE_DOUBLE -> QWORD
        TokenType.TYPE_LONG -> QWORD
        TokenType.TYPE_SHORT -> WORD
        TokenType.TYPE_LONG_LONG -> QWORD
        TokenType.TYPE_LONG_DOUBLE -> QWORD
        else -> throw Exception("Invalid byte size")
      }
    }
  }
}

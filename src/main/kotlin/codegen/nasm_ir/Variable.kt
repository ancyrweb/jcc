package fr.ancyr.jcc.codegen.nasm_ir

data class Variable(val offset: Int, val bytes: Int) {
  fun sizedLocation(): String {
    return "${sizeToStr()} ${location()}"
  }

  fun location(): String {
    return if (offset > 0) {
      "[rbp + $offset]"
    } else if (offset < 0) {
      "[rbp - ${-offset}]"
    } else {
      "[rbp]"
    }
  }

  fun size(): Int {
    return bytes
  }

  fun sizeToStr(): String {
    return when (bytes) {
      1 -> "byte"
      2 -> "word"
      4 -> "dword"
      8 -> "qword"
      else -> "qword"
    }
  }
}

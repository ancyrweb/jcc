package fr.ancyr.jcc.codegen.nasm

import java.util.*

data class StackLocation(val offset: Int, val bytes: ByteSize) {
  fun sizedLocation(): String {
    val qt = bytes.toString().lowercase(Locale.getDefault())
    return "$qt ${location()}"
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

  fun size(): ByteSize {
    return bytes
  }
}

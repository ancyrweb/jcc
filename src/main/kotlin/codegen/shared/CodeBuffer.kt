package fr.ancyr.jcc.codegen.shared

class CodeBuffer(private val code: StringBuilder = StringBuilder()) {
  fun appendNoTab(str: String) {
    code.append("$str\n")
  }

  fun append(str: String) {
    code.append("\t$str\n")
  }

  fun appendLabel(str: String) {
    code.append("$str:\n")
  }

  fun clear() {
    code.clear()
  }

  override fun toString(): String {
    return code.toString()
  }
}
package fr.ancyr.jcc

import fr.ancyr.jcc.lex.Lexer
import java.io.InputStream

class Compiler {
  fun compile(stream: InputStream) {
    val buffer = getContent(stream)
    val lexer = Lexer(buffer)
    val tokens = lexer.parse()

    for (token in tokens) {
      println(token)
    }
  }

  private fun getContent(stream: InputStream): StringBuffer {
    val buffer = StringBuffer()
    var byte = stream.read()
    while (byte != -1) {
      buffer.append(byte.toChar())
      byte = stream.read()
    }

    return buffer
  }
}
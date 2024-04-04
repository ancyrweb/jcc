package fr.ancyr.jcc

import fr.ancyr.jcc.ast.Parser
import fr.ancyr.jcc.codegen.nasm.CodeGenerator
import fr.ancyr.jcc.lex.Lexer
import java.io.InputStream

class Compiler {
  fun compile(stream: InputStream) {
    val buffer = getContent(stream)
    val lexer = Lexer(buffer)
    val tokens = lexer.parse()


    val parser = Parser(tokens)
    val ast = parser.parse()

    val codeGen = CodeGenerator(ast)
    val code = codeGen.generate()

    println(code)
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
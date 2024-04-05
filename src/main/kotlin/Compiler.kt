package fr.ancyr.jcc

import fr.ancyr.jcc.ast.Parser
import fr.ancyr.jcc.codegen.nasm.CodeGenerator
import fr.ancyr.jcc.lex.Lexer
import java.io.InputStream
import java.io.OutputStream

class Compiler(val input: InputStream, val output: OutputStream) {
  fun compile() {
    val buffer = getContent()
    val lexer = Lexer(buffer)
    val tokens = lexer.parse()

    val parser = Parser(tokens)
    val ast = parser.parse()

    val codeGen = CodeGenerator(ast)
    val code = codeGen.generate()

    println(code)

    write(code)
  }

  private fun getContent(): StringBuffer {
    val buffer = StringBuffer()
    var byte = input.read()
    while (byte != -1) {
      buffer.append(byte.toChar())
      byte = input.read()
    }

    return buffer
  }

  private fun write(code: String) {
    output.write(code.toByteArray())
  }
}
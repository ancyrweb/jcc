package fr.ancyr.jcc

import fr.ancyr.jcc.ast.Parser
import fr.ancyr.jcc.codegen.nasm_ir.CodeGenerator
import fr.ancyr.jcc.ir.IRGenerator
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

    val irGen = IRGenerator(ast)
    val ir = irGen.generate()
    
    val codeGen = CodeGenerator(ir)
    val code = codeGen.generate()

    println(code)
    // write(code)
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
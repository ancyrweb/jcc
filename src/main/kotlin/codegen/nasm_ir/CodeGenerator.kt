package fr.ancyr.jcc.codegen.nasm_ir

import fr.ancyr.jcc.ir.IRFunction
import fr.ancyr.jcc.ir.IRProgram

class CodeGenerator(val program: IRProgram) {
  fun generate(): String {
    for (fn in program.functions) {
      genFn(fn)
    }

    return ""
  }

  fun genFn(fn: IRFunction) {
    val allocator = Allocator(fn)

    for (ir in fn.nodes) {
      // println(ir)
    }
  }
}
package fr.ancyr.jcc.codegen.nasm_ir

import fr.ancyr.jcc.codegen.shared.CodeBuffer
import fr.ancyr.jcc.ir.IRFunction
import fr.ancyr.jcc.ir.IRProgram
import fr.ancyr.jcc.ir.nodes.expr.*
import fr.ancyr.jcc.ir.nodes.stmt.IRMove
import fr.ancyr.jcc.ir.nodes.stmt.IRNoop
import fr.ancyr.jcc.ir.nodes.stmt.IRReturn

class CodeGenerator(val program: IRProgram) {
  private val code = CodeBuffer()
  private lateinit var allocator: Allocator

  fun generate(): String {
    code.clear()

    appendNoTab("SECTION .text")
    generateGlobals()
    generateCode()

    return code.toString()
  }

  private fun generateGlobals() {
    for (node in program.functions)
      appendNoTab("global ${node.typedSymbol.identifier}")
  }


  private fun generateCode() {
    for (node in program.functions) {
      genFn(node)

    }
  }

  fun genFn(fn: IRFunction) {
    allocator = Allocator(fn)

    appendNoTab("${fn.typedSymbol.identifier}:")
    append("; prologue")
    append("push rbp")
    append("mov rbp, rsp")


    if (allocator.stackSize > 0) {
      // Note : this might not be required on System V AMD64 ABI
      // See https://en.wikipedia.org/wiki/Red_zone_(computing)
      append("sub rsp, ${allocator.stackSize}")
    }

    append("")



    for (ir in fn.nodes) {
      when (ir) {
        is IRMove -> {
          val dest = getOperand(ir.dest)
          val src = when (ir.src) {
            is IRConst -> {
              ir.src.value.toString()
            }

            is IRTemp, is IRVar -> {
              getOperand(ir.src)
            }

            is IRBinop -> {
              val left = getOperand(ir.src.left)
              val right = getOperand(ir.src.right)

              when (ir.src.op) {
                IRBinop.BinopOperator.PLUS -> {
                  append("add $left, $right")
                }

                IRBinop.BinopOperator.MINUS -> {
                  append("sub $left, $right")
                }

                IRBinop.BinopOperator.MUL -> {
                  append("imul $left, $right")
                }

                IRBinop.BinopOperator.DIV -> {
                  append("mov rax, $left")
                  append("cqo")
                  append("idiv $right")
                }
              }

              left
            }

            is IRDereference -> {
              getOperand(ir.src.node, true)
            }

            is IRAddress -> {
              getOperand(ir.src.node, true)
            }

            else -> {
              "unhandled"
            }
          }

          if (ir.src is IRAddress) {
            append("lea $dest, $src")
          } else if (ir.src is IRDereference) {
            append("mov $dest, $src")
            append("mov $dest, [${dest}]")
          } else {
            append("mov $dest, $src")
          }
        }

        is IRReturn -> {
          if (ir.sym != null) {
            val src = getOperand(ir.sym)
            append("mov rax, $src")
          }
        }

        is IRNoop -> {
          append("")

        }

        else -> {
          append("; unhandled: $ir")
        }
      }

    }

    append("; epilogue")

    if (allocator.stackSize > 0) {
      // This too might not be needed
      append("add rsp, ${allocator.stackSize}")
    }

    append("pop rbp")
    append("ret")
  }

  private fun getOperand(symbol: IRExpr, rawLocation: Boolean = false): String {
    return when (symbol) {
      is IRVar -> {
        val variable = allocator.varBindings[symbol.name]!!
        if (rawLocation) variable.location() else variable.sizedLocation()
      }

      is IRTemp -> {
        val register = allocator.tempBindings[symbol.name]!!
        register.getName(8)
      }

      else -> {
        throw Exception("Invalid destination")
      }
    }
  }

  private fun appendNoTab(str: String) {
    code.appendNoTab(str)
  }

  private fun append(str: String) {
    code.append(str)
  }

  private fun appendLabel(str: String) {
    code.appendLabel(str)
  }
}
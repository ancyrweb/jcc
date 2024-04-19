package fr.ancyr.jcc.codegen.nasm_ir

import fr.ancyr.jcc.codegen.shared.CodeBuffer
import fr.ancyr.jcc.codegen.shared.LabelAllocator
import fr.ancyr.jcc.ir.IRFunction
import fr.ancyr.jcc.ir.IRProgram
import fr.ancyr.jcc.ir.nodes.expr.*
import fr.ancyr.jcc.ir.nodes.expr.IRBinop.BinopOperator
import fr.ancyr.jcc.ir.nodes.stmt.IRMove
import fr.ancyr.jcc.ir.nodes.stmt.IRNoop
import fr.ancyr.jcc.ir.nodes.stmt.IRReturn

class CodeGenerator(private val program: IRProgram) {
  private val code = CodeBuffer()
  private val labelAllocator = LabelAllocator()
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

  private fun genFn(fn: IRFunction) {
    allocator = Allocator(fn)

    val exitLabel = labelAllocator.nextLabel()

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
    genFunParameters(fn)
    append("")
    genFnBody(fn, exitLabel)

    append("; epilogue")
    appendLabel(exitLabel)

    if (allocator.stackSize > 0) {
      // This too might not be needed
      append("add rsp, ${allocator.stackSize}")
    }

    append("pop rbp")
    append("ret")
  }

  private fun genFnBody(
    fn: IRFunction,
    exitLabel: String
  ) {
    for (ir in fn.nodes) {
      append("")
      append("; ${ir}")

      when (ir) {
        is IRMove -> {
          val dest = getOperand(ir.dest)

          when (ir.src) {
            is IRVar -> {
              mov(dest, allocator.varBindings[ir.src.name]!!)
            }

            is IRTemp -> {
              mov(dest, allocator.tempBindings[ir.src.name]!!)
            }

            is IRConst -> {
              val src = Immediate(ir.src.value)
              mov(dest, src)
            }

            is IRBinop -> {
              val left = getOperand(ir.src.left)
              val right = getOperand(ir.src.right)

              mov(dest, left)

              var opSize = 8
              val subSrcStr = when (right) {
                is Variable -> {
                  opSize = right.size()
                  right.sizedLocation()
                }

                is Register -> {
                  right.getName(8)
                }

                else -> throw RuntimeException("Unknown RightStr")
              }

              val subDestStr = (dest as Register).getName(opSize)

              when (ir.src.op) {
                BinopOperator.PLUS -> {
                  append("add $subDestStr, $subSrcStr")
                }

                BinopOperator.MINUS -> {
                  append("sub $subDestStr, $subSrcStr")
                }

                BinopOperator.MUL -> {
                  append("imul $subDestStr, $subSrcStr")
                }

                BinopOperator.DIV -> {
                  append("mov rax, $subDestStr")
                  append("cqo")
                  append("idiv $subSrcStr")
                  append("mov $subDestStr, rax")
                }

                BinopOperator.LESS -> {
                  append("cmp $subDestStr, $subSrcStr")
                  append("setl al")
                  append("movzx $subDestStr, al")
                }

                BinopOperator.LESS_EQUAL -> {
                  append("cmp $subDestStr, $subSrcStr")
                  append("setle al")
                  append("movzx $subDestStr, al")
                }

                BinopOperator.GREATER -> {
                  append("cmp $subDestStr, $subSrcStr")
                  append("setg al")
                  append("movzx $subDestStr, al")
                }

                BinopOperator.GREATER_EQUAL -> {
                  append("cmp $subDestStr, $subSrcStr")
                  append("setge al")
                  append("movzx $subDestStr, al")
                }

                BinopOperator.EQUAL -> {
                  append("cmp $subDestStr, $subSrcStr")
                  append("sete al")
                  append("movzx $subDestStr, al")
                }

                BinopOperator.NOT_EQUAL -> {
                  append("cmp $subDestStr, $subSrcStr")
                  append("setne al")
                  append("movzx $subDestStr, al")
                }

                else -> throw RuntimeException("Unsupported binop ${ir.src.op}")
              }
            }
          }
        }

        is IRReturn -> {
          if (ir.sym != null) {
            mov(Register.rax, getOperand(ir.sym))
            append("jmp $exitLabel")
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
  }

  private fun genFunParameters(fn: IRFunction) {
    val parameterRegisters = Allocator.argumentRegisters.toMutableList()

    for (node in fn.parameters) {
      if (parameterRegisters.isEmpty()) {
        break
      }

      val register = parameterRegisters.removeFirst()
      val size = Allocator.getSize(node)
      val name = register.getName(size)
      val location = allocator.varBindings[node.identifier]!!

      if (size.toInt() <= 2) {
        // Fetch only the last bits
        append("mov ${Register.rax.getName(size)}, $name")
        append("mov ${location.sizedLocation()}, ${Register.rax.getName(size)}")
      } else {
        append("mov ${location.sizedLocation()}, $name")
      }
    }
  }

  private fun getOperand(
    symbol: IRSymbol
  ): Any {
    return when (symbol) {
      is IRVar -> {
        allocator.varBindings[symbol.name]!!
      }

      is IRTemp -> {
        allocator.tempBindings[symbol.name]!!
      }

      else -> {
        throw Exception("Invalid destination")
      }
    }
  }

  private fun mov(dest: Any, src: Any) {
    var l: String = ""
    var r: String = ""

    if (dest is Register) {
      if (src is Variable) {
        if (src.size() != 8) {
          // Zero out
          val reg = dest.getName(8)
          append("xor ${reg}, ${reg}")
        }

        l = dest.getName(src.size())
        r = src.sizedLocation()
      } else if (src is Immediate) {
        l = dest.getName(8)
        r = src.value.toString()
      } else if (src is Register) {
        l = dest.getName(8)
        r = src.getName(8)
      }
    } else if (dest is Variable) {
      l = dest.sizedLocation()

      if (src is Register) {
        r = src.getName(dest.size())
      }
    }

    append("mov $l, $r")
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
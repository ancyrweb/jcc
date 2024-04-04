package fr.ancyr.jcc.ir.codegen

import fr.ancyr.jcc.ir.RegisterAllocator
import fr.ancyr.jcc.ir.nodes.expr.IRConstExpr
import fr.ancyr.jcc.ir.nodes.expr.IRExpr
import fr.ancyr.jcc.ir.nodes.expr.IRTempExpr
import fr.ancyr.jcc.ir.nodes.literal.IRBits
import fr.ancyr.jcc.ir.nodes.literal.IRIntLiteral
import fr.ancyr.jcc.ir.nodes.statements.*

class CodegenX86(private val statements: List<IRStatement>) {
  private val registerAllocator = RegisterAllocator()
  private val code = StringBuilder()

  fun generate(): String {
    code.clear()

    generateCode()

    return code.toString()
  }

  fun generateCode() {
    for (node in statements) {
      when (node) {
        is IRMove -> {
          val dest = registerAllocator.allocate(node.dest.name)
          val source = translateExpr(node.source)
          code.append("mov $dest $source\n")
        }

        is IRBinOp -> {
          val dest = registerAllocator.allocate(node.dest.name)
          val op = when (node.operand) {
            IRBinOpOperand.PLUS -> "add"
            IRBinOpOperand.MINUS -> "sub"
            IRBinOpOperand.MUL -> "mul"
            IRBinOpOperand.DIV -> "div"
            IRBinOpOperand.AND -> "and"
            IRBinOpOperand.OR -> "or"
            IRBinOpOperand.XOR -> "xor"
            IRBinOpOperand.MOD -> TODO()
          }

          code.append("mov rax ${translateExpr(node.left)}\n")
          code.append("$op rax ${translateExpr(node.right)}\n")
          code.append("mov $dest rax\n")
        }

        is IRLabel -> {
          code.append("${node.label}:\n")

          if (node.isFn) {
            code.append("push rbp\n") // save caller's frame pointer
            code.append("mov rbp rsp\n") // set up our frame pointer
            code.append("sub rsp 128\n") // allocate stack frame
          }
        }

        is IRReturn -> {
          val value =
            node.expr ?: IRConstExpr(IRIntLiteral(0, IRBits.IR64Bits));

          code.append("mov rax ${translateExpr(value)}\n") // push result into rax
          code.append("pop rbp\n") // restore stack frame
          code.append("ret\n") // return
        }

        else -> throw IllegalArgumentException("Unsupported statement type")
      }

      code.append("\n")
    }

  }

  private fun translateExpr(expr: IRExpr): String {
    return when (expr) {
      is IRTempExpr -> registerAllocator.getRegister(expr.name)
      is IRConstExpr -> {
        when (val literal = expr.literal) {
          is IRIntLiteral -> literal.value.toString()
          else -> throw IllegalArgumentException("Unsupported literal type")
        }
      }

      else -> throw IllegalArgumentException("Unsupported expression type")
    }
  }
}
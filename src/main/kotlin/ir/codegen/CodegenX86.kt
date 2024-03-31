package fr.ancyr.jcc.ir.codegen

import fr.ancyr.jcc.ir.RegisterAllocator
import fr.ancyr.jcc.ir.nodes.expr.IRConstExpr
import fr.ancyr.jcc.ir.nodes.expr.IRExpr
import fr.ancyr.jcc.ir.nodes.expr.IRTempExpr
import fr.ancyr.jcc.ir.nodes.literal.IRIntLiteral
import fr.ancyr.jcc.ir.nodes.statements.*

class CodegenX86(private val statements: List<IRStatement>) {
  private val registerAllocator = RegisterAllocator()
  private val code = StringBuilder()

  fun compileIR(): String {
    code.clear()

    code.append("section .text\n")
    code.append("global _start\n")
    code.append("\n")

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
            IRBinOpOperand.TIMES -> "mul"
            IRBinOpOperand.DIV -> "div"
            IRBinOpOperand.AND -> "and"
            IRBinOpOperand.OR -> "or"
            IRBinOpOperand.XOR -> "xor"
            IRBinOpOperand.MOD -> TODO()
          }

          code.append("mov eax ${translateExpr(node.left)}\n")
          code.append("$op eax ${translateExpr(node.right)}\n")
          code.append("mov $dest eax\n")
        }

        is IRLabel -> {
          code.append("${node.label}:\n")
        }

        is IRReturn -> {
          if (node.isMain) {
            code.append("mov eax 0x60\n")
            code.append("mov edi ${translateExpr(node.expr)}\n")
            code.append("syscall\n")
          } else {
            code.append("mov eax ${translateExpr(node.expr)}\n")
            code.append("ret\n")
          }

        }

        else -> throw IllegalArgumentException("Unsupported statement type")
      }
    }

    return code.toString()
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
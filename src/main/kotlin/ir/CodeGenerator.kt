package fr.ancyr.jcc.ir

import fr.ancyr.jcc.ir.nodes.IRBinOp
import fr.ancyr.jcc.ir.nodes.IRBinOpOperand
import fr.ancyr.jcc.ir.nodes.IRMove
import fr.ancyr.jcc.ir.nodes.IRStatement
import fr.ancyr.jcc.ir.nodes.expr.IRConstExpr
import fr.ancyr.jcc.ir.nodes.expr.IRExpr
import fr.ancyr.jcc.ir.nodes.expr.IRTempExpr
import fr.ancyr.jcc.ir.nodes.literal.IRIntLiteral

class CodeGenerator(private val statements: List<IRStatement>) {
  private val registerAllocator = RegisterAllocator()
  private val code = StringBuilder()

  fun compileIR(): String {
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
            IRBinOpOperand.MOD -> TODO()
          }

          code.append("mov eax ${translateExpr(node.left)}\n")
          code.append("$op eax ${translateExpr(node.right)}\n")
          code.append("mov $dest eax\n")
        }
      }
    }

    return code.toString()
  }

  fun translateExpr(expr: IRExpr) : String {
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

fun main() {
  val generator = CodeGenerator(listOf(
    IRMove(
      IRTempExpr("t1"),
      IRConstExpr(IRIntLiteral.bits64(10))
    ),
    IRMove(
      IRTempExpr("t2"),
      IRConstExpr(IRIntLiteral.bits64(20))
    ),
    IRBinOp(
      IRTempExpr("t3"),
      IRBinOpOperand.PLUS,
      IRTempExpr("t1"),
      IRTempExpr("t2")
    ),
    IRBinOp(
      IRTempExpr("t4"),
      IRBinOpOperand.MINUS,
      IRTempExpr("t1"),
      IRTempExpr("t2")
    ),
  ))

  val code = generator.compileIR()
  println(code)
}
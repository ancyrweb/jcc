package fr.ancyr.jcc.ir

import fr.ancyr.jcc.ir.nodes.*

fun main() {
  var code = mutableListOf(
    IRMove(
      IRTempExpr("t0"),
      IRBinOpExpr(
        IRBinOpOperand.PLUS,
        IRConstExpr(1),
        IRConstExpr(2)
      )
    )
  )

  var instructions = compileIR(code)
}

fun compileIR(code: List<IRStatement>) {
  var instructions = mutableListOf<String>()

  for (node in code) {
    when (node) {
      is IRMove -> {
        println("Move ${node.dst} <- ${node.src}")
      }
    }
  }
}

fun translateExpr(expr: IRExpr): String {
  return when (expr) {
    is IRConstExpr -> expr.value.toString()
    is IRTempExpr -> "%eax"
    is IRBinOpExpr -> {
      val left = translateExpr(expr.left)
      val right = translateExpr(expr.right)
      when (expr.op) {
        IRBinOpOperand.PLUS -> "add $left, $right"
        IRBinOpOperand.MINUS -> "sub $left, $right"
        IRBinOpOperand.TIMES -> "mul $left, $right"
        IRBinOpOperand.DIV -> "div $left, $right"
        IRBinOpOperand.MOD -> "mod $left, $right"
    }
  }
}
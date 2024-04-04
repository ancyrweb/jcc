package fr.ancyr.jcc.ir

import fr.ancyr.jcc.ir.codegen.CodegenX86
import fr.ancyr.jcc.ir.nodes.expr.IRConstExpr
import fr.ancyr.jcc.ir.nodes.expr.IRTempExpr
import fr.ancyr.jcc.ir.nodes.literal.IRIntLiteral
import fr.ancyr.jcc.ir.nodes.statements.*

fun main() {
  val generator = CodegenX86(
    listOf(
      IRLabel("_start", global = true),
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
      IRReturn(IRTempExpr("t4"))
    )
  )

  val code = generator.generate()
  println(code)
}
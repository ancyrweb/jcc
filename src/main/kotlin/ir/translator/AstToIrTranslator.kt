package fr.ancyr.jcc.ir.translator

import fr.ancyr.jcc.ast.nodes.*
import fr.ancyr.jcc.ir.nodes.expr.IRConstExpr
import fr.ancyr.jcc.ir.nodes.expr.IRExpr
import fr.ancyr.jcc.ir.nodes.expr.IRTempExpr
import fr.ancyr.jcc.ir.nodes.literal.IRBits
import fr.ancyr.jcc.ir.nodes.literal.IRIntLiteral
import fr.ancyr.jcc.ir.nodes.statements.*
import fr.ancyr.jcc.lex.Token
import fr.ancyr.jcc.lex.TokenType

class AstToIrTranslator(private val ast: List<Node>) {
  val ir = mutableListOf<IRStatement>()
  val tempAllocator = TempAllocator()
  val varToTemp = mutableMapOf<String, TempAllocator.Temp>()

  fun translate(): List<IRStatement> {
    for (node in ast) {
      if (node is FunctionNode) {
        translateFunction(node)
      }
    }

    return ir;
  }

  private fun translateFunction(fn: FunctionNode) {
    if (fn.block == null) {
      return // it's a function declaration
    }

    ir.add(IRLabel(fn.identifier.asString(), true))
    var isMain = fn.identifier.asString() == "main";

    for (node in fn.block.statements) {
      if (node is VariableDeclarationNode) {
        val varName = node.identifier.asString()
        val dest = tempAllocator.allocate()
        varToTemp[varName] = dest

        if (node.value != null) {
          ir.add(
            IRMove(
              IRTempExpr(dest.name),
              toExpr(node.value)
            )
          )
        }
      } else if (node is KeywordDeclarationNode) {
        if (node.keyword.isTokenType(TokenType.KEYWORD_RETURN)) {
          if (node.expr != null) {
            ir.add(IRReturn(toExpr(node.expr), isMain))
          } else {
            ir.add(IRReturn(null, isMain))
          }
        } else {
          throw RuntimeException("Unknown keyword: ${node.keyword}")
        }
      }
    }
  }


  private fun toExpr(expr: Expr): IRExpr {
    if (expr is ConstantExpr) {
      val token = expr.token
      return IRConstExpr(IRIntLiteral(token.value as Long, IRBits.IR64Bits))
    } else if (expr is IdentifierExpr) {
      val identifier = expr.token.asString()
      val temp = varToTemp[identifier]
        ?: throw RuntimeException("Unknown variable: ${identifier}")

      return IRTempExpr(temp.name)
    } else if (expr is BinOpExpr) {
      val dest = tempAllocator.allocate()
      val irTemp = IRTempExpr(dest.name)

      ir.add(
        IRBinOp(
          irTemp,
          astBinopToIrBinop(expr.op),
          toExpr(expr.left),
          toExpr(expr.right)
        )
      )

      return irTemp
    } else {
      throw RuntimeException("Unknown expression type: ${expr}")
    }
  }

  private fun astBinopToIrBinop(token: Token): IRBinOpOperand {
    return when (token.type) {
      TokenType.OP_PLUS -> IRBinOpOperand.PLUS
      TokenType.OP_MINUS -> IRBinOpOperand.MINUS
      TokenType.OP_MUL -> IRBinOpOperand.MUL
      TokenType.OP_DIV -> IRBinOpOperand.DIV
      else -> throw RuntimeException("Unknown binop: ${token}")
    }
  }
}
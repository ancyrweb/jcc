package fr.ancyr.jcc.ir

import fr.ancyr.jcc.ast.nodes.*
import fr.ancyr.jcc.ir.nodes.expr.*
import fr.ancyr.jcc.ir.nodes.stmt.IRMove
import fr.ancyr.jcc.ir.nodes.stmt.IRNoop
import fr.ancyr.jcc.lex.TokenType

class IRGenerator(private val program: Program) {
  fun generate(): String {
    for (node in program.nodes) {
      if (node is FunctionBody) {
        FunctionIRGenerator(node).generate()
      }
    }

    return ""
  }


  class FunctionIRGenerator(val fn: FunctionBody) {
    val graph = mutableListOf<Any>()
    val temps = TempGenerator()

    fun generate() {
      for (node in fn.block.statements) {
        when (node) {
          is VariableDeclarationNode -> genVarDecl(node)
          is ExprStatement -> genExprStmt(node)
          else -> throw IllegalArgumentException("Invalid statement: $node")
        }

        graph.add(IRNoop())
      }

      for (node in graph) {
        println(node)
      }
    }

    private fun genVarDecl(node: VariableDeclarationNode) {
      if (node.value != null) {
        val temp = genExpr(node.value)
        graph.add(
          IRMove(
            IRVar(node.typedSymbol.identifier),
            IRTemp(temp)
          )
        )
      }
    }

    private fun genExprStmt(node: ExprStatement) {
      genExpr(node.expr)
    }

    private fun genExpr(node: Expr): String {
      when (node) {
        is ConstantExpr -> {
          val temp = temps.next()
          val expr = IRMove(
            IRTemp(temp),
            IRConst(node.value as Number)
          )

          graph.add(expr)
          return temp
        }

        is BinOpExpr -> {
          val left = genExpr(node.left)
          val right = genExpr(node.right)
          val temp = temps.next()

          val op = when (node.op) {
            TokenType.OP_PLUS -> IRBinop.BinopOperator.PLUS
            TokenType.OP_MINUS -> IRBinop.BinopOperator.MINUS
            TokenType.OP_MUL -> IRBinop.BinopOperator.MUL
            TokenType.OP_DIV -> IRBinop.BinopOperator.DIV
            else -> throw IllegalArgumentException("Invalid operator: ${node.op}")
          }

          val expr = IRMove(
            IRTemp(temp),
            IRBinop(op, IRTemp(left), IRTemp(right))
          )

          graph.add(expr)

          return temp
        }

        is GroupExpr -> {
          return genExpr(node.expr)
        }

        is AddressExpr -> {
          if (node.expr !is IdentifierExpr) {
            throw IllegalArgumentException("Invalid expression: $node")
          }

          val temp = temps.next()
          val expr = IRMove(
            IRTemp(temp),
            IRAddress(IRVar(node.expr.name))
          )

          graph.add(expr)

          return temp
        }

        is DereferenceExpr -> {
          if (node.expr !is IdentifierExpr) {
            throw IllegalArgumentException("Invalid expression: $node")
          }

          val temp = temps.next()
          val expr = IRMove(
            IRTemp(temp),
            IRDereference(IRVar(node.expr.name))
          )

          graph.add(expr)

          return temp
        }

        is AssignOpExpr -> {
          val temp = genExpr(node.right)

          if (node.left is IdentifierExpr) {
            when (node.op) {
              TokenType.OP_EQUAL -> {
                val expr = IRMove(
                  IRVar(node.left.name),
                  IRTemp(temp)
                )

                graph.add(expr)
              }

              else -> throw IllegalArgumentException("Invalid operator: ${node.op}")
            }
          } else {
            throw IllegalArgumentException("Invalid expression: $node")
          }

          return temp
        }

        is FunctionCallExpr -> {
          val arguments = mutableListOf<IRTemp>()
          for (arg in node.arguments) {
            val temp = genExpr(arg)
            arguments.add(IRTemp(temp))
          }

          val temp = temps.next()
          val expr = IRCall(node.identifier, IRTemp(temp), arguments)
          graph.add(expr)

          return temp
        }

        else -> throw IllegalArgumentException("Invalid expression: $node")
      }
    }
  }
}
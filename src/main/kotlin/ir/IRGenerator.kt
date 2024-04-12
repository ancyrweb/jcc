package fr.ancyr.jcc.ir

import fr.ancyr.jcc.ast.nodes.*
import fr.ancyr.jcc.ir.nodes.expr.*
import fr.ancyr.jcc.ir.nodes.stmt.IRMove
import fr.ancyr.jcc.ir.nodes.stmt.IRNoop
import fr.ancyr.jcc.ir.nodes.stmt.IRStmt
import fr.ancyr.jcc.lex.TokenType

class IRGenerator(private val program: Program) {
  fun generate(): IRProgram {
    val functions = mutableListOf<IRFunction>()

    for (node in program.nodes) {
      if (node is FunctionBody) {
        functions.add(FunctionIRGenerator(node).generate())
      }
    }

    return IRProgram(functions)
  }


  class FunctionIRGenerator(private val fn: FunctionBody) {
    private val graph = mutableListOf<IRStmt>()
    private val temps = TempGenerator()

    fun generate(): IRFunction {
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

      return IRFunction(fn.typedSymbol, fn.parameters, graph)
    }

    private fun genVarDecl(node: VariableDeclarationNode) {
      if (node.value != null) {
        val sym = genExpr(node.value)
        graph.add(
          IRMove(
            IRVar(node.typedSymbol.identifier),
            sym
          )
        )
      }
    }

    private fun genExprStmt(node: ExprStatement) {
      genExpr(node.expr)
    }

    private fun genExpr(node: Expr): IRSymbol {
      when (node) {
        is ConstantExpr -> {
          val temp = IRTemp(temps.next())
          val expr = IRMove(
            temp,
            IRConst(node.value as Number)
          )

          graph.add(expr)
          return temp
        }

        is IdentifierExpr -> {
          return IRVar(node.name)
        }

        is BinOpExpr -> {
          val left = genExpr(node.left)
          val right = genExpr(node.right)
          val temp = IRTemp(temps.next())

          val op = when (node.op) {
            TokenType.OP_PLUS -> IRBinop.BinopOperator.PLUS
            TokenType.OP_MINUS -> IRBinop.BinopOperator.MINUS
            TokenType.OP_MUL -> IRBinop.BinopOperator.MUL
            TokenType.OP_DIV -> IRBinop.BinopOperator.DIV
            else -> throw IllegalArgumentException("Invalid operator: ${node.op}")
          }

          val expr = IRMove(
            temp,
            IRBinop(op, left, right)
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

          val temp = IRTemp(temps.next())
          val expr = IRMove(
            temp,
            IRAddress(IRVar(node.expr.name))
          )

          graph.add(expr)

          return temp
        }

        is DereferenceExpr -> {
          if (node.expr !is IdentifierExpr) {
            throw IllegalArgumentException("Invalid expression: $node")
          }

          val temp = IRTemp(temps.next())
          val expr = IRMove(
            temp,
            IRDereference(IRVar(node.expr.name))
          )

          graph.add(expr)

          return temp
        }

        is AssignOpExpr -> {
          val sym = genExpr(node.right)

          if (node.left is IdentifierExpr) {
            when (node.op) {
              TokenType.OP_EQUAL -> {
                val expr = IRMove(
                  IRVar(node.left.name),
                  sym
                )

                graph.add(expr)
              }

              else -> throw IllegalArgumentException("Invalid operator: ${node.op}")
            }
          } else {
            throw IllegalArgumentException("Invalid expression: $node")
          }

          return sym
        }

        is FunctionCallExpr -> {
          val arguments = mutableListOf<IRSymbol>()
          for (arg in node.arguments) {
            val sym = genExpr(arg)
            arguments.add(sym)
          }

          val temp = IRTemp(temps.next())
          val expr = IRMove(temp, IRCall(node.identifier, arguments))
          graph.add(expr)

          return temp
        }

        else -> throw IllegalArgumentException("Invalid expression: $node")
      }
    }
  }
}
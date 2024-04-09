package fr.ancyr.jcc.codegen.nasm

import fr.ancyr.jcc.ast.nodes.*
import fr.ancyr.jcc.lex.TokenType

class CodeGenerator(private val program: Program) {
  private val code = CodeBuffer()
  private lateinit var allocator: MemoryAllocator
  private lateinit var currentFunction: FunctionNode
  private lateinit var returnLabel: String
  private val labelAllocator = LabelAllocator()
  private val optimize = true

  private fun appendNoTab(str: String) {
    code.appendNoTab(str)
  }

  private fun append(str: String) {
    code.append(str)
  }

  private fun appendLabel(str: String) {
    code.appendLabel(str)
  }

  fun generate(): String {
    code.clear()

    appendNoTab("SECTION .text")
    generateGlobals()
    generateCode()

    return code.toString()
  }

  private fun generateGlobals() {
    for (node in program.nodes) {
      if (node is FunctionNode) {
        appendNoTab("global ${node.typedSymbol.identifier}")
      }
    }
  }

  private fun generateCode() {
    for (node in program.nodes) {
      if (node is FunctionNode) {
        genFunction(node)
      }
    }
  }

  private fun genBlock(block: BlockNode) {
    for (child in block.statements) {
      generateNode(child)
    }
  }

  private fun generateNode(node: Node) {
    when (node) {
      is VariableDeclarationNode -> {
        genVarDecl(node)
      }

      is ExprStatement -> {
        genExpr(node.expr)
      }

      is IfNode -> {
        genIf(node)
      }

      is WhileNode -> {
        genWhile(node)
      }

      is ReturnNode -> {
        genReturn(node)
      }

      else -> {
        return
      }
    }
  }

  private fun genReturn(node: ReturnNode) {
    if (node.expr != null) {
      val destSize =
        ByteSize.fromType(currentFunction.typedSymbol)

      genExpr(node.expr, destSize)
      append("jmp $returnLabel")
    }

    // The epilogue is written in the function generation
  }

  private fun genFunction(fn: FunctionNode) {
    if (fn.block == null) {
      return
    }

    currentFunction = fn
    allocator = MemoryAllocator(fn)
    returnLabel = labelAllocator.nextLabel()

    appendNoTab("${fn.typedSymbol.identifier}:")
    append("; prologue")
    append("push rbp")
    append("mov rbp, rsp\n")

    if (allocator.stackSize > 0) {
      // Note : this might not be required on System V AMD64 ABI
      // See https://en.wikipedia.org/wiki/Red_zone_(computing)
      append("sub rsp, ${allocator.stackSize}")
    }

    genFunParameters(fn)
    genBlock(fn.block)

    append("; epilogue")
    appendLabel(returnLabel)

    if (allocator.stackSize > 0) {
      // This too might not be needed
      append("add rsp, ${allocator.stackSize}")
    }

    append("pop rbp")
    append("ret")
  }

  private fun genFunParameters(fn: FunctionNode) {
    val parameterRegisters =
      mutableListOf(
        Register.RDI,
        Register.RSI,
        Register.RDX,
        Register.RCX,
        Register.R8,
        Register.R9
      )

    for (node in fn.parameters) {
      if (parameterRegisters.isEmpty()) {
        break
      }

      val register = parameterRegisters.removeFirst()
      val size = ByteSize.fromType(node)
      val name = register.name(ByteSize.fromType(node))
      val location = allocator.getLocationOrFail(node.identifier)

      if (size.toInt() <= 2) {
        // Fetch only the last bits
        append("mov ${Register.RAX.name(size)}, $name")
        append("mov ${location.sizedLocation()}, ${Register.RAX.name(size)}")
      } else {
        append("mov ${location.sizedLocation()}, $name")
      }
    }
  }

  private fun genVarDecl(node: VariableDeclarationNode) {
    val location = allocator.getLocationOrFail(node.typedSymbol.identifier)
    val src = Register.RAX.name(size = location.size())

    if (node.value != null) {
      if (optimize) {
        if (node.value is ConstantExpr) {
          append("mov ${location.sizedLocation()}, ${node.value.value}")
          return
        }
      }

      genExpr(node.value)
    }


    append("mov ${location.sizedLocation()}, $src")
  }

  private fun genIf(node: IfNode) {
    val endLabel = labelAllocator.nextLabel()

    var cur: IfNode? = node

    while (cur != null) {
      val bodyLabel = labelAllocator.nextLabel()
      val outLabel: String? =
        if (cur.elseBlock != null || cur.elseIf != null) labelAllocator.nextLabel() else null

      genComparisons(
        cur.condition,
        outLabel ?: endLabel,
        bodyLabel,
        context = ExprContext.CONDITIONAL
      )

      appendLabel(bodyLabel)
      genBlock(cur.thenBlock)
      append("jmp $endLabel\n")

      if (cur.elseIf != null) {
        appendLabel(outLabel!!)
        cur = cur.elseIf
      } else if (cur.elseBlock != null) {
        appendLabel(outLabel!!)
        genBlock(cur.elseBlock!!)
        cur = null
      } else {
        cur = null
      }
    }

    appendLabel(endLabel)
  }

  private fun genWhile(node: WhileNode) {
    val startLabel = labelAllocator.nextLabel()
    val bodyLabel = labelAllocator.nextLabel()
    val endLabel = labelAllocator.nextLabel()

    appendLabel(startLabel)
    genComparisons(
      node.condition,
      endLabel,
      bodyLabel,
      context = ExprContext.LOOP
    )

    appendLabel(bodyLabel)
    genBlock(node.block)
    append("jmp $startLabel\n")
    appendLabel(endLabel)
  }

  private fun genComparisons(
    condition: Expr,
    outLabel: String,
    bodyLabel: String,
    isInAnd: Boolean = false,
    depth: Int = 0,
    context: ExprContext
  ) {

    when (condition) {
      is OrExpr -> {
        genComparisons(
          condition.left,
          outLabel,
          bodyLabel,
          depth = depth + 1,
          context = context
        )
        genComparisons(
          condition.right,
          outLabel,
          bodyLabel,
          depth = depth + 1,
          context = context
        )

        // The overall if is contained in an OR expression, so that means
        // branches will jump to the body label if they are true
        if (depth == 0) {
          append("jmp $outLabel\n")
        }
      }

      is AndExpr -> {
        genComparisons(
          condition.left,
          outLabel,
          bodyLabel,
          true,
          depth = depth + 1,
          context = context
        )
        genComparisons(
          condition.right,
          outLabel,
          bodyLabel,
          true,
          depth = depth + 1,
          context = context
        )

        // Here in AND it's the opposite, branches will jump to the out label
        // if they are false because ALL the conditions are necessary in an end
        // Only if all conditions are met we can reach the body
        // And since the body follows the condition, there's no need to jump
      }

      is ComparisonExpr -> {
        genExpr(condition, context = ExprContext.CONDITIONAL)

        if (isInAnd) {
          // Inside "AND", any false condition should jump to the out label
          genInvertedJump(condition.op, outLabel)
        } else {
          // Otherwise, we jump to the body label if the condition is true
          genJump(condition.op, bodyLabel)

          if (context == ExprContext.LOOP) {
            if (depth == 0) {
              append("jmp $outLabel\n")
            }
          }
        }
      }

      else -> {
        println("Unsupported condition type")
      }
    }
  }

  private fun genExpr(
    expr: Expr,
    destSize: ByteSize = ByteSize.QWORD,
    context: ExprContext = ExprContext.NORMAL
  ) {
    when (expr) {
      is ConstantExpr -> {
        // Always move a constant into RAX because we can't care less
        append("mov rax, ${expr.value}")
      }

      is IdentifierExpr -> {

        val location = allocator.getLocationOrFail(expr.name)

        // Depending on the size of the source & the destination,
        // we will have to adjust the registers used

        if (destSize.toInt() > location.size().toInt()) {
          // The destination is larger than the location
          // So we need to add padding 0
          val dest = Register.RAX.name(destSize)
          append("movsx $dest, ${location.sizedLocation()}")
        } else if (destSize.toInt() < location.size().toInt()) {
          // The destination is smaller than the location
          // So we use the size of the source as a reference
          val dest = Register.RAX.name(destSize)
          append("mov $dest, ${location.sizedLocation()}")
        } else {
          // They have the same size
          val dest = Register.RAX.name(destSize)
          append("mov $dest, ${location.sizedLocation()}")
        }
      }

      is BinOpExpr -> {
        genExpr(expr.right)
        append("push rax")
        genExpr(expr.left)
        append("pop rbx")

        when (expr.op) {
          TokenType.OP_PLUS -> append("add rax, rbx")
          TokenType.OP_MINUS -> append("sub rax, rbx")
          // Note : mul & div are handled quite differently
          // from add & sub in GCC
          // it's worth investing time in understanding the difference
          TokenType.OP_MUL -> append("imul rax, rbx")
          TokenType.OP_DIV -> {
            append("cqo")
            append("idiv rbx")
          }

          else -> {
            println("Unsupported binary operator")
            return
          }
        }
      }

      is ComparisonExpr -> {
        if (optimize) {
          if (expr.left is IdentifierExpr && expr.right is ConstantExpr) {
            val location = allocator.getLocationOrFail(expr.left.name)
            append("cmp ${location.sizedLocation()}, ${expr.right.value}")
            return
          }
        }

        genExpr(expr.right)
        append("push rax")
        genExpr(expr.left)
        append("pop rbx")

        if (context == ExprContext.CONDITIONAL) {
          append("cmp rax, rbx")
          return
        } else {
          // TODO Handle setcc
        }
      }

      is GroupExpr -> {
        genExpr(expr.expr)
      }

      is AssignOpExpr -> {
        if (expr.left !is IdentifierExpr) {
          println("Left side of assignment must be an identifier")
          return
        }

        val location = allocator.getLocationOrFail(expr.left.name)
        val dest = Register.RAX.name(location.size())

        if (optimize) {
          if (expr.op == TokenType.OP_EQUAL_EQUAL && expr.right is ConstantExpr) {
            append("mov ${location.sizedLocation()}, ${expr.right.value}")
            return
          }
        }

        genExpr(expr.right)

        when (expr.op) {
          TokenType.OP_EQUAL_EQUAL -> append("mov ${location.sizedLocation()}, $dest")
          TokenType.OP_PLUS_EQUAL -> append("add ${location.sizedLocation()}, $dest")
          TokenType.OP_MINUS_EQUAL -> append("sub ${location.sizedLocation()}, $dest")
          TokenType.OP_MUL_EQUAL -> append("imul ${location.sizedLocation()}, $dest")
          else -> {
            println("Unsupported assignment operator")
            return
          }
        }
      }

      is PostfixOpExpr, is PrefixOpExpr -> {
        // Very ugly
        val subExpr =
          if (expr is PostfixOpExpr) expr.expr else (expr as PrefixOpExpr).expr
        val op =
          if (expr is PostfixOpExpr) expr.op else (expr as PrefixOpExpr).op

        if (subExpr !is IdentifierExpr) {
          throw Exception("Postfix/Prefix operator must be applied to an identifier")
        }

        val location = allocator.getLocationOrFail(subExpr.name)

        when (op) {
          TokenType.OP_PLUS_PLUS -> append("add ${location.sizedLocation()}, 1")
          TokenType.OP_MINUS_MINUS -> append("sub ${location.sizedLocation()}, 1")
          else -> throw Exception("Unsupported postfix operator")
        }
      }

      is AddressExpr -> {
        if (expr.expr is IdentifierExpr) {
          val location = allocator.getLocationOrFail(expr.expr.name)
          append("lea rax, ${location.location()}")
        } else {
          // TODO handle array access
          println("Address operator must be applied to an identifier")
          return
        }
      }

      is DereferenceExpr -> {
        genExpr(expr.expr)
        append("mov rax, [rax]")
      }

      else -> {
        println("Unrecognized expression type")
        println(expr)
        return
      }
    }
  }

  private fun genInvertedJump(op: TokenType, target: String) {
    when (op) {
      TokenType.OP_EQUAL_EQUAL -> append("jne $target")
      TokenType.OP_NOT_EQUAL -> append("je $target")
      TokenType.OP_GREATER -> append("jle $target")
      TokenType.OP_GREATER_EQUAL -> append("jl $target")
      TokenType.OP_LESS -> append("jge $target")
      TokenType.OP_LESS_EQUAL -> append("jg $target")
      else -> throw Exception("Unsupported comparison operator")
    }
  }

  private fun genJump(op: TokenType, target: String) {
    when (op) {
      TokenType.OP_EQUAL_EQUAL -> append("je $target")
      TokenType.OP_NOT_EQUAL -> append("jne $target")
      TokenType.OP_GREATER -> append("jg $target")
      TokenType.OP_GREATER_EQUAL -> append("jge $target")
      TokenType.OP_LESS -> append("jl $target")
      TokenType.OP_LESS_EQUAL -> append("jle $target")
      else -> throw Exception("Unsupported comparison operator")
    }
  }

  enum class ExprContext {
    NORMAL,
    CONDITIONAL,
    LOOP
  }
}
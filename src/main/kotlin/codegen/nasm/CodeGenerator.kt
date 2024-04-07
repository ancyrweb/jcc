package fr.ancyr.jcc.codegen.nasm

import fr.ancyr.jcc.ast.nodes.*
import fr.ancyr.jcc.lex.TokenType

class CodeGenerator(private val nodes: List<Node>) {
  private val code = StringBuilder()
  private lateinit var allocator: MemoryAllocator
  private lateinit var currentFunction: FunctionNode
  private val labelAllocator = LabelAllocator()
  private val optimize = true

  private fun appendNoTab(str: String) {
    code.append("$str\n")
  }

  private fun append(str: String) {
    code.append("\t$str\n")
  }

  private fun appendLabel(str: String) {
    code.append("$str:\n")
  }

  fun generate(): String {
    code.clear()

    appendNoTab("SECTION .text")
    generateGlobals()
    generateCode()

    return code.toString()
  }

  private fun generateGlobals() {
    for (node in nodes) {
      if (node is FunctionNode) {
        appendNoTab("global ${node.identifier}")
      }
    }
  }

  private fun generateCode() {
    for (node in nodes) {
      if (node is FunctionNode) {
        generateFunction(node)
      }
    }
  }

  private fun generateBlock(block: BlockNode) {
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
        if (node.expr != null) {
          val destSize =
            ByteSize.fromType(currentFunction.returnType);

          genExpr(node.expr, destSize)
        }
        // The epilogue is written in the function generation
      }

      else -> {
        return
      }
    }
  }

  private fun generateFunction(fn: FunctionNode) {
    if (fn.block == null) {
      return
    }

    currentFunction = fn
    allocator = MemoryAllocator(fn)

    appendNoTab("${fn.identifier}:")
    append("; prologue")
    append("push rbp")
    append("mov rbp, rsp\n")

    // Note : this might not be required on System V AMD64 ABI
    // See https://en.wikipedia.org/wiki/Red_zone_(computing)
    append("sub rsp, ${allocator.stackSize}")

    for (child in fn.block.statements) {
      generateNode(child)
    }

    append("; epilogue")
    // This too might not be needed
    append("add rsp, ${allocator.stackSize}")
    append("pop rbp")
    append("ret")
  }

  private fun genVarDecl(node: VariableDeclarationNode) {
    val location = allocator.getLocationOrFail(node.identifier)
    val src = getRegisterForSize(size = location.size())

    if (node.value != null) {
      if (optimize) {
        if (node.value is ConstantExpr) {
          append("mov ${location.asString()}, ${node.value.value}")
          return
        }
      }

      genExpr(node.value)
    }

    append("mov ${location.asString()}, $src")
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
          val dest = getRegisterForSize(size = destSize)
          append("movsx $dest, ${location.asString()}")
        } else if (destSize.toInt() < location.size().toInt()) {
          // The destination is smaller than the location
          // So we use the size of the source as a reference
          val dest = getRegisterForSize(size = location.size())
          append("mov $dest, ${location.asString()}")
        } else {
          // They have the same size
          val dest = getRegisterForSize(size = destSize)
          append("mov $dest, ${location.asString()}")
        }
      }

      is BinOpExpr -> {
        genExpr(expr.right)
        append("push rax")
        genExpr(expr.left)
        append("pop rbx")

        generateBinOp(expr.op, "rbx", "rax")
      }

      is ComparisonExpr -> {
        if (optimize) {
          if (expr.left is IdentifierExpr && expr.right is ConstantExpr) {
            val location = allocator.getLocationOrFail(expr.left.name)
            val dest = getRegisterForSize(size = location.size())

            append("cmp ${location.asString()}, ${expr.right.value}")
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
        val dest = getRegisterForSize(size = location.size())

        if (optimize) {
          if (expr.right is ConstantExpr) {
            append("mov ${location.asString()}, ${expr.right.value}")
            return
          }
        }

        genExpr(expr.right)
        append("mov ${location.asString()}, $dest")
      }

      else -> {
        println("Unrecognized expression type")
        println(expr)
        return
      }
    }
  }

  private fun generateBinOp(type: TokenType, source: String, dest: String) {
    when (type) {
      TokenType.OP_PLUS -> append("add $dest, $source")
      TokenType.OP_MINUS -> append("sub $dest, $source")
      TokenType.OP_MUL -> append("imul $dest, $source")
      else -> return
    }
  }

  private fun genIf(node: IfNode) {
    val endLabel = labelAllocator.nextLabel()

    var cur: IfNode? = node

    while (cur != null) {
      val bodyLabel = labelAllocator.nextLabel()
      val outLabel: String? =
        if (cur.elseBlock != null || cur.elseIf != null) labelAllocator.nextLabel() else null

      generateComparisons(
        cur.condition,
        outLabel ?: endLabel,
        bodyLabel,
        context = ExprContext.CONDITIONAL
      )

      appendLabel(bodyLabel)
      generateBlock(cur.thenBlock)
      append("jmp $endLabel\n")

      if (cur.elseIf != null) {
        appendLabel(outLabel!!)
        cur = cur.elseIf
      } else if (cur.elseBlock != null) {
        appendLabel(outLabel!!)
        generateBlock(cur.elseBlock!!)
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
    generateComparisons(
      node.condition,
      endLabel,
      bodyLabel,
      context = ExprContext.LOOP
    )

    appendLabel(bodyLabel)
    generateBlock(node.block)
    append("jmp $startLabel\n")
    appendLabel(endLabel)
  }

  private fun generateComparisons(
    condition: Expr,
    outLabel: String,
    bodyLabel: String,
    isInAnd: Boolean = false,
    depth: Int = 0,
    context: ExprContext
  ) {

    when (condition) {
      is OrExpr -> {
        generateComparisons(
          condition.left,
          outLabel,
          bodyLabel,
          depth = depth + 1,
          context = context
        )
        generateComparisons(
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
        generateComparisons(
          condition.left,
          outLabel,
          bodyLabel,
          true,
          depth = depth + 1,
          context = context
        )
        generateComparisons(
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
          generateInvertedJump(condition.op, outLabel)
        } else {
          // Otherwise, we jump to the body label if the condition is true
          generateJump(condition.op, bodyLabel)

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

  private fun getRegisterForSize(
    r: String = "a",
    size: ByteSize = ByteSize.QWORD
  ): String {
    return when (size) {
      ByteSize.BYTE -> "${r}l"
      ByteSize.WORD -> "${r}x"
      ByteSize.DWORD -> "e${r}x"
      ByteSize.QWORD -> "r${r}x"
    }
  }

  private fun generateInvertedJump(op: TokenType, target: String) {
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

  private fun generateJump(op: TokenType, target: String) {
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
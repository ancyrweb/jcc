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
        generateVariableDeclaration(node)
      }

      is ExprStatement -> {
        generateExpr(node.expr)
      }

      is IfNode -> {
        generateConditional(node)
      }

      is ReturnNode -> {
        if (node.expr != null) {
          val destSize =
            ByteSize.fromType(currentFunction.returnType);

          generateExpr(node.expr, destSize)
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

  private fun generateVariableDeclaration(node: VariableDeclarationNode) {
    val location = allocator.getLocationOrFail(node.identifier)
    val src = getRegisterForSize(size = location.size())

    if (node.value != null) {
      if (optimize) {
        if (node.value is ConstantExpr) {
          append("mov ${location.asString()}, ${node.value.value}")
          return
        }
      }

      generateExpr(node.value)
    }

    append("mov ${location.asString()}, $src")
  }

  private fun generateExpr(
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
        generateExpr(expr.right)
        append("push rax")
        generateExpr(expr.left)
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

        generateExpr(expr.right)
        append("push rax")
        generateExpr(expr.left)
        append("pop rbx")

        if (context == ExprContext.CONDITIONAL) {
          append("cmp rax, rbx")
          return
        } else {
          // TODO Handle setcc
        }
      }

      is GroupExpr -> {
        generateExpr(expr.expr)
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

        generateExpr(expr.right)
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

  private fun generateConditional(node: IfNode) {
    val thenLabel = labelAllocator.nextLabel()
    val elseLabel = node.elseBlock?.let { labelAllocator.nextLabel() }
    val endLabel = labelAllocator.nextLabel()

    if (node.condition is ComparisonExpr) {
      generateExpr(
        node.condition,
        context = ExprContext.CONDITIONAL
      )

      val targetLabel = elseLabel ?: endLabel

      when (node.condition.op) {
        TokenType.OP_EQUAL_EQUAL -> append("jne $targetLabel")
        TokenType.OP_NOT_EQUAL -> append("je $targetLabel")
        TokenType.OP_GREATER -> append("jle $targetLabel")
        TokenType.OP_GREATER_EQUAL -> append("jl $targetLabel")
        TokenType.OP_LESS -> append("jge $targetLabel")
        TokenType.OP_LESS_EQUAL -> append("jg $targetLabel")
        else -> throw Exception("Unsupported comparison operator")
      }
    }

    appendLabel(thenLabel)
    generateBlock(node.thenBlock)

    if (node.elseBlock != null) {
      // Terminate the if statement
      append("jmp $endLabel")

      // Then work on the else statement
      appendLabel(elseLabel!!)
      generateBlock(node.elseBlock!!)
    }

    appendLabel(endLabel)

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

  enum class ExprContext {
    NORMAL,
    CONDITIONAL;
  }
}
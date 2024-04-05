package fr.ancyr.jcc.codegen.nasm

import fr.ancyr.jcc.ast.nodes.*
import fr.ancyr.jcc.lex.TokenType

class CodeGenerator(private val nodes: List<Node>) {
  private val code = StringBuilder()
  private lateinit var allocator: MemoryAllocator
  private lateinit var currentFunction: FunctionNode

  private fun appendNoTab(str: String) {
    code.append("$str\n")
  }

  private fun append(str: String) {
    code.append("\t$str\n")
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
        appendNoTab("global ${node.identifier.asString()}")
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

  private fun generateNode(node: Node) {
    when (node) {
      is VariableDeclarationNode -> {
        val location = allocator.getLocationOrFail(node.identifier.asString())

        if (node.value != null) {
          generateExpr(node.value)
        }

        val src = getRegisterForSize(size = location.size())

        append("mov ${location.asString()}, $src")
      }

      is ExprStatement -> {
        generateExpr(node.expr)
      }

      is ReturnNode -> {
        if (node.expr != null) {
          val destSize =
            MemoryAllocator.ByteSize.fromType(currentFunction.returnType.type);

          generateExpr(node.expr, destSize)
        }
        // The epilogue is written in the function generation
      }

      else -> {
        return
      }
    }

    append("")
  }

  private fun generateFunction(fn: FunctionNode) {
    if (fn.block == null) {
      return
    }

    currentFunction = fn
    allocator = MemoryAllocator(fn)

    appendNoTab("${fn.identifier.asString()}:")
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

  private fun generateExpr(
    expr: Expr,
    destSize: MemoryAllocator.ByteSize = MemoryAllocator.ByteSize.QWORD
  ) {
    when (expr) {
      is ConstantExpr -> {
        val dest = getRegisterForSize(size = destSize)
        append("mov $dest, ${expr.token.value}")
      }

      is IdentifierExpr -> {
        val location = allocator.getLocationOrFail(expr.token.asString())

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

        generateOp(expr.op.type, "rbx", "rax")
      }

      is GroupExpr -> {
        generateExpr(expr.expr)
      }

      is AssignOpExpr -> {
        if (expr.left !is IdentifierExpr) {
          println("Left side of assignment must be an identifier")
          return
        }

        val identifier = expr.left.token.asString()
        val location = allocator.getLocationOrFail(identifier)
        generateExpr(expr.right)
        append("mov ${location.asString()}, rax")
      }

      else -> {
        println("Unrecognized expression type")
        println(expr)
        return
      }
    }
  }

  private fun generateOp(type: TokenType, source: String, dest: String) {
    when (type) {
      TokenType.OP_PLUS -> append("add $dest, $source")
      TokenType.OP_MINUS -> append("sub $dest, $source")
      TokenType.OP_MUL -> append("imul $dest, $source")
      else -> return
    }
  }

  private fun getRegisterForSize(
    r: String = "a",
    size: MemoryAllocator.ByteSize = MemoryAllocator.ByteSize.QWORD
  ): String {
    return when (size) {
      MemoryAllocator.ByteSize.BYTE -> "${r}l"
      MemoryAllocator.ByteSize.WORD -> "${r}x"
      MemoryAllocator.ByteSize.DWORD -> "e${r}x"
      MemoryAllocator.ByteSize.QWORD -> "r${r}x"
    }
  }

}
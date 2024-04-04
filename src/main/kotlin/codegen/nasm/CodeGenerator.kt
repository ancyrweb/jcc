package fr.ancyr.jcc.codegen.nasm

import fr.ancyr.jcc.ast.nodes.*
import fr.ancyr.jcc.lex.TokenType

class CodeGenerator(private val nodes: List<Node>) {
  private val code = StringBuilder()
  private lateinit var allocator: MemoryAllocator

  private fun appendNoTab(str: String) {
    code.append("$str\n")
  }

  private fun append(str: String) {
    code.append("\t$str\n")
  }

  fun generate(): String {
    code.clear()


    append("")
    appendNoTab("SECTION .text")
    generateGlobals()
    generateStart()
    generateCode()

    return code.toString()
  }

  private fun generateGlobals() {
    appendNoTab("global _start")

    for (node in nodes) {
      if (node is FunctionNode) {
        appendNoTab("global ${node.identifier.asString()}")
      }
    }
  }

  private fun generateStart() {
    appendNoTab("_start:")
    append("call main\n")
    append("mov rdi, rax")
    append("mov rax, 60")
    append("syscall")
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

        append("mov ${location.asString()}, rax")
      }

      is ExprStatement -> {
        generateExpr(node.expr)
      }

      is ReturnNode -> {
        if (node.expr != null) {
          generateExpr(node.expr)
        }

        // The epilogue is written in the function generation
      }

      else -> return
    }

    append("")
  }

  private fun generateFunction(fn: FunctionNode) {
    if (fn.block == null) {
      return
    }

    appendNoTab("${fn.identifier.asString()}:")
    append("; prologue")
    append("push rbp")
    append("mov rbp, rsp\n")

    allocator = MemoryAllocator(fn)

    for (child in fn.block.statements) {
      generateNode(child)
    }

    append("; epilogue")
    append("pop rbp")
    append("ret")
  }

  private fun generateExpr(expr: Expr, dest: String = "rax") {
    when (expr) {
      is ConstantExpr -> {
        append("mov $dest, ${expr.token.value}")
      }

      is IdentifierExpr -> {
        val location = allocator.getLocationOrFail(expr.token.asString())
        append("mov $dest, ${location.asString()}")
      }

      is BinOpExpr -> {
        if (expr.right is BinOpExpr) {
          generateExpr(expr.right, "rcx")
          generateExpr(expr.left, "rax")
          generateOp(expr.op.type, "rcx", "rax")
        } else {
          generateExpr(expr.left, "rax")
          generateExpr(expr.right, "rcx")
          generateOp(expr.op.type, "rcx", "rax")
          append("mov $dest, rax\n")
        }
      }

      else -> {
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

}
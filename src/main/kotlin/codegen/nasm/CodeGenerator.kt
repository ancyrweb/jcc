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

    allocator = MemoryAllocator(fn)

    appendNoTab("${fn.identifier.asString()}:")
    append("; prologue")
    append("push rbp")
    append("mov rbp, rsp\n")
    append("sub rsp, ${allocator.stackSize}")
    
    for (child in fn.block.statements) {
      generateNode(child)
    }

    append("; epilogue")
    append("add rsp, ${allocator.stackSize}")
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

}
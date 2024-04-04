package fr.ancyr.jcc.codegen.nasm

import fr.ancyr.jcc.ast.nodes.FunctionNode
import fr.ancyr.jcc.ast.nodes.VariableDeclarationNode
import fr.ancyr.jcc.lex.TokenType

class MemoryAllocator(fn: FunctionNode) {
  private val locations: Map<String, Location>;

  init {
    var tempStackSize = 0
    val tempLocations = mutableMapOf<String, Location>()

    for (node in fn.block!!.statements) {
      if (node is VariableDeclarationNode) {
        val size = byteSize(node.type.type)
        tempStackSize += size

        val location = MemoryLocation(tempStackSize, size)
        tempLocations[node.identifier.asString()] = location
      }
    }

    locations = tempLocations
  }

  fun getLocationOrFail(identifier: String): Location {
    return locations[identifier]
      ?: throw Exception("Location not found for $identifier")
  }

  private fun byteSize(type: TokenType): Int {
    return when (type) {
      TokenType.TYPE_INT -> 4
      TokenType.TYPE_CHAR -> 1
      TokenType.TYPE_FLOAT -> 4
      TokenType.TYPE_DOUBLE -> 8
      TokenType.TYPE_LONG -> 8
      TokenType.TYPE_SHORT -> 2
      TokenType.TYPE_LONG_LONG -> 8
      TokenType.TYPE_LONG_DOUBLE -> 16
      else -> 0
    }
  }

  abstract class Location {
    abstract fun asString(): String
  }

  data class MemoryLocation(private val offset: Int, private val bytes: Int) :
    Location() {
    override fun asString(): String {
      val qt = when (bytes) {
        1 -> "byte"
        2 -> "word"
        4 -> "dword"
        8 -> "qword"
        else -> "qword"
      }

      return "$qt [rbp - $offset]"
    }
  }
}
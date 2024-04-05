package fr.ancyr.jcc.codegen.nasm

import fr.ancyr.jcc.ast.nodes.FunctionNode
import fr.ancyr.jcc.ast.nodes.VariableDeclarationNode
import fr.ancyr.jcc.lex.TokenType
import java.util.*

class MemoryAllocator(fn: FunctionNode) {
  val stackSize: Int
  private val locations: Map<String, Location>;

  init {
    var tempStackSize = 0
    val tempLocations = mutableMapOf<String, Location>()

    for (node in fn.block!!.statements) {
      if (node is VariableDeclarationNode) {
        val size = byteSize(node.type.type)
        tempStackSize += size

        val location = MemoryLocation(tempStackSize, ByteSize.fromInt(size))
        tempLocations[node.identifier.asString()] = location
      }
    }

    stackSize = tempStackSize
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
    abstract fun size(): ByteSize
  }

  data class MemoryLocation(val offset: Int, val bytes: ByteSize) :
    Location() {
    override fun asString(): String {
      val qt = bytes.toString().lowercase(Locale.getDefault())
      return "$qt [rbp - $offset]"
    }

    override fun size(): ByteSize {
      return bytes
    }
  }

  enum class ByteSize {
    BYTE, WORD, DWORD, QWORD;

    fun toInt(): Int {
      return when (this) {
        BYTE -> 1
        WORD -> 2
        DWORD -> 4
        QWORD -> 8
      }
    }

    companion object {
      fun fromInt(value: Int): ByteSize {
        return when (value) {
          1 -> BYTE
          2 -> WORD
          4 -> DWORD
          8 -> QWORD
          else -> throw Exception("Invalid byte size")
        }
      }

      fun fromType(type: TokenType): ByteSize {
        return when (type) {
          TokenType.TYPE_INT -> DWORD
          TokenType.TYPE_CHAR -> BYTE
          TokenType.TYPE_FLOAT -> DWORD
          TokenType.TYPE_DOUBLE -> QWORD
          TokenType.TYPE_LONG -> QWORD
          TokenType.TYPE_SHORT -> WORD
          TokenType.TYPE_LONG_LONG -> QWORD
          TokenType.TYPE_LONG_DOUBLE -> QWORD
          else -> throw Exception("Invalid byte size")
        }
      }
    }
  }
}
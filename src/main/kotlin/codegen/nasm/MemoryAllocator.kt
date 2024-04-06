package fr.ancyr.jcc.codegen.nasm

import fr.ancyr.jcc.ast.nodes.FunctionNode
import fr.ancyr.jcc.ast.nodes.VariableDeclarationNode
import java.util.*

class MemoryAllocator(fn: FunctionNode) {
  val stackSize: Int
  private val locations: Map<String, Location>;

  init {
    var tempStackSize = 0
    val tempLocations = mutableMapOf<String, Location>()

    for (node in fn.block!!.statements) {
      if (node is VariableDeclarationNode) {
        val size = ByteSize.fromType(node.type).toInt()
        tempStackSize += size

        val location = MemoryLocation(tempStackSize, ByteSize.fromInt(size))
        tempLocations[node.identifier] = location
      }
    }

    stackSize = tempStackSize
    locations = tempLocations
  }

  fun getLocationOrFail(identifier: String): Location {
    return locations[identifier]
      ?: throw Exception("Location not found for $identifier")
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
}
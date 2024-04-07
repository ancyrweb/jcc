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
        val size = ByteSize.fromType(node.typedSymbol).toInt()
        tempStackSize += size

        val location = MemoryLocation(tempStackSize, ByteSize.fromInt(size))
        tempLocations[node.typedSymbol.identifier] = location
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
    abstract fun sizedLocation(): String
    abstract fun location(): String
    abstract fun size(): ByteSize
  }

  data class MemoryLocation(val offset: Int, val bytes: ByteSize) :
    Location() {
    override fun sizedLocation(): String {
      val qt = bytes.toString().lowercase(Locale.getDefault())
      return "$qt ${location()}"
    }

    override fun location(): String {
      return "[rbp - $offset]"
    }

    override fun size(): ByteSize {
      return bytes
    }
  }
}
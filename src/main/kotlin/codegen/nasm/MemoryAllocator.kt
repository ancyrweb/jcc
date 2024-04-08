package fr.ancyr.jcc.codegen.nasm

import fr.ancyr.jcc.ast.nodes.FunctionNode
import fr.ancyr.jcc.ast.nodes.VariableDeclarationNode
import java.util.*

class MemoryAllocator(fn: FunctionNode) {
  val stackSize: Int
  private val locations: Map<String, Location>

  init {
    var tempStackSize = 0
    val tempLocations = mutableMapOf<String, Location>()

    var availableRegisters = 6
    var argumentStartOffset = 16

    // Every actual parameter will be moved to the stack after the function prologue
    for (parameter in fn.parameters) {
      val size = ByteSize.fromType(parameter)
      if (size.toInt() <= 2) {
        throw Exception("Unsupported parameter size on the stack")
      }


      if (availableRegisters == 0) {
        tempLocations[parameter.identifier] =
          MemoryLocation(argumentStartOffset, size)

        // Subtle note : the elements pushed onto the stacks
        // for function invocation are always the 64-bit values
        // of the register, so each parameter always take up 8 bytes
        // however if we insert a 32-bit value or a 16-bit value we may
        // have garbage data into the first 4 bytes or 6 bytes
        // TODO : handle the code to manipulate 2-byte and 1-byte data
        argumentStartOffset += 8
      } else {
        tempStackSize += size.toInt()
        tempLocations[parameter.identifier] =
          MemoryLocation(-tempStackSize, size)
        availableRegisters--
      }

    }

    for (node in fn.block!!.statements) {
      if (node is VariableDeclarationNode) {
        val size = ByteSize.fromType(node.typedSymbol)
        tempStackSize += size.toInt()
        tempLocations[node.typedSymbol.identifier] =
          MemoryLocation(-tempStackSize, size)
      }
    }

    stackSize = tempStackSize
    locations = tempLocations
  }

  fun getLocationOrFail(identifier: String): Location {
    return locations[identifier]
      ?: throw Exception("Location not found for $identifier")
  }

  fun moveParametersToStack() {

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
      return if (offset > 0) {
        "[rbp + $offset]"
      } else if (offset < 0) {
        "[rbp - ${-offset}]"
      } else {
        "[rbp]"
      }
    }

    override fun size(): ByteSize {
      return bytes
    }
  }
}
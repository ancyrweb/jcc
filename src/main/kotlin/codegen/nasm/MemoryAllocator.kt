package fr.ancyr.jcc.codegen.nasm

import fr.ancyr.jcc.ast.nodes.FunctionBody
import fr.ancyr.jcc.ast.nodes.VariableDeclarationNode

class MemoryAllocator(fn: FunctionBody) {
  val stackSize: Int
  private val locations: Map<String, StackLocation>

  init {
    var tempStackSize = 0
    val tempLocations = mutableMapOf<String, StackLocation>()

    var availableRegisters = 6
    var argumentStartOffset = 16

    // Every actual parameter will be moved to the stack after the function prologue
    for (parameter in fn.parameters) {
      val size = ByteSize.fromType(parameter)
      if (availableRegisters == 0) {
        tempLocations[parameter.identifier] =
          StackLocation(argumentStartOffset, size)

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
          StackLocation(-tempStackSize, size)
        availableRegisters--
      }

    }

    // Not the most efficient, but OK for now
    if (tempStackSize % 16 != 0) {
      val padding = 16 - (tempStackSize % 16)
      tempStackSize += padding
    }

    for (node in fn.block!!.statements) {
      if (node is VariableDeclarationNode) {
        val size = ByteSize.fromType(node.typedSymbol)
        tempStackSize += size.toInt()
        tempLocations[node.typedSymbol.identifier] =
          StackLocation(-tempStackSize, size)
      }
    }

    stackSize = tempStackSize
    locations = tempLocations
  }

  fun getLocationOrFail(identifier: String): StackLocation {
    return locations[identifier]
      ?: throw Exception("Location not found for $identifier")
  }
}
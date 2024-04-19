package fr.ancyr.jcc.codegen.nasm_ir

import fr.ancyr.jcc.ast.nodes.TypedSymbol
import fr.ancyr.jcc.ast.sem.Symbol
import fr.ancyr.jcc.ast.sem.SymbolType
import fr.ancyr.jcc.ir.IRFunction
import fr.ancyr.jcc.ir.nodes.expr.*
import fr.ancyr.jcc.ir.nodes.stmt.IRMove
import fr.ancyr.jcc.ir.nodes.stmt.IRNoop
import fr.ancyr.jcc.ir.nodes.stmt.IRReturn

class Allocator(fn: IRFunction) {
  companion object {
    val registers = listOf(
      Register.rcx,
      Register.rdx,
      Register.rsi,
      Register.rdi,
      Register.r8,
      Register.r9,
      Register.r10,
      Register.r11,
    )

    val argumentRegisters = listOf(
      Register.rdi,
      Register.rsi,
      Register.rdx,
      Register.rcx,
      Register.r8,
      Register.r9,
    )

    fun getSize(sym: TypedSymbol): Int {
      if (sym.pointer) {
        return 8
      }

      return when (sym.type) {
        SymbolType.INT -> 4
        SymbolType.CHAR -> 1
        SymbolType.SHORT -> 2
        SymbolType.LONG -> 8
        SymbolType.FLOAT -> 4
        SymbolType.DOUBLE -> 8
      }
    }

    fun getSize(sym: Symbol.Variable): Int {
      if (sym.pointer) {
        return 8
      }

      return when (sym.type) {
        SymbolType.INT -> 4
        SymbolType.CHAR -> 1
        SymbolType.SHORT -> 2
        SymbolType.LONG -> 8
        SymbolType.FLOAT -> 4
        SymbolType.DOUBLE -> 8
      }
    }

  }

  val tempBindings: Map<String, Register>
  val varBindings: Map<String, Variable>
  val stackSize: Int


  init {
    // First allocate temporaries
    val liveness = buildLiveness(fn)
    val groups = buildGroups(liveness)
    this.tempBindings = buildBindings(groups, liveness)

    // Then allocate arguments & local variables
    val varBindings = mutableMapOf<String, Variable>()
    var tempStackSize = 0
    var argumentStartOffset = 16
    var registerIndex = 0

    // Every actual parameter will be moved to the stack after the function prologue
    for (parameter in fn.parameters) {
      val size = getSize(parameter)
      if (registerIndex == argumentRegisters.size) {
        varBindings[parameter.identifier] =
          Variable(argumentStartOffset, size)

        // Subtle note : the elements pushed onto the stacks
        // for function invocation are always the 64-bit values
        // of the register, so each parameter always take up 8 bytes
        // however if we insert a 32-bit value or a 16-bit value we may
        // have garbage data into the first 4 bytes or 6 bytes
        // TODO : handle the code to manipulate 2-byte and 1-byte data
        argumentStartOffset += 8
      } else {
        tempStackSize += size
        varBindings[parameter.identifier] =
          Variable(-tempStackSize, size)
        registerIndex--
      }
    }

    // Alignment
    // Not the most efficient, but OK for now
    if (tempStackSize % 16 != 0) {
      val padding = 16 - (tempStackSize % 16)
      tempStackSize += padding
    }

    for (node in fn.scope.symbolTable) {
      val variable = node.value as Symbol.Variable;
      val size = getSize(variable)

      tempStackSize += size
      varBindings[variable.name] = Variable(-tempStackSize, size)
    }

    this.varBindings = varBindings
    this.stackSize = tempStackSize
  }

  /**
   * Build liveness information for each variable
   * Since we're using an IR with temporary variables
   * We can know for sure each temporary is used at most twice
   * Meaning we only need to know when the temps are used, and don't
   * need to keep track of reuses
   *
   * Note : all variables are stored on the stack for simplicity
   */
  private fun buildLiveness(fn: IRFunction): Map<String, List<Int>> {
    val liveness = mutableMapOf<String, List<Int>>()

    fun live(name: String, index: Int) {
      if (!liveness.containsKey(name)) {
        liveness[name] = listOf(index)
      } else {
        liveness[name] = liveness[name]!! + index
      }
    }

    for (i in 0..<fn.nodes.size) {
      when (val node = fn.nodes[i]) {
        is IRMove -> {

          if (node.dest is IRTemp) {
            live(node.dest.name, i)
          }

          when (node.src) {
            is IRBinop -> {
              if (node.src.left is IRTemp) {
                live(node.src.left.name, i)
              }
              if (node.src.right is IRTemp) {
                live(node.src.right.name, i)
              }
            }

            is IRDereference -> {
              if (node.src.node is IRTemp) {
                live(node.src.node.name, i)
              }
            }

            is IRAddress -> {
              if (node.src.node is IRTemp) {
                live(node.src.node.name, i)
              }
            }

            is IRTemp -> {
              live(node.src.name, i)
            }

            is IRCall -> {
              for (arg in node.src.arguments) {
                if (arg is IRTemp) {
                  live(arg.name, i)
                }
              }
            }

            is IRVar, is IRConst -> {}
            else -> {
              throw RuntimeException("Unrecognized node: $node")
            }
          }

        }

        is IRNoop -> {}
        is IRReturn -> {
          if (node.sym is IRTemp) {
            live(node.sym.name, i)
          }
        }

        else -> {
          throw RuntimeException("Unrecognized node: $node")
        }
      }
    }

    return liveness
  }

  private fun buildGroups(liveness: Map<String, List<Int>>): List<Set<String>> {
    val allKeys = liveness.keys.toMutableList()
    val groups = mutableListOf<Set<String>>()

    while (allKeys.isNotEmpty()) {
      var last = allKeys.removeFirst()
      val set = mutableSetOf<String>(last)

      for (key in liveness.keys) {
        if (allKeys.contains(key) && liveness[key]!!.first() > liveness[last]!!.last()) {
          allKeys.remove(key)
          last = key
          set.add(key)
        }
      }

      groups.add(set)
    }

    return groups
  }

  private fun buildBindings(
    groups: List<Set<String>>, liveness:
    Map<String, List<Int>>
  ): Map<String, Register> {
    val binding = mutableMapOf<String, Register>()
    var regIndex = 0

    for (group in groups) {
      for (name in group) {
        binding[name] = registers[regIndex]
      }

      regIndex++

      if (regIndex >= registers.size) {
        throw RuntimeException("Not enough registers, spilling not supported yet")
      }
    }

    // Sort by key for easier debugging
    return binding.toSortedMap(
      compareBy { liveness[it]!!.first() }
    )
  }
}
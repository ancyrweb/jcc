package fr.ancyr.jcc.codegen.nasm_ir

import fr.ancyr.jcc.ir.IRFunction
import fr.ancyr.jcc.ir.nodes.expr.*
import fr.ancyr.jcc.ir.nodes.stmt.IRMove
import fr.ancyr.jcc.ir.nodes.stmt.IRNoop

class Allocator(fn: IRFunction) {
  private val registers = listOf(
    Register.rcx,
    Register.rdx,
    Register.rsi,
    Register.rdi,
    Register.r8,
    Register.r9,
    Register.r10,
    Register.r11,
  )

  private val bindings: Map<String, Register>

  init {
    // First allocate temporaries
    val liveness = buildLiveness(fn)
    val groups = buildGroups(liveness)
    this.bindings = buildBindings(groups, liveness)

    // Then allocate arguments & local variables
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

            is IRConst -> {}
            is IRCall -> {
              for (arg in node.src.arguments) {
                if (arg is IRTemp) {
                  live(arg.name, i)
                }
              }
            }

            else -> {
              throw RuntimeException("Unrecognized node: $node")
            }
          }

        }

        is IRNoop -> {}
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
        throw RuntimeException("Not enough registers, spilling not supported")
      }
    }

    // Sort by key for easier debugging
    return binding.toSortedMap(
      compareBy { liveness[it]!!.first() }
    )
  }
}
package fr.ancyr.jcc.ir

import java.util.*

class RegisterAllocator {
  private val realRegisters = Stack<String>()
  private val tempMap = mutableMapOf<String, String>()

  init {
    realRegisters.push("eax")
    realRegisters.push("ebx")
    realRegisters.push("ecx")
    realRegisters.push("edx")
    realRegisters.push("esi")
    realRegisters.push("edi")
    realRegisters.push("r8")
    realRegisters.push("r9")
    realRegisters.push("r10")
    realRegisters.push("r11")
    realRegisters.push("r12")
    realRegisters.push("r13")
    realRegisters.push("r14")
    realRegisters.push("r15")
  }

  fun allocate(label: String): String {
    if (realRegisters.isEmpty()) {
      throw IllegalStateException("No more registers available")
    }

    val out = realRegisters.pop()
    tempMap[label] = out

    return out
  }

  fun free(label: String) {
    val register = tempMap[label] ?: throw IllegalArgumentException("Register not found")
    realRegisters.push(register)
    tempMap.remove(label)
  }

  fun getRegister(label: String): String {
    return tempMap[label] ?: throw IllegalArgumentException("Register not found")
  }
}
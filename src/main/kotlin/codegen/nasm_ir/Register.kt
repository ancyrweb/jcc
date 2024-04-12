package fr.ancyr.jcc.codegen.nasm_ir

class Register(val variants: List<Variant>) {
  companion object {
    val rax = Register(
      listOf(
        Variant("rax", 8),
        Variant("eax", 4),
        Variant("ax", 2),
        Variant("al", 1),
      )
    )

    val rbx = Register(
      listOf(
        Variant("rbx", 8),
        Variant("ebx", 4),
        Variant("bx", 2),
        Variant("bl", 1),
      )
    )

    val rcx = Register(
      listOf(
        Variant("rcx", 8),
        Variant("ecx", 4),
        Variant("cx", 2),
        Variant("cl", 1),
      )
    )

    val rdx = Register(
      listOf(
        Variant("rdx", 8),
        Variant("edx", 4),
        Variant("dx", 2),
        Variant("dl", 1),
      )
    )

    val rsi = Register(
      listOf(
        Variant("rsi", 8),
        Variant("esi", 4),
        Variant("si", 2),
        Variant("sil", 1),
      )
    )

    val rdi = Register(
      listOf(
        Variant("rdi", 8),
        Variant("edi", 4),
        Variant("di", 2),
        Variant("dil", 1),
      )
    )

    val r8 = Register(
      listOf(
        Variant("r8", 8),
        Variant("r8d", 4),
        Variant("r8w", 2),
        Variant("r8b", 1),
      )
    )

    val r9 = Register(
      listOf(
        Variant("r9", 8),
        Variant("r9d", 4),
        Variant("r9w", 2),
        Variant("r9b", 1),
      )
    )

    val r10 = Register(
      listOf(
        Variant("r10", 8),
        Variant("r10d", 4),
        Variant("r10w", 2),
        Variant("r10b", 1),
      )
    )

    val r11 = Register(
      listOf(
        Variant("r11", 8),
        Variant("r11d", 4),
        Variant("r11w", 2),
        Variant("r11b", 1),
      )
    )

    val r12 = Register(
      listOf(
        Variant("r12", 8),
        Variant("r12d", 4),
        Variant("r12w", 2),
        Variant("r12b", 1),
      )
    )

    val r13 = Register(
      listOf(
        Variant("r13", 8),
        Variant("r13d", 4),
        Variant("r13w", 2),
        Variant("r13b", 1),
      )
    )

    val r14 = Register(
      listOf(
        Variant("r14", 8),
        Variant("r14d", 4),
        Variant("r14w", 2),
        Variant("r14b", 1),
      )
    )

    val r15 = Register(
      listOf(
        Variant("r15", 8),
        Variant("r15d", 4),
        Variant("r15w", 2),
        Variant("r15b", 1),
      )
    )
  }

  data class Variant(val name: String, val byteSize: Int) {}

  override fun toString(): String {
    return "Reg(${variants[0].name})"
  }
}
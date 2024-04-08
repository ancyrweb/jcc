package fr.ancyr.jcc.codegen.nasm

enum class Register(
  private val name64bits: String,
  private val name32bits: String,
  private val name16bits: String,
  private val name8bits: String
) {
  RAX("rax", "eax", "ax", "al"),
  RBX("rbx", "ebx", "bx", "bl"),
  RCX("rcx", "ecx", "cx", "cl"),
  RDX("rdx", "edx", "dx", "dl"),
  RSI("rsi", "esi", "si", "sil"),
  RDI("rdi", "edi", "di", "dil"),
  RBP("rbp", "ebp", "bp", "bpl"),
  RSP("rsp", "esp", "sp", "spl"),
  R8("r8", "r8d", "r8w", "r8b"),
  R9("r9", "r9d", "r9w", "r9b"),
  R10("r10", "r10d", "r10w", "r10b"),
  R11("r11", "r11d", "r11w", "r11b"),
  R12("r12", "r12d", "r12w", "r12b"),
  R13("r13", "r13d", "r13w", "r13b"),
  R14("r14", "r14d", "r14w", "r14b"),
  R15("r15", "r15d", "r15w", "r15b");

  fun name(size: ByteSize): String {
    return when (size) {
      ByteSize.QWORD -> name64bits
      ByteSize.DWORD -> name32bits
      ByteSize.WORD -> name16bits
      ByteSize.BYTE -> name8bits
    }
  }
}
package fr.ancyr.jcc.ir.nodes.literal

data class IRIntLiteral (val value: Long, val bits: IRBits) : IRLiteral() {
  override fun toString(): String {
    return value.toString()
  }

  companion object {
    fun bits8(value: Long): IRIntLiteral {
      return IRIntLiteral(value, IRBits.IR8Bits)
    }

    fun bits32(value: Long): IRIntLiteral {
      return IRIntLiteral(value, IRBits.IR32Bits)
    }

    fun bits64(value: Long): IRIntLiteral {
      return IRIntLiteral(value, IRBits.IR64Bits)
    }
  }
}
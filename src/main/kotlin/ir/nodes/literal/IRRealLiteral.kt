package fr.ancyr.jcc.ir.nodes.literal

data class IRRealLiteral(val value: Double, val bits: IRBits) : IRLiteral() {
  init {
    require(bits == IRBits.IR32Bits || bits == IRBits.IR64Bits) {
      "Invalid bits for real literal: $bits"
    }
  }

  override fun toString(): String {
    return "IRRealLiteral($value, $bits)"
  }
}
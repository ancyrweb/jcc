package fr.ancyr.jcc.ir.translator

class TempAllocator {
  private var counter = 0

  fun allocate(): Temp {
    val temp = "t$counter"
    counter++
    return Temp(temp)
  }

  data class Temp(val name: String)
}
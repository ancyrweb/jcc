package fr.ancyr.jcc.ir

class TempGenerator {
  private var counter = 0

  fun next(): String {
    return "t(${counter++})"
  }
}
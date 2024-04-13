package fr.ancyr.jcc.codegen.shared

class LabelAllocator {
  private var counter = 0

  fun nextLabel(): String {
    return "L${counter++}"
  }
}
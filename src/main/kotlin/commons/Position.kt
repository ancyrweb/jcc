package fr.ancyr.jcc.commons

data class Position(
  var line: Int = 1,
  var column: Int = 1) {

  fun nextLine() {
    line++
    column = 1
  }

  fun copyFrom(other: Position) {
    line = other.line
    column = other.column
  }

  fun clone(): Position {
    return Position(line, column)
  }

  fun advance(offset: Int = 1) {
    column += offset
  }
}
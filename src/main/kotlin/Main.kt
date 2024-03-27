package fr.ancyr.jcc

fun main() {
  val fileName = "test.c"
  val resourceFile = Thread.currentThread().contextClassLoader.getResource(fileName)

  if (resourceFile == null) {
    println("Resource file not found: $fileName")
    return
  }

  var stream = resourceFile.openStream()

  val compiler = Compiler()
  compiler.compile(stream)
}
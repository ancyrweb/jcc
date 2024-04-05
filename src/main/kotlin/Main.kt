package fr.ancyr.jcc

import java.io.File

fun main() {
  val fileName = "test.c"
  val output = "./out/test.asm"

  val resourceFileUrl = Thread.currentThread().contextClassLoader.getResource(fileName)
  if (resourceFileUrl == null) {
    println("Resource file not found: $fileName")
    return
  }

  var inputStream = resourceFileUrl.openStream()
  var outputStream = File(output).outputStream()

  val compiler = Compiler(inputStream, outputStream)
  compiler.compile()
}
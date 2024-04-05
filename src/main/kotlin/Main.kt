package fr.ancyr.jcc

import java.io.File

fun main() {
  val fileName = "test.c"
  val output = "./out/test.asm"

  val resourceFileUrl =
    Thread.currentThread().contextClassLoader.getResource(fileName)
  if (resourceFileUrl == null) {
    println("Resource file not found: $fileName")
    return
  }

  var inputStream = resourceFileUrl.openStream()
  var outputFile = File(output)
  if (!outputFile.exists()) {
    outputFile.createNewFile()
  }

  val compiler = Compiler(inputStream, outputFile.outputStream())
  compiler.compile()
}
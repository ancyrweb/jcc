package fr.ancyr.jcc.ast.sem

enum class SymbolType {
    INT,
    FLOAT,
    STRING,
    CHAR,
    SHORT,
    LONG,
    DOUBLE;

    companion object {
        fun fromString(type: String): SymbolType {
            return when (type) {
                "int" -> INT
                "float" -> FLOAT
                "string" -> STRING
                "char" -> CHAR
                "short" -> SHORT
                "long" -> LONG
                "double" -> DOUBLE
                else -> throw IllegalArgumentException("Unknown type: $type")
            }
        }
    }
}
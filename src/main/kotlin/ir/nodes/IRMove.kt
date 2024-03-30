package fr.ancyr.jcc.ir.nodes

data class IRMove (val target: IRExpr, val source: IRExpr) : IRStatement() {
    override fun toString(): String {
        return "$target = $source"
    }
}
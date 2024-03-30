package fr.ancyr.jcc.ir.nodes.expr

import fr.ancyr.jcc.ir.nodes.literal.IRLiteral

data class IRConstExpr(val literal: IRLiteral) : IRExpr()
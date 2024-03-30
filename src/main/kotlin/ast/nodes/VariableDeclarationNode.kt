package fr.ancyr.jcc.ast.nodes

import fr.ancyr.jcc.lex.Token

data class VariableDeclarationNode(
   val type: Token,
   val identifier: Token,
   val value: Node?
) : Node()
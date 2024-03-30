package fr.ancyr.jcc.ast.nodes

import fr.ancyr.jcc.lex.Token

data class DeclarationNode(
   val type: Token,
   val identifier: Token,
   val value: Node?
) : Node()
package fr.ancyr.jcc.ast.nodes

import fr.ancyr.jcc.lex.Token

data class IdentifierStatementNode(
   val identifier: Token,
   val value: Node?
) : Node()
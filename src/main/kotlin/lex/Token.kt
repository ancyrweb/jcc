package fr.ancyr.jcc.lex

import fr.ancyr.jcc.commons.Position

data class Token(
  public val type: TokenType,
  public val position: Position,
  public val value: Any
)
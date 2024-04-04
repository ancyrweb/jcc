package fr.ancyr.jcc.ast.nodes

import fr.ancyr.jcc.lex.Token

data class ReturnNode(val keyword: Token, val expr: Expr? = null) : Node() {
  override fun toString(): String {
    return "KeywordDeclarationNode($keyword)"
  }
}
package io.prismio.psi;

import com.intellij.psi.tree.IElementType;

/** Token types generated from the Prismio grammar and lexer definitions. */
public interface PrismioTypes {

  IElementType BOOLEAN = new PrismioTokenType("BOOLEAN");
  IElementType CHARACTER_LITERAL = new PrismioTokenType("CHARACTER_LITERAL");
  IElementType FLOAT = new PrismioTokenType("FLOAT");
  IElementType INTEGER = new PrismioTokenType("INTEGER");
  IElementType STRING_LITERAL = new PrismioTokenType("STRING_LITERAL");

  IElementType IDENTIFIER = new PrismioTokenType("IDENTIFIER");
  IElementType KEYWORD = new PrismioTokenType("KEYWORD");
  IElementType TYPE_KEYWORD = new PrismioTokenType("TYPE_KEYWORD");

  IElementType ARITHMETIC_OP = new PrismioTokenType("ARITHMETIC_OP");
  IElementType ARROW = new PrismioTokenType("ARROW");
  IElementType ASSIGNMENT_OP = new PrismioTokenType("ASSIGNMENT_OP");
  IElementType BITWISE = new PrismioTokenType("BITWISE");
  IElementType COMPARISON = new PrismioTokenType("COMPARISON");
  IElementType FAT_ARROW = new PrismioTokenType("FAT_ARROW");
  IElementType LOGICAL_OP = new PrismioTokenType("LOGICAL_OP");
  IElementType OPERATOR = new PrismioTokenType("OPERATOR");
  IElementType RELATIONAL_OP = new PrismioTokenType("RELATIONAL_OP");
  IElementType UNARY_OP = new PrismioTokenType("UNARY_OP");

  IElementType COLON = new PrismioTokenType("COLON");
  IElementType COMMA = new PrismioTokenType("COMMA");
  IElementType DOT = new PrismioTokenType("DOT");
  IElementType LBRACE = new PrismioTokenType("LBRACE");
  IElementType LBRACKET = new PrismioTokenType("LBRACKET");
  IElementType LPAREN = new PrismioTokenType("LPAREN");
  IElementType RBRACE = new PrismioTokenType("RBRACE");
  IElementType RBRACKET = new PrismioTokenType("RBRACKET");
  IElementType RPAREN = new PrismioTokenType("RPAREN");
  IElementType SEMICOLON = new PrismioTokenType("SEMICOLON");
  IElementType SEPARATOR = new PrismioTokenType("SEPARATOR");

  IElementType MULTILINE_COMMENT = new PrismioTokenType("MULTILINE_COMMENT");
  IElementType SINGLE_LINE_COMMENT = new PrismioTokenType("SINGLE_LINE_COMMENT");
}

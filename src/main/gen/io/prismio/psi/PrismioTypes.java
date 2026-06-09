package io.prismio.psi;

import com.intellij.psi.tree.IElementType;

public interface PrismioTypes {

  // Element types
  IElementType BLOCK = new PrismioElementType("BLOCK");
  IElementType FUNCTION_DECL = new PrismioElementType("FUNCTION_DECL");

  // Token types - Literals
  IElementType BOOLEAN = new PrismioTokenType("BOOLEAN");
  IElementType CHARACTER_LITERAL = new PrismioTokenType("CHARACTER_LITERAL");
  IElementType FLOAT = new PrismioTokenType("FLOAT");
  IElementType INTEGER = new PrismioTokenType("INTEGER");
  IElementType STRING_LITERAL = new PrismioTokenType("STRING_LITERAL");

  // Token types - Identifiers and Keywords
  IElementType IDENTIFIER = new PrismioTokenType("IDENTIFIER");
  IElementType KEYWORD = new PrismioTokenType("KEYWORD");
  IElementType TYPE_KEYWORD = new PrismioTokenType("TYPE_KEYWORD");

  // Token types - Operators (specific)
  IElementType ARROW = new PrismioTokenType("ARROW");
  IElementType FAT_ARROW = new PrismioTokenType("FAT_ARROW");
  IElementType ARITHMETIC_OP = new PrismioTokenType("ARITHMETIC_OP");
  IElementType RELATIONAL_OP = new PrismioTokenType("RELATIONAL_OP");
  IElementType ASSIGNMENT_OP = new PrismioTokenType("ASSIGNMENT_OP");
  IElementType UNARY_OP = new PrismioTokenType("UNARY_OP");
  IElementType LOGICAL_OP = new PrismioTokenType("LOGICAL_OP");
  IElementType COMPARISON = new PrismioTokenType("COMPARISON");
  IElementType BITWISE = new PrismioTokenType("BITWISE");

  // Generic operator (for backward compatibility)
  IElementType OPERATOR = new PrismioTokenType("OPERATOR");

  // Token types - Separators (specific)
  IElementType LPAREN = new PrismioTokenType("LPAREN");
  IElementType RPAREN = new PrismioTokenType("RPAREN");
  IElementType LBRACE = new PrismioTokenType("LBRACE");
  IElementType RBRACE = new PrismioTokenType("RBRACE");
  IElementType LBRACKET = new PrismioTokenType("LBRACKET");
  IElementType RBRACKET = new PrismioTokenType("RBRACKET");
  IElementType COMMA = new PrismioTokenType("COMMA");
  IElementType COLON = new PrismioTokenType("COLON");
  IElementType SEMICOLON = new PrismioTokenType("SEMICOLON");
  IElementType DOT = new PrismioTokenType("DOT");

  // Generic separator (for backward compatibility)
  IElementType SEPARATOR = new PrismioTokenType("SEPARATOR");

  // Token types - Comments
  IElementType SINGLE_LINE_COMMENT = new PrismioTokenType("SINGLE_LINE_COMMENT");
  IElementType MULTILINE_COMMENT = new PrismioTokenType("MULTILINE_COMMENT");

  // Deprecated - kept for compatibility
  IElementType FN = KEYWORD;
  IElementType PARAMETER_LIST = new PrismioTokenType("PARAMETER_LIST");
  IElementType STATEMENT = new PrismioTokenType("STATEMENT");
  IElementType KEY = new PrismioTokenType("KEY");
  IElementType VALUE = new PrismioTokenType("VALUE");
}
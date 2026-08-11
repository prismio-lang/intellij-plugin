package io.prismio.psi;

import com.intellij.psi.tree.TokenSet;

public interface PrismioTokenSets {
  TokenSet IDENTIFIERS = TokenSet.create(PrismioTypes.IDENTIFIER);

  TokenSet COMMENTS =
      TokenSet.create(PrismioTypes.SINGLE_LINE_COMMENT, PrismioTypes.MULTILINE_COMMENT);

  TokenSet KEYWORDS = TokenSet.create(PrismioTypes.KEYWORD, PrismioTypes.TYPE_KEYWORD);

  TokenSet LITERALS = TokenSet.create(PrismioTypes.STRING_LITERAL, PrismioTypes.CHARACTER_LITERAL,
      PrismioTypes.INTEGER, PrismioTypes.FLOAT, PrismioTypes.BOOLEAN);

  TokenSet OPERATORS = TokenSet.create(PrismioTypes.ARITHMETIC_OP, PrismioTypes.RELATIONAL_OP,
      PrismioTypes.ASSIGNMENT_OP, PrismioTypes.UNARY_OP, PrismioTypes.LOGICAL_OP,
      PrismioTypes.COMPARISON, PrismioTypes.BITWISE, PrismioTypes.ARROW, PrismioTypes.FAT_ARROW);

  TokenSet SEPARATORS = TokenSet.create(PrismioTypes.LPAREN, PrismioTypes.RPAREN,
      PrismioTypes.LBRACE, PrismioTypes.RBRACE, PrismioTypes.LBRACKET, PrismioTypes.RBRACKET,
      PrismioTypes.COMMA, PrismioTypes.COLON, PrismioTypes.SEMICOLON, PrismioTypes.DOT);
}
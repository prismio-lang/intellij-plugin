package io.prismio.ums;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;

/**
 * The UMS token vocabulary, following {@code ums/parser/token.psm} and
 * {@code ums/parser/lexer.psm} in the compiler repository.
 *
 * <p>Manifests have no keywords. {@code project}, {@code targets} and
 * {@code executable} are ordinary identifiers that the *semantic* layer gives
 * meaning to — {@code ums/ARCHITECTURE.md} calls the AST "intentionally generic"
 * so a new block can be parsed before the model learns what it means. The lexer
 * here keeps that property: it emits {@link #IDENTIFIER} for all of them, and
 * {@link UmsAnnotator} colours the ones it recognises. That is why adding a
 * block to the language needs no change in this file.
 */
public interface UmsTypes {

  IElementType IDENTIFIER = new UmsTokenType("IDENTIFIER");
  IElementType STRING = new UmsTokenType("STRING");
  IElementType INTEGER = new UmsTokenType("INTEGER");
  IElementType BOOLEAN = new UmsTokenType("BOOLEAN");

  /** {@code //} and {@code #} both start a line comment; the lexer accepts either. */
  IElementType COMMENT = new UmsTokenType("COMMENT");

  IElementType LEFT_BRACE = new UmsTokenType("LEFT_BRACE");
  IElementType RIGHT_BRACE = new UmsTokenType("RIGHT_BRACE");
  IElementType LEFT_PAREN = new UmsTokenType("LEFT_PAREN");
  IElementType RIGHT_PAREN = new UmsTokenType("RIGHT_PAREN");
  IElementType LEFT_BRACKET = new UmsTokenType("LEFT_BRACKET");
  IElementType RIGHT_BRACKET = new UmsTokenType("RIGHT_BRACKET");
  IElementType COMMA = new UmsTokenType("COMMA");
  IElementType EQUAL = new UmsTokenType("EQUAL");
  IElementType SEMICOLON = new UmsTokenType("SEMICOLON");

  TokenSet COMMENTS = TokenSet.create(COMMENT);
  TokenSet STRINGS = TokenSet.create(STRING);
  TokenSet LITERALS = TokenSet.create(STRING, INTEGER, BOOLEAN);
  TokenSet BRACES = TokenSet.create(LEFT_BRACE, RIGHT_BRACE);
}

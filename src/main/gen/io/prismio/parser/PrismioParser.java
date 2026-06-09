// This is a generated file. Not intended for manual editing.
package io.prismio.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static io.prismio.psi.PrismioTypes.*;
import static com.intellij.lang.parser.GeneratedParserUtilBase.*;
import static io.prismio.psi.PrismioTypes.BLOCK;
import static io.prismio.psi.PrismioTypes.BOOLEAN;
import static io.prismio.psi.PrismioTypes.CHARACTER_LITERAL;
import static io.prismio.psi.PrismioTypes.FLOAT;
import static io.prismio.psi.PrismioTypes.FN;
import static io.prismio.psi.PrismioTypes.FUNCTION_DECL;
import static io.prismio.psi.PrismioTypes.IDENTIFIER;
import static io.prismio.psi.PrismioTypes.INTEGER;
import static io.prismio.psi.PrismioTypes.KEYWORD;
import static io.prismio.psi.PrismioTypes.LBRACE;
import static io.prismio.psi.PrismioTypes.MULTILINE_COMMENT;
import static io.prismio.psi.PrismioTypes.PARAMETER_LIST;
import static io.prismio.psi.PrismioTypes.RBRACE;
import static io.prismio.psi.PrismioTypes.SINGLE_LINE_COMMENT;
import static io.prismio.psi.PrismioTypes.STATEMENT;
import static io.prismio.psi.PrismioTypes.STRING_LITERAL;
import static io.prismio.psi.PrismioTypes.TYPE_KEYWORD;

import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.lang.PsiParser;
import com.intellij.lang.LightPsiParser;

@SuppressWarnings({ "SimplifiableIfStatement", "UnusedAssignment" })
public class PrismioParser implements PsiParser, LightPsiParser {

  public ASTNode parse(IElementType t, PsiBuilder b) {
    parseLight(t, b);
    return b.getTreeBuilt();
  }

  public void parseLight(IElementType t, PsiBuilder b) {
    boolean r;
    b = adapt_builder_(t, b, this, null);
    Marker m = enter_section_(b, 0, _COLLAPSE_, null);
    r = parse_root_(t, b);
    exit_section_(b, 0, m, t, r, true, TRUE_CONDITION);
  }

  protected boolean parse_root_(IElementType t, PsiBuilder b) {
    return parse_root_(t, b, 0);
  }

  static boolean parse_root_(IElementType t, PsiBuilder b, int l) {
    return prismioFile(b, l + 1);
  }

  /* ********************************************************** */
  // "{"
  public static boolean LBRACE(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "LBRACE"))
      return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, LBRACE, "<lbrace>");
    r = consumeToken(b, "{");
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // "}"
  public static boolean RBRACE(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "RBRACE"))
      return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, RBRACE, "<rbrace>");
    r = consumeToken(b, "}");
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // LBRACE statement* RBRACE
  public static boolean block(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "block"))
      return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, BLOCK, "<block>");
    r = LBRACE(b, l + 1);
    r = r && block_1(b, l + 1);
    r = r && RBRACE(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // statement*
  private static boolean block_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "block_1"))
      return false;
    while (true) {
      int c = current_position_(b);
      if (!consumeToken(b, STATEMENT))
        break;
      if (!empty_element_parsed_guard_(b, "block_1", c))
        break;
    }
    return true;
  }

  /* ********************************************************** */
  // FN IDENTIFIER parameter_list block
  public static boolean function_decl(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "function_decl"))
      return false;
    if (!nextTokenIs(b, FN))
      return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, FN, IDENTIFIER, PARAMETER_LIST);
    r = r && block(b, l + 1);
    exit_section_(b, m, FUNCTION_DECL, r);
    return r;
  }

  /* ********************************************************** */
  // KEYWORD
  // | TYPE_KEYWORD
  // | IDENTIFIER
  // | STRING_LITERAL
  // | CHARACTER_LITERAL
  // | BOOLEAN
  // | INTEGER
  // | FLOAT
  // | MULTILINE_COMMENT
  // | SINGLE_LINE_COMMENT
  // | LPAREN | RPAREN | LBRACE | RBRACE | LBRACKET | RBRACKET
  // | COMMA | COLON | DOT | SEMICOLON
  // | ARROW | FAT_ARROW
  // | RELATIONAL_OP | ASSIGNMENT_OP | UNARY_OP | LOGICAL_OP
  // | ARITHMETIC_OP | COMPARISON | BITWISE
  static boolean item_(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "item_"))
      return false;
    boolean r;
    r = consumeToken(b, KEYWORD);
    if (!r)
      r = consumeToken(b, TYPE_KEYWORD);
    if (!r)
      r = consumeToken(b, IDENTIFIER);
    if (!r)
      r = consumeToken(b, STRING_LITERAL);
    if (!r)
      r = consumeToken(b, CHARACTER_LITERAL);
    if (!r)
      r = consumeToken(b, BOOLEAN);
    if (!r)
      r = consumeToken(b, MULTILINE_COMMENT);
    if (!r)
      r = consumeToken(b, SINGLE_LINE_COMMENT);
    if (!r)
      r = consumeToken(b, INTEGER);
    if (!r)
      r = consumeToken(b, FLOAT);
    // Separators (specific)
    if (!r)
      r = consumeToken(b, LPAREN);
    if (!r)
      r = consumeToken(b, RPAREN);
    if (!r)
      r = consumeToken(b, LBRACE);
    if (!r)
      r = consumeToken(b, RBRACE);
    if (!r)
      r = consumeToken(b, LBRACKET);
    if (!r)
      r = consumeToken(b, RBRACKET);
    if (!r)
      r = consumeToken(b, COMMA);
    if (!r)
      r = consumeToken(b, COLON);
    if (!r)
      r = consumeToken(b, DOT);
    if (!r)
      r = consumeToken(b, SEMICOLON);
    // Operators (specific)
    if (!r)
      r = consumeToken(b, ARROW);
    if (!r)
      r = consumeToken(b, FAT_ARROW);
    if (!r)
      r = consumeToken(b, RELATIONAL_OP);
    if (!r)
      r = consumeToken(b, ASSIGNMENT_OP);
    if (!r)
      r = consumeToken(b, UNARY_OP);
    if (!r)
      r = consumeToken(b, LOGICAL_OP);
    if (!r)
      r = consumeToken(b, ARITHMETIC_OP);
    if (!r)
      r = consumeToken(b, COMPARISON);
    if (!r)
      r = consumeToken(b, BITWISE);
    return r;
  }

  /* ********************************************************** */
  // item_*
  static boolean prismioFile(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "prismioFile"))
      return false;
    while (true) {
      int c = current_position_(b);
      if (!item_(b, l + 1))
        break;
      if (!empty_element_parsed_guard_(b, "prismioFile", c))
        break;
    }
    return true;
  }
}

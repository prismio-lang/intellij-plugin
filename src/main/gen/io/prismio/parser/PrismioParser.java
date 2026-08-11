// This is a generated file. Not intended for manual editing.
package io.prismio.parser;

import com.intellij.lang.ASTNode;
import com.intellij.lang.LightPsiParser;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import com.intellij.lang.PsiParser;
import com.intellij.psi.tree.IElementType;

import static com.intellij.lang.parser.GeneratedParserUtilBase.TRUE_CONDITION;
import static com.intellij.lang.parser.GeneratedParserUtilBase._COLLAPSE_;
import static com.intellij.lang.parser.GeneratedParserUtilBase.adapt_builder_;
import static com.intellij.lang.parser.GeneratedParserUtilBase.consumeToken;
import static com.intellij.lang.parser.GeneratedParserUtilBase.current_position_;
import static com.intellij.lang.parser.GeneratedParserUtilBase.empty_element_parsed_guard_;
import static com.intellij.lang.parser.GeneratedParserUtilBase.enter_section_;
import static com.intellij.lang.parser.GeneratedParserUtilBase.exit_section_;
import static com.intellij.lang.parser.GeneratedParserUtilBase.recursion_guard_;
import static io.prismio.psi.PrismioTypes.ARITHMETIC_OP;
import static io.prismio.psi.PrismioTypes.ARROW;
import static io.prismio.psi.PrismioTypes.ASSIGNMENT_OP;
import static io.prismio.psi.PrismioTypes.BITWISE;
import static io.prismio.psi.PrismioTypes.BOOLEAN;
import static io.prismio.psi.PrismioTypes.CHARACTER_LITERAL;
import static io.prismio.psi.PrismioTypes.COLON;
import static io.prismio.psi.PrismioTypes.COMMA;
import static io.prismio.psi.PrismioTypes.COMPARISON;
import static io.prismio.psi.PrismioTypes.DOT;
import static io.prismio.psi.PrismioTypes.FAT_ARROW;
import static io.prismio.psi.PrismioTypes.FLOAT;
import static io.prismio.psi.PrismioTypes.IDENTIFIER;
import static io.prismio.psi.PrismioTypes.INTEGER;
import static io.prismio.psi.PrismioTypes.KEYWORD;
import static io.prismio.psi.PrismioTypes.LBRACE;
import static io.prismio.psi.PrismioTypes.LBRACKET;
import static io.prismio.psi.PrismioTypes.LOGICAL_OP;
import static io.prismio.psi.PrismioTypes.LPAREN;
import static io.prismio.psi.PrismioTypes.MULTILINE_COMMENT;
import static io.prismio.psi.PrismioTypes.RBRACE;
import static io.prismio.psi.PrismioTypes.RBRACKET;
import static io.prismio.psi.PrismioTypes.RELATIONAL_OP;
import static io.prismio.psi.PrismioTypes.RPAREN;
import static io.prismio.psi.PrismioTypes.SEMICOLON;
import static io.prismio.psi.PrismioTypes.SINGLE_LINE_COMMENT;
import static io.prismio.psi.PrismioTypes.STRING_LITERAL;
import static io.prismio.psi.PrismioTypes.TYPE_KEYWORD;
import static io.prismio.psi.PrismioTypes.UNARY_OP;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public final class PrismioParser implements PsiParser, LightPsiParser {

  @Override
  public ASTNode parse(IElementType root, PsiBuilder builder) {
    parseLight(root, builder);
    return builder.getTreeBuilt();
  }

  @Override
  public void parseLight(IElementType root, PsiBuilder builder) {
    builder = adapt_builder_(root, builder, this, null);
    Marker marker = enter_section_(builder, 0, _COLLAPSE_, null);
    boolean result = parseRoot(builder, 1);
    exit_section_(builder, 0, marker, root, result, true, TRUE_CONDITION);
  }

  private static boolean parseRoot(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "prismioFile")) {
      return false;
    }
    while (true) {
      int position = current_position_(builder);
      if (!parseItem(builder, level + 1)) {
        break;
      }
      if (!empty_element_parsed_guard_(builder, "prismioFile", position)) {
        break;
      }
    }
    return true;
  }

  private static boolean parseItem(PsiBuilder builder, int level) {
    if (!recursion_guard_(builder, level, "item")) {
      return false;
    }
    IElementType tokenType = builder.getTokenType();
    if (tokenType == KEYWORD
        || tokenType == TYPE_KEYWORD
        || tokenType == IDENTIFIER
        || tokenType == STRING_LITERAL
        || tokenType == CHARACTER_LITERAL
        || tokenType == BOOLEAN
        || tokenType == INTEGER
        || tokenType == FLOAT
        || tokenType == MULTILINE_COMMENT
        || tokenType == SINGLE_LINE_COMMENT
        || tokenType == LPAREN
        || tokenType == RPAREN
        || tokenType == LBRACE
        || tokenType == RBRACE
        || tokenType == LBRACKET
        || tokenType == RBRACKET
        || tokenType == COMMA
        || tokenType == COLON
        || tokenType == DOT
        || tokenType == SEMICOLON
        || tokenType == ARROW
        || tokenType == FAT_ARROW
        || tokenType == RELATIONAL_OP
        || tokenType == ASSIGNMENT_OP
        || tokenType == UNARY_OP
        || tokenType == LOGICAL_OP
        || tokenType == ARITHMETIC_OP
        || tokenType == COMPARISON
        || tokenType == BITWISE) {
      return consumeToken(builder, tokenType);
    }
    return false;
  }
}

package io.prismio.formatter;

import com.intellij.formatting.Alignment;
import com.intellij.formatting.Block;
import com.intellij.formatting.ChildAttributes;
import com.intellij.formatting.Indent;
import com.intellij.formatting.Spacing;
import com.intellij.formatting.SpacingBuilder;
import com.intellij.formatting.Wrap;
import com.intellij.formatting.WrapType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.TokenType;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.formatter.common.AbstractBlock;
import com.intellij.psi.tree.IElementType;
import io.prismio.psi.PrismioTokenSets;
import io.prismio.PrismioLanguage;
import io.prismio.psi.PrismioTypes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Enhanced code formatter for Prismio with comprehensive spacing rules.
 * Uses brace counting for indentation since PSI tree is flat (no block nodes).
 */
final class PrismioBlock extends AbstractBlock {
  private final CodeStyleSettings settings;
  private final SpacingBuilder spacingBuilder;
  private final int indentLevel;
  private @Nullable Set<Integer> typeArgumentAngles;

  public PrismioBlock(@NotNull ASTNode node, @Nullable Wrap wrap, @Nullable Alignment alignment,
      CodeStyleSettings settings) {
    this(node, wrap, alignment, settings, 0);
  }

  public PrismioBlock(@NotNull ASTNode node, @Nullable Wrap wrap, @Nullable Alignment alignment,
      CodeStyleSettings settings, int indentLevel) {
    super(node, wrap, alignment);
    this.settings = settings;
    this.spacingBuilder = createSpacingBuilder(settings);
    this.indentLevel = indentLevel;
  }

  /**
   * Spacing, most specific rule first — {@link SpacingBuilder} takes the first
   * match, so order is the whole design.
   *
   * <p>There is deliberately no blanket "no space after an identifier" rule.
   * One used to sit above these, and because `and`, `or`, `as` and `in` are
   * keywords used after an expression, it collapsed `a and b` into `aand b` and
   * `for i in xs` into `for iin xs` — reformatting produced source the compiler
   * rejects. Every case it was there for (a call's parenthesis, an index
   * bracket, a field dot) has its own rule below.
   */
  private static SpacingBuilder createSpacingBuilder(CodeStyleSettings settings) {
    return new SpacingBuilder(settings, PrismioLanguage.INSTANCE)
        // Tightest first: these bind harder than anything they sit between.
        .around(PrismioTypes.DOT).spaces(0)
        .around(PrismioTypes.RANGE).spaces(0)
        .before(PrismioTypes.OPTIONAL).spaces(0)

        // A keyword always has air around it. Above the parenthesis rules, so
        // `if (a)` keeps its space while `println(a)` does not.
        .after(PrismioTypes.KEYWORD).spaces(1)
        .before(PrismioTypes.KEYWORD).spaces(1)
        .after(PrismioTypes.CONTEXTUAL_KEYWORD).spaces(1)

        .before(PrismioTypes.COMMA).spaces(0)
        .after(PrismioTypes.COMMA).spaces(1)
        .before(PrismioTypes.COLON).spaces(0)
        .after(PrismioTypes.COLON).spaces(1)

        .before(PrismioTypes.LPAREN).spaces(0)
        .after(PrismioTypes.LPAREN).spaces(0)
        .before(PrismioTypes.RPAREN).spaces(0)
        .before(PrismioTypes.LBRACKET).spaces(0)
        .after(PrismioTypes.LBRACKET).spaces(0)
        .before(PrismioTypes.RBRACKET).spaces(0)

        .around(PrismioTypes.ARROW).spaces(1)
        .around(PrismioTypes.FAT_ARROW).spaces(1)
        .around(PrismioTypes.ARITHMETIC_OP).spaces(1)
        .around(PrismioTypes.RELATIONAL_OP).spaces(1)
        .around(PrismioTypes.ASSIGNMENT_OP).spaces(1)
        .around(PrismioTypes.LOGICAL_OP).spaces(1)
        .around(PrismioTypes.BITWISE_OP).spaces(1)
        .around(PrismioTypes.SHIFT_OP).spaces(1)

        .before(PrismioTypes.LBRACE).spaces(1);
  }

  /**
   * Whether this `<`, `>` or `>>` delimits a type-argument list.
   *
   * <p>They lex as relational and shift operators, so the rule that puts air
   * around a comparison would otherwise turn `List<Int>` into `List < Int >`.
   * Told apart the way the compiler's parser tells them apart: scan from the `<`
   * to its matching `>` and require everything between to be something a type
   * can be made of. `a < b` has no closing `>`; `if (a < b)` hits a `)`; and
   * `x < 5` hits an integer. `List<Map<String, Int>>` closes cleanly, with the
   * `>>` counting for two.
   */
  private static Set<Integer> typeArgumentAngles(ASTNode fileNode) {
    List<ASTNode> tokens = new ArrayList<>();
    for (ASTNode child : fileNode.getChildren(null)) {
      IElementType type = child.getElementType();
      if (type != TokenType.WHITE_SPACE && !PrismioTokenSets.COMMENTS.contains(type)) {
        tokens.add(child);
      }
    }

    Set<Integer> angles = new HashSet<>();
    for (int i = 0; i < tokens.size(); i++) {
      if (!isAngleOpen(tokens.get(i))) {
        continue;
      }
      // Only a name can introduce type arguments. A `<` after `)` or a literal
      // is a comparison whatever follows it.
      IElementType before = i == 0 ? null : tokens.get(i - 1).getElementType();
      if (before != PrismioTypes.IDENTIFIER && before != PrismioTypes.BUILTIN_TYPE
          && before != PrismioTypes.STDLIB_TYPE
          && before != PrismioTypes.CONTEXTUAL_KEYWORD) {
        continue;
      }

      int depth = 0;
      List<Integer> span = new ArrayList<>();
      for (int j = i; j < tokens.size(); j++) {
        ASTNode token = tokens.get(j);
        if (isAngleOpen(token)) {
          depth++;
          span.add(j);
        } else if (token.getElementType() == PrismioTypes.SHIFT_OP
            && ">>".equals(token.getText())) {
          depth -= 2;
          span.add(j);
        } else if (isAngleClose(token)) {
          depth--;
          span.add(j);
        } else if (!allowedInsideTypeArguments(token)) {
          depth = -1;
          break;
        }
        if (depth <= 0) {
          break;
        }
      }
      if (depth == 0) {
        for (int index : span) {
          angles.add(tokens.get(index).getStartOffset());
        }
      }
    }
    return angles;
  }

  private static boolean isAngleOpen(ASTNode node) {
    return node.getElementType() == PrismioTypes.RELATIONAL_OP && "<".equals(node.getText());
  }

  private static boolean isAngleClose(ASTNode node) {
    return node.getElementType() == PrismioTypes.RELATIONAL_OP && ">".equals(node.getText());
  }

  /** What a type-argument list may contain, including a `+` joining two bounds. */
  private static boolean allowedInsideTypeArguments(ASTNode node) {
    IElementType type = node.getElementType();
    if (type == PrismioTypes.IDENTIFIER || type == PrismioTypes.BUILTIN_TYPE
        || type == PrismioTypes.STDLIB_TYPE || type == PrismioTypes.CONTEXTUAL_KEYWORD
        || type == PrismioTypes.COMMA || type == PrismioTypes.COLON
        || type == PrismioTypes.DOT || type == PrismioTypes.OPTIONAL
        || type == PrismioTypes.LBRACKET || type == PrismioTypes.RBRACKET) {
      return true;
    }
    return type == PrismioTypes.ARITHMETIC_OP && "+".equals(node.getText());
  }

  @Override
  protected List<Block> buildChildren() {
    List<Block> blocks = new ArrayList<>();
    ASTNode child = myNode.getFirstChildNode();
    int currentIndent = 0;

    while (child != null) {
      if (child.getElementType() != TokenType.WHITE_SPACE && child.getTextLength() > 0) {
        IElementType type = child.getElementType();

        // Decrease indent BEFORE adding RBRACE
        if (type == PrismioTypes.RBRACE) {
          currentIndent = Math.max(0, currentIndent - 1);
        }

        Block block = new PrismioBlock(
            child, Wrap.createWrap(WrapType.NONE, false), null, settings, currentIndent);
        blocks.add(block);

        // Increase indent AFTER adding LBRACE
        if (type == PrismioTypes.LBRACE) {
          currentIndent++;
        }
      }
      child = child.getTreeNext();
    }
    return blocks;
  }

  @Override
  public Indent getIndent() {
    // For tokens at file level, use their calculated indent level
    if (indentLevel > 0) {
      // Return a specific indent based on the level
      return Indent.getSpaceIndent(indentLevel * 4);
    }
    return Indent.getNoneIndent();
  }

  /** Computed once per file, because the scan is over the whole token list. */
  private Set<Integer> typeArgumentAngles() {
    if (typeArgumentAngles == null) {
      typeArgumentAngles = typeArgumentAngles(myNode);
    }
    return typeArgumentAngles;
  }

  private static boolean isOpeningAngle(@Nullable Block block) {
    return block instanceof PrismioBlock prismio && isAngleOpen(prismio.myNode);
  }

  private boolean isTypeArgumentAngle(@Nullable Block block) {
    return block instanceof PrismioBlock prismio
        && typeArgumentAngles().contains(prismio.myNode.getStartOffset());
  }

  @Nullable
  @Override
  public Spacing getSpacing(@Nullable Block child1, @NotNull Block child2) {
    // `List<Int>` closes up; `a < b` keeps its air. Checked before the builder,
    // because to it both are the same relational-operator token.
    //
    // Only *inside* the brackets. What follows the closing `>` is ordinary code
    // and keeps its ordinary spacing -- `List<Int> = list_new()` needs the space
    // before the `=`, and `Point<Int> { x: 1 }` needs the one before the brace.
    if (isTypeArgumentAngle(child2)
        || (isTypeArgumentAngle(child1) && isOpeningAngle(child1))) {
      return Spacing.createSpacing(0, 0, 0, false, 0);
    }
    return spacingBuilder.getSpacing(this, child1, child2);
  }

  @Override
  public boolean isLeaf() {
    return myNode.getFirstChildNode() == null;
  }

  @NotNull
  @Override
  public ChildAttributes getChildAttributes(int newChildIndex) {
    // Get the indent level for new children based on brace context
    List<Block> children = getSubBlocks();
    int braceDepth = 0;

    for (int i = 0; i < newChildIndex && i < children.size(); i++) {
      Block block = children.get(i);
      if (block instanceof PrismioBlock) {
        IElementType type = ((PrismioBlock) block).myNode.getElementType();
        if (type == PrismioTypes.LBRACE) {
          braceDepth++;
        } else if (type == PrismioTypes.RBRACE) {
          braceDepth = Math.max(0, braceDepth - 1);
        }
      }
    }

    if (braceDepth > 0) {
      return new ChildAttributes(Indent.getSpaceIndent(braceDepth * 4), null);
    }
    return new ChildAttributes(Indent.getNoneIndent(), null);
  }
}

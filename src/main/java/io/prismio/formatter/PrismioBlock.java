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
import io.prismio.PrismioLanguage;
import io.prismio.psi.PrismioTypes;
import java.util.ArrayList;
import java.util.List;
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

  private static SpacingBuilder createSpacingBuilder(CodeStyleSettings settings) {
    return new SpacingBuilder(settings, PrismioLanguage.INSTANCE)
        // Arrow operators - space on both sides for return type arrows
        .around(PrismioTypes.ARROW)
        .spaces(1)
        .around(PrismioTypes.FAT_ARROW)
        .spaces(1)

        // Around all operators - space on both sides
        .around(PrismioTypes.ARITHMETIC_OP)
        .spaces(1)
        .around(PrismioTypes.RELATIONAL_OP)
        .spaces(1)
        .around(PrismioTypes.ASSIGNMENT_OP)
        .spaces(1)
        .around(PrismioTypes.LOGICAL_OP)
        .spaces(1)
        .around(PrismioTypes.COMPARISON)
        .spaces(1)
        .around(PrismioTypes.OPERATOR)
        .spaces(1)
        .around(PrismioTypes.BITWISE)
        .spaces(1)

        // After keywords - single space
        .after(PrismioTypes.KEYWORD)
        .spaces(1)
        // Space before type keywords
        .before(PrismioTypes.TYPE_KEYWORD)
        .spaces(1)

        // Identifiers - no extra space after by default
        .after(PrismioTypes.IDENTIFIER)
        .spaces(0)

        // Specific separators
        .before(PrismioTypes.COMMA)
        .spaces(0)
        .after(PrismioTypes.COMMA)
        .spaces(1)
        .before(PrismioTypes.COLON)
        .spaces(0)
        .after(PrismioTypes.COLON)
        .spaces(1)
        .around(PrismioTypes.DOT)
        .spaces(0)

        // Braces - space before opening brace
        .before(PrismioTypes.LBRACE)
        .spaces(1)

        // Parentheses - no internal spacing
        .after(PrismioTypes.LPAREN)
        .spaces(0)
        .before(PrismioTypes.RPAREN)
        .spaces(0)
        .before(PrismioTypes.LPAREN)
        .spaces(0)

        // Brackets - no internal spacing
        .after(PrismioTypes.LBRACKET)
        .spaces(0)
        .before(PrismioTypes.RBRACKET)
        .spaces(0);
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

  @Nullable
  @Override
  public Spacing getSpacing(@Nullable Block child1, @NotNull Block child2) {
    // Always apply spacing rules from the builder
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

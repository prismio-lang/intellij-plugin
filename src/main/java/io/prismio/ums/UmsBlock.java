package io.prismio.ums;

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
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Manifest formatting: four spaces per brace level, and the spacing a manifest
 * is conventionally written with.
 *
 * <p>Indentation is counted from braces rather than read from a tree, because
 * the PSI here is a flat token list. The same approach as the Prismio formatter,
 * and for the same reason.
 */
final class UmsBlock extends AbstractBlock {

  private static final int INDENT_SIZE = 4;

  private final CodeStyleSettings settings;
  private final SpacingBuilder spacingBuilder;
  private final int indentLevel;

  UmsBlock(@NotNull ASTNode node, @Nullable Wrap wrap, @Nullable Alignment alignment,
      CodeStyleSettings settings) {
    this(node, wrap, alignment, settings, 0);
  }

  private UmsBlock(@NotNull ASTNode node, @Nullable Wrap wrap, @Nullable Alignment alignment,
      CodeStyleSettings settings, int indentLevel) {
    super(node, wrap, alignment);
    this.settings = settings;
    this.spacingBuilder = createSpacingBuilder(settings);
    this.indentLevel = indentLevel;
  }

  private static SpacingBuilder createSpacingBuilder(CodeStyleSettings settings) {
    return new SpacingBuilder(settings, UmsLanguage.INSTANCE)
        // `name = value`, never `name=value`.
        .around(UmsTypes.EQUAL).spaces(1)
        // `executable("app")`, never `executable ("app")` or `( "app" )`.
        .before(UmsTypes.LEFT_PAREN).spaceIf(false)
        .after(UmsTypes.LEFT_PAREN).spaceIf(false)
        .before(UmsTypes.RIGHT_PAREN).spaceIf(false)
        .before(UmsTypes.COMMA).spaceIf(false)
        .after(UmsTypes.COMMA).spaces(1)
        .before(UmsTypes.SEMICOLON).spaceIf(false)
        // `targets {`, with the brace on the same line as the name that opens it.
        .before(UmsTypes.LEFT_BRACE).spaces(1)
        .after(UmsTypes.LEFT_BRACKET).spaceIf(false)
        .before(UmsTypes.RIGHT_BRACKET).spaceIf(false);
  }

  @Override
  protected List<Block> buildChildren() {
    List<Block> blocks = new ArrayList<>();
    ASTNode child = myNode.getFirstChildNode();
    int currentIndent = 0;

    while (child != null) {
      if (child.getElementType() != TokenType.WHITE_SPACE && child.getTextLength() > 0) {
        IElementType type = child.getElementType();

        // A closing brace belongs to the level it closes, so the level drops
        // before the token is added rather than after.
        if (UmsTypes.RIGHT_BRACE.equals(type)) {
          currentIndent = Math.max(0, currentIndent - 1);
        }

        blocks.add(new UmsBlock(child, Wrap.createWrap(WrapType.NONE, false), null,
            settings, currentIndent));

        if (UmsTypes.LEFT_BRACE.equals(type)) {
          currentIndent++;
        }
      }
      child = child.getTreeNext();
    }
    return blocks;
  }

  @Override
  public Indent getIndent() {
    return indentLevel > 0 ? Indent.getSpaceIndent(indentLevel * INDENT_SIZE)
                           : Indent.getNoneIndent();
  }

  @Override
  public @Nullable Spacing getSpacing(@Nullable Block child1, @NotNull Block child2) {
    return spacingBuilder.getSpacing(this, child1, child2);
  }

  @Override
  public boolean isLeaf() {
    return myNode.getFirstChildNode() == null;
  }

  @Override
  public @NotNull ChildAttributes getChildAttributes(int newChildIndex) {
    List<Block> children = getSubBlocks();
    int depth = 0;
    for (int i = 0; i < newChildIndex && i < children.size(); i++) {
      if (children.get(i) instanceof UmsBlock block) {
        IElementType type = block.myNode.getElementType();
        if (UmsTypes.LEFT_BRACE.equals(type)) {
          depth++;
        } else if (UmsTypes.RIGHT_BRACE.equals(type)) {
          depth = Math.max(0, depth - 1);
        }
      }
    }
    return new ChildAttributes(
        depth > 0 ? Indent.getSpaceIndent(depth * INDENT_SIZE) : Indent.getNoneIndent(), null);
  }
}

package io.prismio.ums;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;

public final class UmsParserDefinition implements ParserDefinition {
  public static final IFileElementType FILE = new IFileElementType(UmsLanguage.INSTANCE);

  @Override
  public @NotNull Lexer createLexer(Project project) {
    return new UmsLexer();
  }

  @Override
  public @NotNull TokenSet getCommentTokens() {
    return UmsTypes.COMMENTS;
  }

  @Override
  public @NotNull TokenSet getStringLiteralElements() {
    return UmsTypes.STRINGS;
  }

  @Override
  public @NotNull PsiParser createParser(Project project) {
    return new UmsParser();
  }

  @Override
  public @NotNull IFileElementType getFileNodeType() {
    return FILE;
  }

  @Override
  public @NotNull PsiFile createFile(@NotNull FileViewProvider viewProvider) {
    return new UmsFile(viewProvider);
  }

  @Override
  public @NotNull PsiElement createElement(ASTNode node) {
    return new ASTWrapperPsiElement(node);
  }
}

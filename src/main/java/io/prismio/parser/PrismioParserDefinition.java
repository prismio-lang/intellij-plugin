package io.prismio.parser;

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
import io.prismio.PrismioLanguage;
import io.prismio.lexer.PrismioLexerAdapter;
import io.prismio.psi.PrismioFile;
import io.prismio.psi.PrismioTokenSets;
import io.prismio.psi.PrismioTypes;
import org.jetbrains.annotations.NotNull;

public final class PrismioParserDefinition implements ParserDefinition {
  public static final IFileElementType FILE = new IFileElementType(PrismioLanguage.INSTANCE);

  @NotNull
  @Override
  public Lexer createLexer(Project project) {
    return new PrismioLexerAdapter();
  }

  @NotNull
  @Override
  public TokenSet getCommentTokens() {
    return PrismioTokenSets.COMMENTS;
  }

  @NotNull
  @Override
  public TokenSet getStringLiteralElements() {
    return TokenSet.create(PrismioTypes.STRING_LITERAL, PrismioTypes.CHARACTER_LITERAL);
  }

  @NotNull
  @Override
  public PsiParser createParser(final Project project) {
    return new PrismioParser();
  }

  @NotNull
  @Override
  public IFileElementType getFileNodeType() {
    return FILE;
  }

  @NotNull
  @Override
  public PsiFile createFile(@NotNull FileViewProvider viewProvider) {
    return new PrismioFile(viewProvider);
  }

  @NotNull
  @Override
  public PsiElement createElement(ASTNode node) {
    // Prismio currently uses a flat PSI token stream; semantic services derive
    // declaration context from the lexer until the compiler AST is exposed.
    return new com.intellij.extapi.psi.ASTWrapperPsiElement(node);
  }
}

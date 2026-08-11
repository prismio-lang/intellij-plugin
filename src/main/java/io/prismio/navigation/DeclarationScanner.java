package io.prismio.navigation;

import com.intellij.lexer.Lexer;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import io.prismio.lexer.PrismioLexerAdapter;
import io.prismio.psi.PrismioTypes;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class DeclarationScanner {
  private DeclarationScanner() {}

  public static @NotNull List<Declaration> collect(@NotNull PsiFile file) {
    List<Declaration> declarations = new ArrayList<>();
    CharSequence text = file.getViewProvider().getContents();
    Lexer lexer = new PrismioLexerAdapter();
    lexer.start(text);

    int braceDepth = 0;
    DeclarationKind pendingKind = null;
    while (lexer.getTokenType() != null) {
      IElementType tokenType = lexer.getTokenType();
      String tokenText = text.subSequence(lexer.getTokenStart(), lexer.getTokenEnd()).toString();

      if (tokenType == PrismioTypes.KEYWORD) {
        DeclarationKind declarationKind = DeclarationKind.fromKeyword(tokenText, braceDepth);
        if (declarationKind != null) {
          pendingKind = declarationKind;
        } else if (!"mut".equals(tokenText)) {
          pendingKind = null;
        }
      } else if (tokenType == PrismioTypes.IDENTIFIER && pendingKind != null) {
        PsiElement element = file.findElementAt(lexer.getTokenStart());
        if (element != null) {
          declarations.add(new Declaration(element, tokenText, pendingKind));
        }
        pendingKind = null;
      } else if (tokenType != TokenType.WHITE_SPACE && tokenType != PrismioTypes.SINGLE_LINE_COMMENT
          && tokenType != PrismioTypes.MULTILINE_COMMENT) {
        pendingKind = null;
      }

      if (tokenType == PrismioTypes.LBRACE) {
        braceDepth++;
      } else if (tokenType == PrismioTypes.RBRACE && braceDepth > 0) {
        braceDepth--;
      }
      lexer.advance();
    }
    return declarations;
  }

  public static @Nullable Declaration findDeclaration(@NotNull PsiElement element) {
    PsiFile file = element.getContainingFile();
    if (file == null) {
      return null;
    }
    int offset = element.getTextOffset();
    return collect(file)
        .stream()
        .filter(declaration -> {
          PsiElement declarationElement = declaration.getElement();
          return declarationElement != null && declarationElement.getTextOffset() == offset;
        })
        .findFirst()
        .orElse(null);
  }
}

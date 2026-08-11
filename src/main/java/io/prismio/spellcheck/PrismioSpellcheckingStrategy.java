package io.prismio.spellcheck;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.spellchecker.inspections.CommentSplitter;
import com.intellij.spellchecker.inspections.PlainTextSplitter;
import com.intellij.spellchecker.tokenizer.SpellcheckingStrategy;
import com.intellij.spellchecker.tokenizer.TokenConsumer;
import com.intellij.spellchecker.tokenizer.Tokenizer;
import io.prismio.psi.PrismioTypes;
import org.jetbrains.annotations.NotNull;

public final class PrismioSpellcheckingStrategy extends SpellcheckingStrategy {
  private static final Tokenizer<PsiComment> COMMENT_TOKENIZER = new CommentTokenizer();
  private static final Tokenizer<PsiElement> STRING_TOKENIZER = new StringTokenizer();

  @Override
  public @NotNull Tokenizer<?> getTokenizer(PsiElement element) {
    if (element instanceof PsiComment) {
      return COMMENT_TOKENIZER;
    }
    if (element.getNode().getElementType() == PrismioTypes.STRING_LITERAL) {
      return STRING_TOKENIZER;
    }
    return EMPTY_TOKENIZER;
  }

  private static final class CommentTokenizer extends Tokenizer<PsiComment> {
    @Override
    public void tokenize(@NotNull PsiComment element, @NotNull TokenConsumer consumer) {
      String text = element.getText();
      int start = text.startsWith("//") || text.startsWith("/*") ? 2 : 0;
      int end = text.endsWith("*/") ? text.length() - 2 : text.length();
      if (start < end) {
        consumer.consumeToken(
            element, text, false, 0, TextRange.create(start, end), CommentSplitter.getInstance());
      }
    }
  }

  private static final class StringTokenizer extends Tokenizer<PsiElement> {
    @Override
    public void tokenize(@NotNull PsiElement element, @NotNull TokenConsumer consumer) {
      String text = element.getText();
      if (text.length() > 2) {
        consumer.consumeToken(element, text, false, 0, TextRange.create(1, text.length() - 1),
            PlainTextSplitter.getInstance());
      }
    }
  }
}

package io.prismio.lexer;

import com.intellij.lexer.FlexAdapter;
import io.prismio.PrismioLexer;

public final class PrismioLexerAdapter extends FlexAdapter {
  public PrismioLexerAdapter() {
    super(new PrismioLexer(null));
  }
}

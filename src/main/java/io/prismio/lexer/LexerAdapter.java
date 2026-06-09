package io.prismio.lexer;

import com.intellij.lexer.FlexAdapter;
import io.prismio.PsLexer;

public class LexerAdapter extends FlexAdapter {
  public LexerAdapter() {
    super(new PsLexer(null));
  }
}

package io.prismio.ums;

import com.intellij.lang.Language;

/**
 * The Unified Manifest System language: `build.ums`.
 *
 * <p>A separate {@link Language} from Prismio rather than a dialect of it. UMS
 * has its own lexer and parser in the compiler repository for the reason stated
 * in `ums/ARCHITECTURE.md` — a manifest is a smaller, forward-extensible DSL with
 * different recovery and validation needs, and it deliberately does not reuse the
 * Prismio grammar. Sharing a Language here would mean sharing a file type, a
 * commenter and a formatter with a language that agrees with it on almost
 * nothing: `#` is a comment in one and an error in the other.
 */
public final class UmsLanguage extends Language {
  public static final UmsLanguage INSTANCE = new UmsLanguage();

  private UmsLanguage() {
    super("UMS");
  }
}

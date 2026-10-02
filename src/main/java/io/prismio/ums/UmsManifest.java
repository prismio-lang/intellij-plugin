package io.prismio.ums;

import com.intellij.lexer.Lexer;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * What a {@code build.ums} gives the IDE to run: its targets and its commands.
 *
 * <p>These are the manifest's half of the CLI, which is where the run widget's
 * entries come from: {@code prismio build}, {@code prismio run <executable>},
 * {@code prismio test}, and {@code prismio <name>} for every {@code command}
 * the manifest declares ({@code ums/model/lowering.psm}). Read with the
 * plugin's own UMS lexer, from text, so it needs no editor and no index.
 */
public record UmsManifest(@Nullable String projectName, @NotNull List<Target> targets,
    @NotNull List<Command> commands) {

  /**
   * {@code executable("name")}, {@code test("name")} or {@code library("name")}.
   *
   * @param start where the declaration's kind word starts; {@code end} where its block closes
   */
  public record Target(@NotNull String kind, @NotNull String name, @Nullable String entry, int start, int end) {
    public boolean isExecutable() {
      return kind.equals("executable");
    }

    public boolean isTest() {
      return kind.equals("test");
    }
  }

  /** {@code command("name") { description = "..." ... }} */
  public record Command(@NotNull String name, @Nullable String description, int start, int end) {}

  private record Tok(IElementType type, String text, int start, int end) {}

  public static @NotNull UmsManifest read(@NotNull CharSequence text) {
    List<Tok> toks = new ArrayList<>();
    Lexer lexer = new UmsLexer();
    lexer.start(text);
    while (lexer.getTokenType() != null) {
      IElementType type = lexer.getTokenType();
      if (type != TokenType.WHITE_SPACE && !UmsTypes.COMMENTS.contains(type)) {
        toks.add(new Tok(type, text.subSequence(lexer.getTokenStart(), lexer.getTokenEnd()).toString(),
            lexer.getTokenStart(), lexer.getTokenEnd()));
      }
      lexer.advance();
    }

    String projectName = null;
    List<Target> targets = new ArrayList<>();
    List<Command> commands = new ArrayList<>();
    List<String> path = new ArrayList<>();
    for (int i = 0; i < toks.size(); i++) {
      Tok tok = toks.get(i);
      if (tok.type == UmsTypes.RIGHT_BRACE) {
        if (!path.isEmpty()) {
          path.remove(path.size() - 1);
        }
        continue;
      }
      if (tok.type != UmsTypes.IDENTIFIER) {
        continue;
      }
      // `name { ... }` — a block.
      if (next(toks, i, UmsTypes.LEFT_BRACE)) {
        path.add(tok.text);
        i++;
        continue;
      }
      // `key = value`
      if (next(toks, i, UmsTypes.EQUAL) && i + 2 < toks.size()) {
        if (path.equals(List.of("project")) && tok.text.equals("name")) {
          projectName = unquote(toks.get(i + 2).text);
        }
        continue;
      }
      // `kind("name", ...) { ... }` — a declaration with a block.
      if (next(toks, i, UmsTypes.LEFT_PAREN) && i + 2 < toks.size()
          && toks.get(i + 2).type == UmsTypes.STRING) {
        int close = i + 2;
        while (close < toks.size() && toks.get(close).type != UmsTypes.RIGHT_PAREN) {
          close++;
        }
        boolean block = close + 1 < toks.size() && toks.get(close + 1).type == UmsTypes.LEFT_BRACE;
        String name = unquote(toks.get(i + 2).text);
        if (!block) {
          i = close;
          continue;
        }
        int blockEnd = matchingBrace(toks, close + 1);
        int end = blockEnd < toks.size() ? toks.get(blockEnd).end : tok.end;
        if (path.equals(List.of("targets"))) {
          targets.add(new Target(tok.text, name, property(toks, close + 1, blockEnd, "entry"), tok.start, end));
        } else if (path.equals(List.of("commands")) && tok.text.equals("command")) {
          commands.add(new Command(name, property(toks, close + 1, blockEnd, "description"), tok.start, end));
        }
        // Past the declaration's own block: its nested blocks are not declarations here.
        i = blockEnd;
      }
    }
    return new UmsManifest(projectName, List.copyOf(targets), List.copyOf(commands));
  }

  /** The target or command whose declaration spans {@code offset}, as the gutter and context runs ask. */
  public @Nullable Object declarationAt(int offset) {
    for (Target target : targets) {
      if (offset >= target.start() && offset <= target.end()) {
        return target;
      }
    }
    for (Command command : commands) {
      if (offset >= command.start() && offset <= command.end()) {
        return command;
      }
    }
    return null;
  }

  private static boolean next(List<Tok> toks, int i, IElementType type) {
    return i + 1 < toks.size() && toks.get(i + 1).type == type;
  }

  private static int matchingBrace(List<Tok> toks, int open) {
    int depth = 0;
    for (int k = open; k < toks.size(); k++) {
      if (toks.get(k).type == UmsTypes.LEFT_BRACE) {
        depth++;
      } else if (toks.get(k).type == UmsTypes.RIGHT_BRACE && --depth == 0) {
        return k;
      }
    }
    return toks.size() - 1;
  }

  /** {@code key = "value"} directly inside the block {@code [open, close]}. */
  private static @Nullable String property(List<Tok> toks, int open, int close, String key) {
    int depth = 0;
    for (int k = open; k <= close && k < toks.size(); k++) {
      Tok tok = toks.get(k);
      if (tok.type == UmsTypes.LEFT_BRACE) {
        depth++;
      } else if (tok.type == UmsTypes.RIGHT_BRACE) {
        depth--;
      } else if (depth == 1 && tok.type == UmsTypes.IDENTIFIER && tok.text.equals(key)
          && next(toks, k, UmsTypes.EQUAL) && k + 2 < toks.size()
          && toks.get(k + 2).type == UmsTypes.STRING) {
        return unquote(toks.get(k + 2).text);
      }
    }
    return null;
  }

  private static String unquote(String text) {
    if (text.length() >= 2 && text.startsWith("\"") && text.endsWith("\"")) {
      return text.substring(1, text.length() - 1);
    }
    return text;
  }
}

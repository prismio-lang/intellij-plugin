package io.prismio.ums;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Where an identifier sits in a manifest, and therefore what it means.
 *
 * <p>The PSI is a flat token list, so "which block am I in" is answered by
 * walking from the start of the file and tracking brace depth. That is cheap —
 * a manifest is tens of lines — and it is the only way to tell {@code library}
 * the target kind from {@code library} the linker input, which are the same word
 * one nesting level apart.
 */
public final class UmsContext {

  /** What an identifier is being used as. */
  public enum Role {
    /** Opens a block: `project {`, `executable("x") {`. */
    BLOCK,
    /** Left of an `=`. */
    KEY,
    /** A call that opens no block: `library("m")`, `build("app")`. */
    CALL,
    /** The `args` marker inside a command step. */
    ARGS,
    /** An identifier in value position, or one nothing recognises. */
    VALUE
  }

  /** The enclosing block names, outermost first. Empty at the top level. */
  public final List<String> path;
  public final Role role;
  public final String name;

  private UmsContext(List<String> path, Role role, String name) {
    this.path = List.copyOf(path);
    this.role = role;
    this.name = name;
  }

  /** True when the manifest system gives this identifier, here, a meaning. */
  public boolean isRecognised() {
    Map<String, String> expected = expectedHere();
    if (expected == null) {
      // Inside a block this plugin does not model. Say nothing rather than
      // marking every identifier unknown: `ums/ARCHITECTURE.md` is explicit that
      // new blocks are parsed before the model learns them, so a manifest may
      // legitimately contain a block newer than this plugin.
      return true;
    }
    return role == Role.ARGS || role == Role.VALUE || expected.containsKey(name);
  }

  /** The identifiers valid at this position, or null when the position is unmodelled. */
  public Map<String, String> expectedHere() {
    if (role == Role.VALUE || role == Role.ARGS) {
      return null;
    }
    return completionsFor(path);
  }

  /** What may be written inside the block named by {@code path}. */
  public static Map<String, String> completionsFor(List<String> path) {
    if (path.isEmpty()) {
      return UmsWords.TOP_LEVEL_BLOCKS;
    }
    String outer = path.get(0);
    switch (outer) {
      case "project":
        return path.size() == 1 ? UmsWords.PROJECT_KEYS : null;
      case "toolchain":
        return path.size() == 1 ? UmsWords.TOOLCHAIN_KEYS : null;
      case "dependencies":
        return path.size() == 1 ? UmsWords.DEPENDENCY_SCOPES : null;
      case "targets":
        if (path.size() == 1) {
          return UmsWords.TARGET_KINDS;
        }
        if (path.size() == 2) {
          return UmsWords.TARGET_BODY;
        }
        return "link".equals(path.get(2)) && path.size() == 3 ? UmsWords.LINK_KINDS : null;
      case "commands":
        if (path.size() == 1) {
          return UmsWords.COMMAND_KINDS;
        }
        return path.size() == 2 ? UmsWords.COMMAND_BODY : null;
      default:
        return null;
    }
  }

  /**
   * Resolves the context of one identifier token.
   *
   * <p>Returns null when the element is not an identifier, so callers can skip
   * it without a second type test.
   */
  public static UmsContext of(PsiElement element) {
    ASTNode node = element.getNode();
    if (node == null || !UmsTypes.IDENTIFIER.equals(node.getElementType())) {
      return null;
    }
    PsiFile file = element.getContainingFile();
    if (file == null) {
      return null;
    }

    List<Token> tokens = tokensOf(file);
    int index = indexOf(tokens, element.getTextRange().getStartOffset());
    if (index < 0) {
      return null;
    }

    List<String> path = pathBefore(tokens, index);
    return new UmsContext(path, roleOf(tokens, index, path), tokens.get(index).text);
  }

  /** The completions valid at an arbitrary offset, used before a name is typed. */
  public static List<String> pathAt(PsiFile file, int offset) {
    List<Token> tokens = tokensOf(file);
    int index = 0;
    while (index < tokens.size() && tokens.get(index).start < offset) {
      index++;
    }
    return pathBefore(tokens, index);
  }

  private static Role roleOf(List<Token> tokens, int index, List<String> path) {
    String text = tokens.get(index).text;
    Token next = at(tokens, index + 1);
    if (next == null) {
      return Role.VALUE;
    }

    if (UmsTypes.EQUAL.equals(next.type)) {
      return Role.KEY;
    }
    if (UmsTypes.LEFT_BRACE.equals(next.type)) {
      return Role.BLOCK;
    }
    if (UmsTypes.LEFT_PAREN.equals(next.type)) {
      // `name(...)` is a block when a `{` follows the closing paren, and a plain
      // call otherwise. Scanning to that paren is what separates
      // `executable("a") { ... }` from `library("m")`.
      int close = matchingParen(tokens, index + 1);
      Token after = at(tokens, close + 1);
      return after != null && UmsTypes.LEFT_BRACE.equals(after.type) ? Role.BLOCK : Role.CALL;
    }
    if (UmsWords.ARGS_MARKER.equals(text) && !path.isEmpty()) {
      return Role.ARGS;
    }
    return Role.VALUE;
  }

  /** The block names enclosing the token at {@code index}, outermost first. */
  private static List<String> pathBefore(List<Token> tokens, int index) {
    List<String> stack = new ArrayList<>();
    for (int i = 0; i < index && i < tokens.size(); i++) {
      Token token = tokens.get(i);
      if (UmsTypes.LEFT_BRACE.equals(token.type)) {
        stack.add(openerBefore(tokens, i));
      } else if (UmsTypes.RIGHT_BRACE.equals(token.type) && !stack.isEmpty()) {
        stack.remove(stack.size() - 1);
      }
    }
    return stack;
  }

  /** The identifier that opened the block whose `{` sits at {@code braceIndex}. */
  private static String openerBefore(List<Token> tokens, int braceIndex) {
    int i = braceIndex - 1;
    if (i >= 0 && UmsTypes.RIGHT_PAREN.equals(tokens.get(i).type)) {
      int depth = 0;
      while (i >= 0) {
        IElementType type = tokens.get(i).type;
        if (UmsTypes.RIGHT_PAREN.equals(type)) {
          depth++;
        } else if (UmsTypes.LEFT_PAREN.equals(type)) {
          depth--;
          if (depth == 0) {
            i--;
            break;
          }
        }
        i--;
      }
    }
    return i >= 0 && UmsTypes.IDENTIFIER.equals(tokens.get(i).type) ? tokens.get(i).text : "";
  }

  private static int matchingParen(List<Token> tokens, int openIndex) {
    int depth = 0;
    for (int i = openIndex; i < tokens.size(); i++) {
      IElementType type = tokens.get(i).type;
      if (UmsTypes.LEFT_PAREN.equals(type)) {
        depth++;
      } else if (UmsTypes.RIGHT_PAREN.equals(type)) {
        depth--;
        if (depth == 0) {
          return i;
        }
      }
    }
    return tokens.size();
  }

  private static Token at(List<Token> tokens, int index) {
    return index >= 0 && index < tokens.size() ? tokens.get(index) : null;
  }

  private static int indexOf(List<Token> tokens, int startOffset) {
    for (int i = 0; i < tokens.size(); i++) {
      if (tokens.get(i).start == startOffset) {
        return i;
      }
    }
    return -1;
  }

  /** Significant tokens only: whitespace and comments carry no structure. */
  private static List<Token> tokensOf(PsiFile file) {
    List<Token> tokens = new ArrayList<>();
    for (ASTNode node : file.getNode().getChildren(null)) {
      IElementType type = node.getElementType();
      if (UmsTypes.COMMENTS.contains(type) || com.intellij.psi.TokenType.WHITE_SPACE.equals(type)) {
        continue;
      }
      tokens.add(new Token(type, node.getText(), node.getStartOffset()));
    }
    return tokens;
  }

  private record Token(IElementType type, String text, int start) {}
}

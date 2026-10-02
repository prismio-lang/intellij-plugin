package io.prismio.symbols;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A type as source spells it: a name and its type arguments.
 *
 * <p>Deliberately not a semantic type. The plugin never decides whether two types
 * are the same the way the compiler does; it only needs to know which declarations
 * a receiver's members come from and what a member returns, and both are
 * questions about names. The compiler's internal {@code List<T>} is read as
 * {@code Vec<T>}, the type its members are declared on. {@code [T]} is
 * {@code Array<T>}, not a Vec: a Vec carries its length and grows, an array carries
 * it in its type ({@code Array<T, N>}) and has a member surface of its own.
 */
public record TypeRef(@NotNull String name, @NotNull List<TypeRef> args, boolean optional) {

  public static final TypeRef STRING = simple("String");
  public static final TypeRef INT = simple("Int");
  public static final TypeRef FLOAT = simple("Float");
  public static final TypeRef BOOL = simple("Bool");
  public static final TypeRef CHAR = simple("Char");

  public static @NotNull TypeRef simple(@NotNull String name) {
    return new TypeRef(name, List.of(), false);
  }

  public static @NotNull TypeRef of(@NotNull String name, @NotNull TypeRef... args) {
    return new TypeRef(name, List.of(args), false);
  }

  /** {@code Array<element, length>}: what {@code [a, b]} is when nothing says it is a Vec. */
  public static @NotNull TypeRef array(@NotNull TypeRef element, int length) {
    return of("Array", element, simple(Integer.toString(length)));
  }

  /** The name members are looked up under: {@code Vec} for {@code Vec<Int>} and {@code List<Int>}, {@code Array} for {@code [Int]}. */
  public @NotNull String base() {
    return "List".equals(name) ? "Vec" : name;
  }

  /**
   * The key a type's members are filed and looked up under: its base, and a {@code ?} for an
   * optional. {@code Int?} has members of its own -- {@code unwrapOr}, and std's
   * {@code impl Display for Int?} -- and none of {@code Int}'s, so the two must not share a key.
   * Filing {@code impl Display for Int?} under {@code Int} offered its {@code show} on an
   * {@code Int}, and every {@code Int} method on an {@code Int?}.
   */
  public @NotNull String memberKey() {
    return optional ? base() + "?" : base();
  }

  public @NotNull TypeRef withOptional(boolean value) {
    return value == optional ? this : new TypeRef(name, args, value);
  }

  /** Replaces type parameters by what they stand for; a name not in the map stays as written. */
  public @NotNull TypeRef substitute(@NotNull Map<String, TypeRef> bindings) {
    if (bindings.isEmpty()) {
      return this;
    }
    if (args.isEmpty()) {
      TypeRef bound = bindings.get(name);
      return bound == null ? this : bound.withOptional(optional || bound.optional());
    }
    List<TypeRef> substituted = new ArrayList<>(args.size());
    for (TypeRef arg : args) {
      substituted.add(arg.substitute(bindings));
    }
    return new TypeRef(name, substituted, optional);
  }

  /**
   * Binds the type parameters in {@code pattern} by matching it against
   * {@code actual}: {@code Vec<T>} against {@code Vec<Int>} binds {@code T} to
   * {@code Int}. Answers false when the two cannot be the same type.
   */
  public static boolean unify(@NotNull TypeRef pattern, @NotNull TypeRef actual,
      @NotNull List<String> parameters, @NotNull Map<String, TypeRef> bindings) {
    if (pattern.args.isEmpty() && parameters.contains(pattern.name)) {
      TypeRef bound = bindings.get(pattern.name);
      if (bound == null) {
        bindings.put(pattern.name, actual.withOptional(false));
        return true;
      }
      return bound.base().equals(actual.base());
    }
    if (!pattern.base().equals(actual.base())) {
      return false;
    }
    int count = Math.min(pattern.args.size(), actual.args.size());
    for (int i = 0; i < count; i++) {
      if (!unify(pattern.args.get(i), actual.args.get(i), parameters, bindings)) {
        return false;
      }
    }
    return true;
  }

  /** Parses the canonical text {@link #toString} writes. Answers null for anything else. */
  public static @Nullable TypeRef parse(@Nullable String text) {
    if (text == null || text.isBlank()) {
      return null;
    }
    int[] at = {0};
    TypeRef type = parse(text.replace(" ", ""), at);
    return type != null && at[0] == text.replace(" ", "").length() ? type : null;
  }

  private static @Nullable TypeRef parse(String text, int[] at) {
    int start = at[0];
    if (start < text.length() && text.charAt(start) == '[') {
      at[0]++;
      TypeRef element = parse(text, at);
      // `[T]` is an `Array<T>`; a length after the element is not kept.
      while (at[0] < text.length() && text.charAt(at[0]) != ']') {
        at[0]++;
      }
      if (element == null || at[0] >= text.length()) {
        return null;
      }
      at[0]++;
      return optionalSuffix(text, at, new TypeRef("Array", List.of(element), false));
    }
    while (at[0] < text.length()
        && (Character.isLetterOrDigit(text.charAt(at[0])) || text.charAt(at[0]) == '_'
            || text.charAt(at[0]) == '.')) {
      at[0]++;
    }
    if (at[0] == start) {
      return null;
    }
    String name = text.substring(start, at[0]);
    List<TypeRef> args = new ArrayList<>();
    if (at[0] < text.length() && text.charAt(at[0]) == '<') {
      at[0]++;
      while (true) {
        TypeRef arg = parse(text, at);
        if (arg == null) {
          return null;
        }
        args.add(arg);
        if (at[0] < text.length() && text.charAt(at[0]) == ',') {
          at[0]++;
          continue;
        }
        if (at[0] < text.length() && text.charAt(at[0]) == '>') {
          at[0]++;
          break;
        }
        return null;
      }
    }
    return optionalSuffix(text, at, new TypeRef(name, args, false));
  }

  private static TypeRef optionalSuffix(String text, int[] at, TypeRef type) {
    if (at[0] < text.length() && text.charAt(at[0]) == '?') {
      at[0]++;
      return type.withOptional(true);
    }
    return type;
  }

  @Override
  public @NotNull String toString() {
    StringBuilder out = new StringBuilder(name);
    if (!args.isEmpty()) {
      out.append('<');
      for (int i = 0; i < args.size(); i++) {
        if (i > 0) {
          out.append(", ");
        }
        out.append(args.get(i));
      }
      out.append('>');
    }
    if (optional) {
      out.append('?');
    }
    return out.toString();
  }
}

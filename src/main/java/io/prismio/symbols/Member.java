package io.prismio.symbols;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * One thing that can follow {@code receiver.}: what completion lists and what
 * inference reads a chain's next type from.
 *
 * @param type what reading it yields: a field's or property's type, a method's
 *     return type, with the receiver's type arguments already substituted
 * @param function the declaration, when it is a function; null for a field, constant or variant
 * @param module the module that must be imported for it to resolve, or empty
 *     when it needs none (the receiver's own file, or a member the compiler lowers itself)
 */
public record Member(
    @NotNull String name,
    @NotNull Kind kind,
    @NotNull Origin origin,
    @Nullable TypeRef type,
    @Nullable FnSig function,
    @NotNull String module,
    @NotNull String doc) {

  public enum Kind { FIELD, PROPERTY, METHOD, STATIC_METHOD, CONSTANT, VARIANT }

  /**
   * Where a member comes from, which is also how prominently completion shows it:
   * the type's own declarations first, then what traits and free functions
   * contribute.
   */
  public enum Origin { OWN, BUILT_IN, TRAIT, EXTENSION }

  public boolean isCall() {
    return kind == Kind.METHOD || kind == Kind.STATIC_METHOD;
  }
}

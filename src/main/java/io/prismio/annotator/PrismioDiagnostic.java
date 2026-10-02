package io.prismio.annotator;

import org.jetbrains.annotations.Nullable;

/**
 * Diagnostic message structure matching Prismio compiler's JSON output
 * from `prismio check <source.psm> --diagnostic-format=json`.
 */
public record PrismioDiagnostic(
    String kind,
    int schemaVersion,
    String severity,
    @Nullable String code,
    @Nullable String file,
    int line,
    int column,
    int length,
    String message
) {}

package io.prismio.annotator;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PrismioDiagnosticParser {

  private static final Gson GSON = new Gson();

  private PrismioDiagnosticParser() {}

  /**
   * Every diagnostic in the compiler's JSON output, with each note folded into the diagnostic
   * before it. The compiler writes a note as a record of its own -- usually with no file and
   * line 0 -- straight after the error it explains, so shown alone it is a stray annotation on
   * the first character of the file and the error loses its explanation.
   */
  public static @NotNull List<PrismioDiagnostic> parseOutput(@NotNull String output) {
    List<PrismioDiagnostic> diagnostics = new ArrayList<>();
    String[] lines = output.split("\\R");
    for (String line : lines) {
      String trimmed = line.trim();
      if (trimmed.isEmpty() || !trimmed.startsWith("{")) {
        continue;
      }
      PrismioDiagnostic diag = parseLine(trimmed);
      if (diag == null || !"diagnostic".equals(diag.kind())) {
        continue;
      }
      if (isNote(diag)) {
        if (!diagnostics.isEmpty()) {
          PrismioDiagnostic last = diagnostics.remove(diagnostics.size() - 1);
          diagnostics.add(withMessage(last,
              last.message() + "\n" + diag.severity().toLowerCase() + ": " + diag.message()));
        }
        continue;
      }
      diagnostics.add(diag);
    }
    return diagnostics;
  }

  private static boolean isNote(@NotNull PrismioDiagnostic diag) {
    String severity = diag.severity().toLowerCase();
    return severity.equals("note") || severity.equals("help");
  }

  private static @NotNull PrismioDiagnostic withMessage(@NotNull PrismioDiagnostic d, @NotNull String message) {
    return new PrismioDiagnostic(d.kind(), d.schemaVersion(), d.severity(), d.code(), d.file(),
        d.line(), d.column(), d.length(), message);
  }

  public static @Nullable PrismioDiagnostic parseLine(@NotNull String jsonLine) {
    try {
      JsonObject obj = JsonParser.parseString(jsonLine).getAsJsonObject();
      String kind = obj.has("kind") && !obj.get("kind").isJsonNull() ? obj.get("kind").getAsString() : "";
      if (!"diagnostic".equals(kind)) {
        return null;
      }
      int schemaVersion = obj.has("schemaVersion") && !obj.get("schemaVersion").isJsonNull()
          ? obj.get("schemaVersion").getAsInt() : 1;
      String severity = obj.has("severity") && !obj.get("severity").isJsonNull()
          ? obj.get("severity").getAsString() : "error";
      String code = obj.has("code") && !obj.get("code").isJsonNull()
          ? obj.get("code").getAsString() : null;
      String file = obj.has("file") && !obj.get("file").isJsonNull()
          ? obj.get("file").getAsString() : null;
      int line = obj.has("line") && !obj.get("line").isJsonNull()
          ? obj.get("line").getAsInt() : 0;
      int column = obj.has("column") && !obj.get("column").isJsonNull()
          ? obj.get("column").getAsInt() : 0;
      int length = obj.has("length") && !obj.get("length").isJsonNull()
          ? obj.get("length").getAsInt() : 0;
      String message = obj.has("message") && !obj.get("message").isJsonNull()
          ? obj.get("message").getAsString() : "";

      return new PrismioDiagnostic(kind, schemaVersion, severity, code, file, line, column, length, message);
    } catch (Exception e) {
      return null;
    }
  }
}

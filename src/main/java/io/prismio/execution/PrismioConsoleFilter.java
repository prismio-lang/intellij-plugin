package io.prismio.execution;

import com.intellij.execution.filters.Filter;
import com.intellij.execution.filters.OpenFileHyperlinkInfo;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Filter that converts compiler error locations (e.g. `src/main.psm:12:5`)
 * into clickable hyperlinks in the Run / Debug console.
 */
public final class PrismioConsoleFilter implements Filter {

  private static final Pattern LOCATION_PATTERN =
      Pattern.compile("(?:^|[\\s(\\[])(?<path>[a-zA-Z0-9_./\\\\-]+\\.psm):(?<line>\\d+):(?<col>\\d+)");

  private final Project project;

  public PrismioConsoleFilter(@NotNull Project project) {
    this.project = project;
  }

  @Override
  public @Nullable Result applyFilter(@NotNull String line, int entireLength) {
    Matcher matcher = LOCATION_PATTERN.matcher(line);
    int textStartOffset = entireLength - line.length();
    List<ResultItem> items = new ArrayList<>();

    while (matcher.find()) {
      String filePath = matcher.group("path");
      int lineNumber;
      int colNumber;
      try {
        lineNumber = Integer.parseInt(matcher.group("line"));
        colNumber = Integer.parseInt(matcher.group("col"));
      } catch (NumberFormatException e) {
        continue;
      }

      VirtualFile virtualFile = resolveVirtualFile(filePath);
      if (virtualFile != null && virtualFile.isValid()) {
        int highlightStart = textStartOffset + matcher.start("path");
        int highlightEnd = textStartOffset + matcher.end("col");

        OpenFileHyperlinkInfo hyperlink =
            new OpenFileHyperlinkInfo(project, virtualFile, Math.max(0, lineNumber - 1), Math.max(0, colNumber - 1));
        items.add(new ResultItem(highlightStart, highlightEnd, hyperlink));
      }
    }
    // Every location on the line, not the first: a note can name two files.
    return items.isEmpty() ? null : new Result(items);
  }

  private @Nullable VirtualFile resolveVirtualFile(@NotNull String pathString) {
    // 1. Try project directory (works in both real project and in-memory test fixtures)
    VirtualFile baseDir = com.intellij.openapi.project.ProjectUtil.guessProjectDir(project);
    if (baseDir != null) {
      VirtualFile vf = baseDir.findFileByRelativePath(pathString);
      if (vf != null && vf.isValid()) {
        return vf;
      }
    }

    // 2. Try LocalFileSystem
    Path path = Path.of(pathString);
    if (path.isAbsolute()) {
      VirtualFile vf = LocalFileSystem.getInstance().findFileByNioFile(path);
      if (vf != null && vf.isValid()) {
        return vf;
      }
    } else {
      String basePath = project.getBasePath();
      if (basePath != null) {
        VirtualFile vf = LocalFileSystem.getInstance().findFileByNioFile(Path.of(basePath).resolve(pathString));
        if (vf != null && vf.isValid()) {
          return vf;
        }
      }
    }

    // 3. Fallback: match by filename within project
    Path p = Path.of(pathString);
    Path fileNamePath = p.getFileName();
    if (fileNamePath != null) {
      var files = com.intellij.psi.search.FilenameIndex.getVirtualFilesByName(
          fileNamePath.toString(), com.intellij.psi.search.GlobalSearchScope.projectScope(project));
      if (!files.isEmpty()) {
        return files.iterator().next();
      }
    }

    return null;
  }
}

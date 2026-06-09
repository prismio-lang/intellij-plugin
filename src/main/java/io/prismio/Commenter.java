package io.prismio;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class Commenter implements com.intellij.lang.Commenter {

    @Override
    public String getLineCommentPrefix() {
        return "//";
    }

    @Override
    public String getBlockCommentPrefix() {
        return "/*";
    }

    @Override
    public @NotNull String getBlockCommentSuffix() {
        return "*/";
    }

    @Override
    public String getCommentedBlockCommentPrefix() {
        return "//";
    }

    @Nullable
    @Override
    public String getCommentedBlockCommentSuffix() {
        return null;
    }

}

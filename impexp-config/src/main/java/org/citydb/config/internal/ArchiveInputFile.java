package org.citydb.config.internal;

import java.nio.file.Path;
import java.util.Objects;

public abstract class ArchiveInputFile extends InputFile {
    protected final String contentFile;

    protected ArchiveInputFile(String contentFile, Path file) {
        super(file, InputFileType.ARCHIVE);
        this.contentFile = Objects.requireNonNull(contentFile, "content file must not be null.");
    }

    public String getContentFile() {
        return contentFile;
    }
}

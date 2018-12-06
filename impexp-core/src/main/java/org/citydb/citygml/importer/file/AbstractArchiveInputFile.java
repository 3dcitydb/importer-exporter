package org.citydb.citygml.importer.file;

import org.citydb.config.internal.InputFile;
import org.citydb.config.internal.FileType;

import java.nio.file.Path;
import java.util.Objects;

public abstract class AbstractArchiveInputFile extends InputFile {
    final String contentFile;

    AbstractArchiveInputFile(String contentFile, Path file) {
        super(file, FileType.ARCHIVE);
        this.contentFile = Objects.requireNonNull(contentFile, "content file must not be null.");
    }

    public String getContentFile() {
        return contentFile;
    }
}

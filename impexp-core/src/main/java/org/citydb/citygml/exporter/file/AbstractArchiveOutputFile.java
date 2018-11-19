package org.citydb.citygml.exporter.file;

import org.citydb.config.internal.FileType;
import org.citydb.config.internal.OutputFile;

import java.nio.file.Path;
import java.util.Objects;

public abstract class AbstractArchiveOutputFile extends OutputFile {
    final String contentFile;

    AbstractArchiveOutputFile(String contentFile, Path file) {
        super(file, FileType.ARCHIVE);
        this.contentFile = Objects.requireNonNull(contentFile, "content file must not be null.");
    }

    public String getContentFile() {
        return contentFile;
    }
}

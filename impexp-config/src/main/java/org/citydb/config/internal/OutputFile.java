package org.citydb.config.internal;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;

public abstract class OutputFile extends AbstractFile {

    protected OutputFile(Path file, FileType type) {
        super(file, type);
    }

    public abstract OutputStream openStream() throws IOException;
}

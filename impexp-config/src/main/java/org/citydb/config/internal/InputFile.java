package org.citydb.config.internal;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public abstract class InputFile extends AbstractFile {

    protected InputFile(Path file, FileType type) {
        super(file, type);
    }

    public abstract InputStream openStream() throws IOException;
}

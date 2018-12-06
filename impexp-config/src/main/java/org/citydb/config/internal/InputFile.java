package org.citydb.config.internal;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Objects;

public abstract class InputFile implements AutoCloseable {
    protected final Path file;
    protected final FileType type;

    protected InputFile(Path file, FileType type) {
        Objects.requireNonNull(file, "file must not be null.");
        this.file = file.toAbsolutePath().normalize();
        this.type = type;
    }

    public abstract InputStream openStream() throws IOException;
    public abstract Path resolve(String path) throws InvalidPathException;
    public abstract String getSeparator();

    @Override
    public abstract void close() throws IOException;

    public Path getFile() {
        return file;
    }

    public FileType getType() {
        return type;
    }

}

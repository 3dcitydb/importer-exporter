package org.citydb.config.internal;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Objects;

public abstract class OutputFile implements AutoCloseable {
    protected final Path file;
    protected final FileType type;

    protected OutputFile(Path file, FileType type) {
        Objects.requireNonNull(file, "file must not be null.");
        this.file = file.toAbsolutePath().normalize();
        this.type = type;
    }

    public abstract OutputStream openStream() throws IOException;
    public abstract String resolve(String... paths) throws InvalidPathException;
    public abstract void createDirectories(String path) throws IOException;
    public abstract OutputStream newOutputStream(String file) throws IOException;

    @Override
    public abstract void close() throws IOException;

    public Path getFile() {
        return file;
    }

    public FileType getType() {
        return type;
    }
}

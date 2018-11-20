package org.citydb.citygml.exporter.file;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Objects;

public class ZipOutputFile extends AbstractArchiveOutputFile {
    private final FileSystem fileSystem;

    ZipOutputFile(String contentFile, Path zipFile, FileSystem fileSystem) {
        super(contentFile, zipFile);
        this.fileSystem = Objects.requireNonNull(fileSystem, "zip file system must not be null.");
    }

    @Override
    public OutputStream openStream() throws IOException {
        return Files.newOutputStream(fileSystem.getPath(contentFile));
    }

    @Override
    public Path resolve(String path) throws InvalidPathException {
        return fileSystem.getPath(contentFile).getParent().resolve(path);
    }

    @Override
    public String getSeparator() {
        return fileSystem.getSeparator();
    }

    @Override
    public void close() throws IOException {
        if (fileSystem.isOpen())
            fileSystem.close();
    }

}

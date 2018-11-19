package org.citydb.citygml.importer.file;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Objects;

public class ZipInputFile extends AbstractArchiveInputFile {
    private final URI zipFileUri;

    private FileSystem fileSystem;

    ZipInputFile(String contentFile, Path zipFile, URI zipFileUri) {
        super(contentFile, zipFile);
        this.zipFileUri = Objects.requireNonNull(zipFileUri, "zip file URI must not be null.");
    }

    @Override
    public InputStream openStream() throws IOException {
        return new BufferedInputStream(Files.newInputStream(getFileSystem().getPath(contentFile)));
    }

    @Override
    public Path resolve(String path) throws InvalidPathException {
        return getFileSystem().getPath(contentFile).getParent().resolve(path);
    }

    @Override
    public String getSeparator() {
        return getFileSystem().getSeparator();
    }

    @Override
    public void close() throws IOException {
        if (fileSystem != null && fileSystem.isOpen())
            fileSystem.close();
    }

    private FileSystem getFileSystem() {
        if (fileSystem != null)
            return fileSystem;

        try {
            fileSystem = FileSystems.getFileSystem(zipFileUri);
        } catch (Throwable ignored) {
            //
        }

        if (fileSystem == null) {
            try {
                fileSystem = FileSystems.newFileSystem(zipFileUri, new HashMap<>());
            } catch (IOException e) {
                throw new IllegalStateException("Failed to open zip file system '" + zipFileUri + "'.", e);
            }
        }

        return fileSystem;
    }
}

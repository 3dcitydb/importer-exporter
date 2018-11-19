package org.citydb.citygml.exporter.file;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ZipOutputFile extends AbstractArchiveOutputFile {
    private final URI zipFileUri;
    private FileSystem fileSystem;
    private volatile boolean isInitialized;

    ZipOutputFile(String contentFile, Path zipFile, URI zipFileUri) {
        super(contentFile, zipFile);
        this.zipFileUri = Objects.requireNonNull(zipFileUri, "zip file URI must not be null.");
    }

    @Override
    public void init() throws IOException {
        if (!isInitialized) {
            Files.deleteIfExists(file);
            getFileSystem();
            isInitialized = true;
        }
    }

    @Override
    public OutputStream openStream() throws IOException {
        return Files.newOutputStream(getFileSystem().getPath(contentFile));
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
                Map<String, String> env = new HashMap<>();
                env.put("create", "true");
                fileSystem = FileSystems.newFileSystem(zipFileUri, env);
            } catch (IOException e) {
                throw new IllegalStateException("Failed to open zip file system '" + zipFileUri + "'.", e);
            }
        }

        return fileSystem;
    }

}

package org.citydb.citygml.exporter.file;

import org.citydb.config.internal.FileType;
import org.citydb.config.internal.OutputFile;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class AbstractRegularOutputFile extends OutputFile {

    AbstractRegularOutputFile(Path file, boolean isCompressed) {
        super(file, isCompressed ? FileType.COMPRESSED : FileType.REGULAR);
    }

    @Override
    public String resolve(String... paths) {
        return Paths.get(file.getParent().toString(), paths).toString();
    }

    @Override
    public void createDirectories(String path) throws IOException {
        Files.createDirectories(file.getParent().resolve(path));
    }

    @Override
    public OutputStream newOutputStream(String file) throws IOException {
        return Files.newOutputStream(Paths.get(file) );
    }

    @Override
    public void close() {
        // nothing to do here
    }
}

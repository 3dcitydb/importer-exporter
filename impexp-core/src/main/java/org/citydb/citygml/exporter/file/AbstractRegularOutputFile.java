package org.citydb.citygml.exporter.file;

import org.citydb.config.internal.FileType;
import org.citydb.config.internal.OutputFile;

import java.nio.file.FileSystems;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

public abstract class AbstractRegularOutputFile extends OutputFile {

    AbstractRegularOutputFile(Path file, boolean isCompressed) {
        super(file, isCompressed ? FileType.COMPRESSED : FileType.REGULAR);
    }

    @Override
    public Path resolve(String path) throws InvalidPathException {
        return file.getParent().resolve(path);
    }

    @Override
    public String getSeparator() {
        return FileSystems.getDefault().getSeparator();
    }

    @Override
    public void close() {
        // nothing to do here
    }
}

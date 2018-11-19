package org.citydb.citygml.importer.file;

import org.citydb.config.internal.InputFile;
import org.citydb.config.internal.InputFileType;

import java.nio.file.FileSystems;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

public abstract class AbstractRegularInputFile extends InputFile {

    AbstractRegularInputFile(Path file, boolean isCompressed) {
        super(file, isCompressed ? InputFileType.COMPRESSED : InputFileType.REGULAR);
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

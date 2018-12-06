package org.citydb.citygml.importer.file;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class XMLInputFile extends AbstractRegularInputFile {

    XMLInputFile(Path file) {
        super(file, false);
    }

    @Override
    public InputStream openStream() throws IOException {
        return new BufferedInputStream(Files.newInputStream(file));
    }
}

package org.citydb.citygml.exporter.file;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class XMLOutputFile extends AbstractRegularOutputFile {

    XMLOutputFile(Path file) {
        super(file, false);
    }

    @Override
    public OutputStream openStream() throws IOException {
        return Files.newOutputStream(file);
    }
}

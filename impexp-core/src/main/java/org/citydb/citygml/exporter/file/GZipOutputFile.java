package org.citydb.citygml.exporter.file;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPOutputStream;

public class GZipOutputFile extends AbstractRegularOutputFile {

    GZipOutputFile(Path file) {
        super(file, false);
    }

    @Override
    public OutputStream openStream() throws IOException {
        return new GZIPOutputStream(Files.newOutputStream(file));
    }
}

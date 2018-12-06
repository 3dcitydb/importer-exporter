package org.citydb.citygml.importer.file;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;

public class GZipInputFile extends AbstractRegularInputFile {

    GZipInputFile(Path file) {
        super(file, true);
    }

    @Override
    public InputStream openStream() throws IOException {
        return new GZIPInputStream(new BufferedInputStream(Files.newInputStream(file)));
    }
}

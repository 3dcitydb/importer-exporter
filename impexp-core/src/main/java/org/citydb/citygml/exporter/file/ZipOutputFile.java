package org.citydb.citygml.exporter.file;

import org.apache.commons.compress.archivers.zip.ParallelScatterZipCreator;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.parallel.FileBasedScatterGatherBackingStore;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;

public class ZipOutputFile extends AbstractArchiveOutputFile {
    private final ZipArchiveOutputStream out;
    private final ParallelScatterZipCreator scatterZipCreator;

    ZipOutputFile(String contentFile, Path zipFile, Path tempDir, int threads) throws IOException {
        super(contentFile, zipFile);

        out = new ZipArchiveOutputStream(zipFile.toFile());
        scatterZipCreator = new ParallelScatterZipCreator(
                Executors.newFixedThreadPool(Math.max(Runtime.getRuntime().availableProcessors(), threads)),
                () -> new FileBasedScatterGatherBackingStore(Files.createTempFile(tempDir, "zip", ".tmp").toFile()));
    }

    @Override
    public OutputStream openStream() throws IOException {
        return newOutputStream(contentFile);
    }

    @Override
    public String resolve(String... paths) {
        return String.join("/", paths).replace("\\", "/");
    }

    @Override
    public OutputStream newOutputStream(String file) throws IOException {
        ZipArchiveEntry entry = new ZipArchiveEntry(file);
        entry.setMethod(ZipEntry.DEFLATED);

        PipedInputStream in = new PipedInputStream();
        PipedOutputStream out = new PipedOutputStream(in);
        scatterZipCreator.addArchiveEntry(entry, () -> in);

        return out;
    }

    @Override
    public void createDirectories(String path) throws IOException {
        // we do not need to separately create directories
    }

    @Override
    public void close() throws IOException {
        try {
            scatterZipCreator.writeTo(out);
        } catch (Exception e) {
            //
        }

        out.close();
    }
}

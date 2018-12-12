package org.citydb.citygml.exporter.file;

import org.apache.commons.compress.archivers.zip.StreamCompressor;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.parallel.InputStreamSupplier;
import org.apache.commons.compress.parallel.ScatterGatherBackingStore;
import org.apache.commons.compress.parallel.ScatterGatherBackingStoreSupplier;
import org.apache.commons.compress.utils.BoundedInputStream;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.zip.Deflater;

public class ScatterZipOutputStream implements Closeable {
    private final ScatterGatherBackingStore backingStore;
    private final ScatterGatherBackingStore entryStore;
    private final StreamCompressor streamCompressor;

    ScatterZipOutputStream(ScatterGatherBackingStoreSupplier supplier) throws IOException {
        backingStore = supplier.get();
        entryStore = supplier.get();
        streamCompressor = StreamCompressor.create(Deflater.DEFAULT_COMPRESSION, backingStore);
    }

    void addArchiveEntry(ZipArchiveEntry zipArchiveEntry, InputStreamSupplier source) throws IOException {
        try (InputStream payloadStream = source.get()) {
            streamCompressor.deflate(payloadStream, zipArchiveEntry.getMethod());

            String entry = zipArchiveEntry.getName() + "," +
                    zipArchiveEntry.getMethod() + "," +
                    streamCompressor.getCrc32() + "," +
                    streamCompressor.getBytesWrittenForLastEntry() + "," +
                    streamCompressor.getBytesRead() + "\n";

            entryStore.writeOut(entry.getBytes(StandardCharsets.UTF_8), 0, entry.length());
        }
    }

    void writeTo(ZipArchiveOutputStream target) throws IOException {
        entryStore.closeForWriting();
        backingStore.closeForWriting();
        String line;

        try (InputStream stream = backingStore.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(entryStore.getInputStream(), StandardCharsets.UTF_8))) {
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length != 5)
                    throw new IOException("Failed to read temporary zip entry.");

                ZipArchiveEntry entry = new ZipArchiveEntry(values[0]);
                entry.setMethod(Integer.valueOf(values[1]));
                entry.setCrc(Long.valueOf(values[2]));
                entry.setCompressedSize(Long.valueOf(values[3]));
                entry.setSize(Long.valueOf(values[4]));

                try (BoundedInputStream rawStream = new BoundedInputStream(stream, entry.getCompressedSize())) {
                    target.addRawArchiveEntry(entry, rawStream);
                }
            }
        } catch (NumberFormatException e) {
            throw new IOException("Failed to read temporary zip entry.", e);
        }
    }

    @Override
    public void close() throws IOException {
        try {
            backingStore.close();
            entryStore.close();
        } finally {
            streamCompressor.close();
        }
    }
}

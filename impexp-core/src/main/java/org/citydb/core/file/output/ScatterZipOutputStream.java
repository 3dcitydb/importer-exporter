/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2021
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.lrg.tum.de/gis/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * Virtual City Systems, Berlin <https://vc.systems/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Taufkirchen <http://www.moss.de/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citydb.core.file.output;

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
                entry.setMethod(Integer.parseInt(values[1]));
                entry.setCrc(Long.parseLong(values[2]));
                entry.setCompressedSize(Long.parseLong(values[3]));
                entry.setSize(Long.parseLong(values[4]));

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

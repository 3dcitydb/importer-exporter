/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2019
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.gis.bgu.tum.de/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
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

package org.citydb.file.output;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.parallel.FileBasedScatterGatherBackingStore;
import org.apache.commons.compress.parallel.InputStreamSupplier;
import org.apache.commons.compress.parallel.ScatterGatherBackingStoreSupplier;
import org.citydb.concurrent.DefaultWorker;
import org.citydb.concurrent.PoolSizeAdaptationStrategy;
import org.citydb.concurrent.WorkerPool;
import org.citydb.config.project.global.LogLevel;
import org.citydb.event.EventDispatcher;
import org.citydb.event.global.InterruptEvent;
import org.citydb.log.Logger;
import org.citydb.util.Pipe;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;

public class ZipOutputFile extends AbstractArchiveOutputFile {
    private final Logger log = Logger.getInstance();

    private final ZipArchiveOutputStream out;
    private final WorkerPool<ScatterZipWork> scatterZipPool;
    private final Set<ScatterZipOutputStream> scatterStreams;

    private volatile boolean shouldRun = true;

    ZipOutputFile(String contentFile, Path zipFile, Path tempDir, int threads, EventDispatcher eventDispatcher, Object eventChannel) throws IOException {
        super(contentFile, zipFile);

        out = new ZipArchiveOutputStream(zipFile.toFile());
        scatterStreams = ConcurrentHashMap.newKeySet();

        int minThreads = Math.max(2, Runtime.getRuntime().availableProcessors());
        int maxThreads = Math.max(minThreads, threads);
        ScatterGatherBackingStoreSupplier supplier = () -> new FileBasedScatterGatherBackingStore(Files.createTempFile(tempDir, "zip", ".tmp").toFile());

        scatterZipPool = new WorkerPool<>("scatter_zip_pool", minThreads, maxThreads, PoolSizeAdaptationStrategy.AGGRESSIVE,
                () -> {
                    try {
                        return new ScatterZipWorker(supplier, eventDispatcher);
                    } catch (IOException e) {
                        log.error("Failed to create scatter zip writer.");
                        log.error(e.getClass().getTypeName() + ": " + e.getMessage());
                        return null;
                    }
                },
                maxThreads);

        scatterZipPool.setEventSource(eventChannel);
        scatterZipPool.prestartCoreWorkers();
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
        OutputStream out;

        if (shouldRun) {
            ZipArchiveEntry entry = new ZipArchiveEntry(file);
            entry.setMethod(ZipEntry.DEFLATED);

            Pipe pipe = new Pipe();
            out = pipe.source();
            scatterZipPool.addWork(new ScatterZipWork(entry, pipe::sink));
        } else {
            out = new OutputStream() {
                @Override
                public void write(int b) {
                    // do not write output
                }
            };
        }

        return out;
    }

    @Override
    public void createDirectories(String path) {
        // we do not need to separately create directories
    }

    @Override
    public void close() throws IOException {
        try {
            // wait for zip entries to be written
            scatterZipPool.shutdownAndWait();

            // merge scatter screams into final zip
            log.info("Merging temporary files to target ZIP file...");
            for (ScatterZipOutputStream scatterStream : scatterStreams) {
                scatterStream.writeTo(out);
                scatterStream.close();
            }
        } catch (InterruptedException e) {
            scatterZipPool.shutdownNow();
        } finally {
            out.close();
        }
    }

    private final class ScatterZipWorker extends DefaultWorker<ScatterZipWork> {
        private final ScatterZipOutputStream scatterStream;
        private final EventDispatcher eventDispatcher;

        private ScatterZipWorker(ScatterGatherBackingStoreSupplier supplier, EventDispatcher eventDispatcher) throws IOException {
            this.eventDispatcher = eventDispatcher;
            scatterStream = new ScatterZipOutputStream(supplier);
            scatterStreams.add(scatterStream);
        }

        @Override
        public void doWork(ScatterZipWork work) {
            try {
                scatterStream.addArchiveEntry(work.zipArchiveEntry, work.source);
            } catch (IOException e) {
                eventDispatcher.triggerSyncEvent(new InterruptEvent("Failed to write temporary zip archive.", LogLevel.ERROR, e, eventChannel, this));
                shouldRun = false;
            } finally {
                try {
                    work.source.get().close();
                } catch (IOException ignored) {
                    //
                }
            }
        }

        @Override
        public void shutdown() {
            // nothing to do
        }
    }

    private static final class ScatterZipWork {
        private final ZipArchiveEntry zipArchiveEntry;
        private final InputStreamSupplier source;

        private ScatterZipWork(ZipArchiveEntry zipArchiveEntry, InputStreamSupplier source) {
            this.zipArchiveEntry = zipArchiveEntry;
            this.source = source;
        }
    }
}

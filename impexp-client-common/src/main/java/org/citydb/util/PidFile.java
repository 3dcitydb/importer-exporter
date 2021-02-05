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

package org.citydb.util;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class PidFile {
    private final long pid;
    private final Path path;
    private final boolean deleteOnExit;

    private PidFile(Path path, boolean deleteOnExit, long pid) {
        this.path = path;
        this.deleteOnExit = deleteOnExit;
        this.pid = pid;
    }

    public static PidFile create(Path path, boolean deleteOnExit) throws IOException {
        return create(path, deleteOnExit, ProcessHandle.current().pid());
    }

    public static PidFile create(Path path, boolean deleteOnExit, long pid) throws IOException {
        path = path.normalize().toAbsolutePath();
        Path parent = path.getParent();

        if (parent != null) {
            if (Files.exists(parent) && !Files.isDirectory(parent))
                throw new IOException(parent + " exists but is not a directory.");

            if (!Files.exists(parent))
                Files.createDirectories(parent);
        }

        if (Files.exists(path) && !Files.isRegularFile(path))
            throw new IOException(path + " exists but is not a regular file.");

        try (OutputStream stream = Files.newOutputStream(path, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            stream.write(Long.toString(pid).getBytes(StandardCharsets.UTF_8));
        }

        if (deleteOnExit)
            addShutdownHook(path);

        return new PidFile(path, deleteOnExit, pid);
    }

    public long getPid() {
        return pid;
    }

    public Path getPath() {
        return path;
    }

    public boolean isDeleteOnExit() {
        return deleteOnExit;
    }

    private static void addShutdownHook(final Path path) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                Files.deleteIfExists(path);
            } catch (IOException e) {
                throw new RuntimeException("Failed to delete pid file " + path, e);
            }
        }));
    }
}

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

package org.citydb.file;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Objects;

public abstract class OutputFile implements AutoCloseable {
    protected final Path file;
    protected final FileType type;

    protected OutputFile(Path file, FileType type) {
        Objects.requireNonNull(file, "file must not be null.");
        this.file = file.toAbsolutePath().normalize();
        this.type = type;
    }

    public abstract OutputStream openStream() throws IOException;
    public abstract String resolve(String... paths) throws InvalidPathException;
    public abstract void createDirectories(String path) throws IOException;
    public abstract OutputStream newOutputStream(String file) throws IOException;

    @Override
    public abstract void close() throws IOException;

    public Path getFile() {
        return file;
    }

    public FileType getType() {
        return type;
    }
}

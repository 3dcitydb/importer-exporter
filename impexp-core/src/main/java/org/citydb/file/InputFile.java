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

import org.apache.tika.mime.MediaType;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Objects;

public abstract class InputFile implements AutoCloseable {
    protected final Path file;
    protected final FileType type;
    protected final MediaType mediaType;

    protected InputFile(Path file, FileType type, MediaType mediaType) {
        Objects.requireNonNull(file, "file must not be null.");
        this.file = file.toAbsolutePath().normalize();
        this.type = Objects.requireNonNull(type, "file type must not be null.");
        this.mediaType = Objects.requireNonNull(mediaType, "media type must not be null.");
    }

    public abstract InputStream openStream() throws IOException;
    public abstract Path resolve(String path) throws InvalidPathException;
    public abstract String getSeparator();

    @Override
    public abstract void close() throws IOException;

    public Path getFile() {
        return file;
    }

    public FileType getType() {
        return type;
    }

    public MediaType getMediaType() {
        return mediaType;
    }
}

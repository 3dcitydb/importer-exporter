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

package org.citydb.file.input;

import org.apache.tika.mime.MediaType;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Objects;

public class ZipInputFile extends AbstractArchiveInputFile {
    private final URI zipFileUri;
    private FileSystem fileSystem;

    ZipInputFile(String contentFile, Path zipFile, URI zipFileUri, MediaType mediaType) {
        super(contentFile, zipFile, mediaType);
        this.zipFileUri = Objects.requireNonNull(zipFileUri, "zip file URI must not be null.");
    }

    @Override
    public InputStream openStream() throws IOException {
        return new BufferedInputStream(Files.newInputStream(getFileSystem().getPath(contentFile)));
    }

    @Override
    public Path resolve(String path) throws InvalidPathException {
        return getFileSystem().getPath(contentFile).getParent().resolve(path);
    }

    @Override
    public String getSeparator() {
        return getFileSystem().getSeparator();
    }

    @Override
    public void close() throws IOException {
        // nothing to do here
    }

    private FileSystem getFileSystem() {
        if (fileSystem != null)
            return fileSystem;

        try {
            fileSystem = FileSystems.getFileSystem(zipFileUri);
        } catch (Throwable ignored) {
            //
        }

        if (fileSystem == null) {
            try {
                fileSystem = FileSystems.newFileSystem(zipFileUri, new HashMap<>());
            } catch (IOException e) {
                throw new IllegalStateException("Failed to open zip file system '" + zipFileUri + "'.", e);
            }
        }

        return fileSystem;
    }
}

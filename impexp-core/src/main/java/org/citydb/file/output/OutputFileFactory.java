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

import org.citydb.config.Config;
import org.citydb.file.FileType;
import org.citydb.file.OutputFile;
import org.citydb.event.EventDispatcher;
import org.citydb.util.Util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class OutputFileFactory {
    private final Config config;
    private final EventDispatcher eventDispatcher;
    private final Object eventChannel;

    public OutputFileFactory(Config config, EventDispatcher eventDispatcher, Object eventChannel) {
        this.config = config;
        this.eventDispatcher = eventDispatcher;
        this.eventChannel = eventChannel;
    }

    public OutputFileFactory(Config config, EventDispatcher eventDispatcher) {
        this(config, eventDispatcher, null);
    }

    public OutputFile createOutputFile(Path file) throws IOException {
        file = file.toAbsolutePath().normalize();
        Files.createDirectories(file.getParent());

        String extension = Util.getFileExtension(file.getFileName().toString());
        if (extension.isEmpty()) {
            extension = "gml";
            file = file.resolveSibling(file.getFileName() + ".gml");
        }

        switch (extension) {
            case "zip":
                return new ZipOutputFile(Util.stripFileExtension(file.getFileName().toString()) + ".gml",
                        file,
                        file.getParent(),
                        config.getProject().getExporter().getResources().getThreadPool().getDefaultPool().getMaxThreads(),
                        eventDispatcher,
                        eventChannel);
            case "gzip":
            case "gz":
                return new GZipOutputFile(file);
            default:
                return new XMLOutputFile(file);
        }
    }

    public FileType getFileType(Path file) {
        switch (Util.getFileExtension(file.getFileName().toString())) {
            case "zip":
                return FileType.ARCHIVE;
            case "gzip":
            case "gz":
                return FileType.COMPRESSED;
            default:
                return FileType.REGULAR;
        }
    }
}

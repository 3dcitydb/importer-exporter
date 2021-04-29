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

package org.citydb.file.output;

import org.citydb.config.Config;
import org.citydb.config.project.exporter.OutputFormat;
import org.citydb.event.EventDispatcher;
import org.citydb.file.FileType;
import org.citydb.file.OutputFile;
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

    public OutputFile createOutputFile(Path file, OutputFormat outputFormat) throws IOException {
        file = file.toAbsolutePath().normalize();
        Files.createDirectories(file.getParent());

        String extension = Util.getFileExtension(file);
        if (extension.isEmpty()) {
            extension = "gml";
            file = file.resolveSibling(file.getFileName() + ".gml");
        }

        switch (extension) {
            case "zip":
                extension = outputFormat == OutputFormat.CITYJSON ? ".json" : ".gml";
                return new ZipOutputFile(Util.stripFileExtension(file.getFileName().toString()) + extension,
                        file,
                        file.getParent(),
                        config.getExportConfig().getResources().getThreadPool().getMaxThreads(),
                        eventDispatcher,
                        eventChannel);
            case "gzip":
            case "gz":
                return new GZipOutputFile(file);
            default:
                return new RegularOutputFile(file);
        }
    }

    public static FileType getFileType(Path file) {
        switch (Util.getFileExtension(file)) {
            case "zip":
                return FileType.ARCHIVE;
            case "gzip":
            case "gz":
                return FileType.COMPRESSED;
            default:
                return FileType.REGULAR;
        }
    }

    public static OutputFormat getOutputFormat(Path file, Config config) {
        switch (Util.getFileExtension(file)) {
            case "json":
            case "cityjson":
                return OutputFormat.CITYJSON;
            case "zip":
            case "gzip":
            case "gz":
                return config.getExportConfig().getGeneralOptions().getCompressedOutputFormat();
        }

        return OutputFormat.CITYGML;
    }
}

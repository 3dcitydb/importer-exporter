/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2024
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

package org.citydb.core.file.input;

import org.apache.tika.config.TikaConfig;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MimeTypes;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;


public class FileTypeDetector {
    private final TikaConfig tikaConfig;


    public FileTypeDetector() throws TikaException, IOException {
        tikaConfig = new TikaConfig();
        // map additional file extensions to mime types
        tikaConfig.getMimeRepository().addPattern(MimeTypes.getDefaultMimeTypes().forName("application/json"), "*.cityjson");
    }

    public MediaType getMediaType(Path file) {
        try (InputStream stream = TikaInputStream.get(file)) {
            Metadata metadata = new Metadata();
            metadata.set(TikaCoreProperties.RESOURCE_NAME_KEY, file.toString());
            return tikaConfig.getDetector().detect(stream, metadata);
        } catch (IOException e) {
            return MediaType.EMPTY;
        }
    }

    public MediaType getMediaType(InputStream stream, String fileName) {
        try {
            Metadata metadata = new Metadata();
            if (fileName != null) {
                metadata.set(TikaCoreProperties.RESOURCE_NAME_KEY, fileName);
            }
            return tikaConfig.getDetector().detect(TikaInputStream.get(stream), metadata);
        } catch (IOException e) {
            return MediaType.EMPTY;
        }
    }
}

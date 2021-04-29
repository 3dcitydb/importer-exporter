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

package org.citydb.file.input;

import org.apache.tika.detect.Detector;
import org.apache.tika.io.LookaheadInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.citydb.file.InputFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class InputFileDetector implements Detector {

    @Override
    public MediaType detect(InputStream input, Metadata metadata) throws IOException {
        int bufferSize = 1024;
        try (LookaheadInputStream stream = new LookaheadInputStream(input, bufferSize)) {
            byte[] buffer = new byte[bufferSize];
            if (stream.read(buffer) > 0) {
                if (Arrays.stream(convertToString(buffer)).anyMatch(v -> v.contains("CityJSON"))) {
                    return InputFile.APPLICATION_JSON;
                }
            }
        }

        return MediaType.OCTET_STREAM;
    }

    private String[] convertToString(byte[] buffer) {
        // convert bytes to strings using default encodings
        String[] strings = new String[5];
        strings[0] = new String(buffer, StandardCharsets.UTF_8);
        strings[1] = new String(buffer, StandardCharsets.ISO_8859_1);
        strings[2] = new String(buffer, StandardCharsets.US_ASCII);
        strings[3] = new String(buffer, StandardCharsets.UTF_16);
        strings[4] = new String(buffer);
        return strings;
    }
}

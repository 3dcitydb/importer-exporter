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

package org.citydb.citygml.importer.util;

import org.citydb.file.FileType;
import org.citydb.file.InputFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.Map;

public class ExternalFileChecker {
    private final InputFile inputFile;
    private final boolean replaceSeparator;

    public ExternalFileChecker(InputFile inputFile) {
        this.inputFile = inputFile;
        replaceSeparator = inputFile != null && inputFile.getSeparator().equals("/");
    }

    public Map.Entry<String, String> getFileInfo(String imageURI) throws IOException {
        try {
            new URL(imageURI);
            return new AbstractMap.SimpleEntry<>(imageURI, imageURI);
        } catch (MalformedURLException ignored) {
            //
        }

        if (inputFile == null)
            throw new IOException("Base file path for resolving file references is null.");

        String path = null;
        Path file = null;
        if (replaceSeparator)
            imageURI = imageURI.replace("\\", "/");

        try {
            file = inputFile.resolve(imageURI);
            if (Files.exists(file))
                path = imageURI;
        } catch (InvalidPathException e) {
            //
        }

        if (path == null && inputFile.getType() == FileType.ARCHIVE) {
            try {
                file = inputFile.getFile().getParent().resolve(imageURI);
                if (Files.exists(file))
                    path = file.toString();
            } catch (InvalidPathException e) {
                //
            }
        }

        if (path == null)
            throw new IOException("Failed to find file.");

        if (Files.size(file) == 0)
            throw new IOException("Zero byte file.");

        return new AbstractMap.SimpleEntry<>(path, file.getFileName().toString());
    }

    public InputFile getInputFile() {
        return inputFile;
    }
}

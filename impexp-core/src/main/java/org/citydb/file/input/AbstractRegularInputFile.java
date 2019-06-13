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
import org.citydb.file.InputFile;
import org.citydb.file.FileType;

import java.nio.file.FileSystems;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

public abstract class AbstractRegularInputFile extends InputFile {

    AbstractRegularInputFile(Path file, MediaType mediaType, boolean isCompressed) {
        super(file, isCompressed ? FileType.COMPRESSED : FileType.REGULAR, mediaType);
    }

    @Override
    public Path resolve(String path) throws InvalidPathException {
        return file.getParent().resolve(path);
    }

    @Override
    public String getSeparator() {
        return FileSystems.getDefault().getSeparator();
    }

    @Override
    public void close() {
        // nothing to do here
    }
}

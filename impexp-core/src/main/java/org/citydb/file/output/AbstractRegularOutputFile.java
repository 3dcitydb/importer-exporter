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

import org.citydb.file.FileType;
import org.citydb.file.OutputFile;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class AbstractRegularOutputFile extends OutputFile {

    AbstractRegularOutputFile(Path file, boolean isCompressed) {
        super(file, isCompressed ? FileType.COMPRESSED : FileType.REGULAR);
    }

    @Override
    public String resolve(String... paths) {
        return Paths.get(file.getParent().toString(), paths).toString();
    }

    @Override
    public void createDirectories(String path) throws IOException {
        Files.createDirectories(file.getParent().resolve(path));
    }

    @Override
    public OutputStream newOutputStream(String file) throws IOException {
        return Files.newOutputStream(Paths.get(file) );
    }

    @Override
    public void close() {
        // nothing to do here
    }
}

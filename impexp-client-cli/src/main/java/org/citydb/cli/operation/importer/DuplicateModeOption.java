/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2022
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

package org.citydb.cli.operation.importer;

import org.citydb.cli.option.CliOption;
import org.citydb.config.project.importer.*;
import picocli.CommandLine;

import java.nio.file.Path;

public class DuplicateModeOption implements CliOption {
    enum Mode {import_all, skip, delete, terminate}

    @CommandLine.Option(names = {"-o", "--duplicate-mode"}, defaultValue = "import_all",
            description = "Mode for handling conflicting top-level features that already exist in the database: " +
                    "${COMPLETION-CANDIDATES} (default: ${DEFAULT-VALUE}).")
    private Mode mode;

    @CommandLine.Option(names = "--duplicate-log", paramLabel = "<file>",
            description = "Record already existing top-level features to this file. Use only if duplicate mode " +
                    "is not set to import_all.")
    private Path duplicateLogFile;

    public ImportMode toImportMode() {
        ImportMode importMode = new ImportMode();
        switch (mode) {
            case import_all:
                importMode.setOperation(OperationName.INSERT);
                importMode.setInsertMode(InsertMode.IMPORT_ALL);
                break;
            case skip:
                importMode.setOperation(OperationName.INSERT);
                importMode.setInsertMode(InsertMode.SKIP_EXISTING);
                break;
            case delete:
                importMode.setOperation(OperationName.OVERWRITE);
                importMode.setOverwriteMode(OverwriteMode.DELETE_EXISTING);
                break;
            case terminate:
                importMode.setOperation(OperationName.OVERWRITE);
                importMode.setOverwriteMode(OverwriteMode.TERMINATE_EXISTING);
                break;
        }

        return importMode;
    }

    public DuplicateLog toDuplicateLog() {
        DuplicateLog duplicateLog = new DuplicateLog();
        if (duplicateLogFile != null) {
            duplicateLog.setLogFile(duplicateLogFile.toAbsolutePath().toString());
            duplicateLog.setLogDuplicates(true);
        }

        return duplicateLog;
    }
}

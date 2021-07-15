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

package org.citydb.cli.operation.importer;

import org.citydb.cli.option.CliOption;
import org.citydb.cli.option.ResourceIdListOption;
import org.citydb.config.project.importer.ImportList;
import org.citydb.config.project.importer.ImportListMode;
import picocli.CommandLine;

import java.nio.file.Path;

public class ImportListOption implements CliOption {
    @CommandLine.Option(names = {"-f", "--import-list"}, required = true,
            description = "Name of the CSV file containing the import list.")
    private Path file;

    @CommandLine.Option(names = {"-m", "--import-list-mode"}, paramLabel = "<mode>", defaultValue = "import",
            description = "Import list mode: import, skip (default: ${DEFAULT-VALUE}).")
    private String modeOption;

    @CommandLine.Option(names = {"-w", "--import-list-preview"},
            description = "Print a preview of the import list and exit.")
    private boolean preview;

    @CommandLine.ArgGroup(exclusive = false)
    private ResourceIdListOption resourceIdListOption;

    private ImportListMode mode = ImportListMode.IMPORT;

    public boolean isPreview() {
        return preview;
    }

    public ImportList toImportList() {
        if (resourceIdListOption == null) {
            resourceIdListOption = new ResourceIdListOption();
        }

        ImportList importList = resourceIdListOption.toIdList(ImportList::new);
        importList.setFile(file.toAbsolutePath().toString());
        importList.setMode(mode);
        return importList;
    }

    @Override
    public void preprocess(CommandLine commandLine) {
        if (resourceIdListOption != null) {
            resourceIdListOption.preprocess(commandLine);
        }

        if (modeOption != null) {
            switch (modeOption.toLowerCase()) {
                case "import":
                    mode = ImportListMode.IMPORT;
                    break;
                case "skip":
                    mode = ImportListMode.SKIP;
                    break;
                default:
                    throw new CommandLine.ParameterException(commandLine, "Invalid value for option '--import-list-mode': " +
                            "expected one of [import, skip] (case-insensitive) but was '" + modeOption + "'");
            }
        }
    }
}

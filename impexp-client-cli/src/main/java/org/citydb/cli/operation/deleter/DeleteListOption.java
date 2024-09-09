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

package org.citydb.cli.operation.deleter;

import org.citydb.cli.option.CliOption;
import org.citydb.cli.option.IdListOption;
import org.citydb.config.project.common.IdList;
import picocli.CommandLine;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Collectors;

public class DeleteListOption implements CliOption {
    @CommandLine.Option(names = {"-f", "--delete-list"}, paramLabel = "<file>", required = true, split = ",",
            description = "One or more CSV files containing the delete list.")
    private Path[] files;

    @CommandLine.Option(names = {"-w", "--delete-list-preview"},
            description = "Print a preview of the delete list and exit. If more than one CSV file is specified, " +
                    "the preview is only generated for the first one.")
    private boolean preview;

    @CommandLine.ArgGroup(exclusive = false)
    private IdListOption deleteListOption;

    public boolean isPreview() {
        return preview;
    }

    public IdList toDeleteList() {
        if (deleteListOption == null) {
            deleteListOption = new IdListOption();
        }

        IdList deleteList = deleteListOption.toIdList(IdList::new);
        deleteList.setFiles(Arrays.stream(files).map(Path::toString).collect(Collectors.toList()));
        return deleteList;
    }

    @Override
    public void preprocess(CommandLine commandLine) {
        if (deleteListOption != null) {
            deleteListOption.preprocess(commandLine);
        }
    }
}

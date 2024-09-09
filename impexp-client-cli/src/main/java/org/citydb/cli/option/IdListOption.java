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

package org.citydb.cli.option;

import org.citydb.config.project.common.IdColumnType;
import org.citydb.config.project.common.IdList;
import picocli.CommandLine;

import java.util.function.Supplier;

public class IdListOption implements CliOption {
    enum Type {resource, db}

    @CommandLine.Option(names = {"-C", "--id-column-type"}, paramLabel = "<type>", defaultValue = "resource",
            description = "Type of id column value: ${COMPLETION-CANDIDATES} (default: ${DEFAULT-VALUE}).")
    private Type type;

    @CommandLine.ArgGroup(exclusive = false)
    private ResourceIdListOption resourceIdListOption;

    public <T extends IdList> T toIdList(Supplier<T> idListSupplier) {
        if (resourceIdListOption == null) {
            resourceIdListOption = new ResourceIdListOption();
        }

        T idList = resourceIdListOption.toIdList(idListSupplier);
        idList.setIdColumnType(type == Type.db ? IdColumnType.DATABASE_ID : IdColumnType.RESOURCE_ID);
        return idList;
    }

    @Override
    public void preprocess(CommandLine commandLine) {
        if (resourceIdListOption != null) {
            resourceIdListOption.preprocess(commandLine);
        }
    }
}

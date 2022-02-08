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
import org.citydb.cli.option.CliOptionBuilder;
import org.citydb.config.project.query.filter.counter.CounterFilter;
import picocli.CommandLine;

public class CounterOption implements CliOption {
    @CommandLine.Option(names = "--count",
            description = "Maximum number of top-level features to import.")
    private Long count;

    @CommandLine.Option(names = "--start-index", paramLabel = "<index>",
            description = "Index within the input set to import from.")
    private Long startIndex;

    private CounterFilter counterFilter;

    public CounterFilter toCounterFilter() {
        return counterFilter;
    }

    @Override
    public void preprocess(CommandLine commandLine) throws Exception {
        counterFilter = CliOptionBuilder.counterFilter(count, startIndex, commandLine);
    }
}

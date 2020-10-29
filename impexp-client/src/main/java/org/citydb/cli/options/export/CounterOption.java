/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2020
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

package org.citydb.cli.options.export;

import org.citydb.config.project.query.filter.counter.CounterFilter;
import org.citydb.plugin.cli.CliOption;
import picocli.CommandLine;

public class CounterOption implements CliOption {
    @CommandLine.Option(names = "--count",
            description = "Maximum number of top-level features to export.")
    private Long count;

    @CommandLine.Option(names = "--start-index", paramLabel = "<index>",
            description = "Index within the result set from which to export.")
    private Long startIndex;

    private CounterFilter counterFilter;

    public CounterFilter toCounterFilter() {
        return counterFilter;
    }

    @Override
    public void preprocess(CommandLine commandLine) throws Exception {
        counterFilter = new CounterFilter();
        if (count != null) {
            if (count < 0) {
                throw new CommandLine.ParameterException(commandLine,
                        "Error: Count must be a non-negative integer but was " + count);
            }

            counterFilter.setCount(count);
        }

        if (startIndex != null) {
            if (startIndex < 0) {
                throw new CommandLine.ParameterException(commandLine,
                        "Error: Start index must be a non-negative integer but was " + startIndex);
            }

            counterFilter.setStartIndex(startIndex);
        }
    }
}

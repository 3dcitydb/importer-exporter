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

package org.citydb.cli.options.importer;

import org.citydb.config.geometry.BoundingBox;
import org.citydb.config.project.importer.SimpleBBOXMode;
import org.citydb.config.project.importer.SimpleBBOXOperator;
import org.citydb.plugin.cli.CliOption;
import org.citydb.plugin.cli.CliOptionBuilder;
import picocli.CommandLine;

public class BoundingBoxOption implements CliOption {
    enum Mode {overlaps, within}

    @CommandLine.Option(names = {"-b", "--bbox"}, paramLabel = "<minx,miny,maxx,maxy[,srid]>", required = true,
            description = "Bounding box to use as spatial filter.")
    private String bbox;

    @CommandLine.Option(names = "--bbox-mode", paramLabel = "<mode>", defaultValue = "overlaps",
            description = "Bounding box filter mode: ${COMPLETION-CANDIDATES} (default: ${DEFAULT-VALUE}).")
    private Mode mode;

    private SimpleBBOXOperator bboxOperator;

    public SimpleBBOXOperator toBBOXOperator() {
        return bboxOperator;
    }

    @Override
    public void preprocess(CommandLine commandLine) throws Exception {
        BoundingBox envelope = CliOptionBuilder.boundingBox(bbox, commandLine);
        if (envelope != null) {
            bboxOperator = new SimpleBBOXOperator();
            bboxOperator.setExtent(envelope);
            bboxOperator.setMode(mode == Mode.within ? SimpleBBOXMode.WITHIN : SimpleBBOXMode.BBOX);
        }
    }
}

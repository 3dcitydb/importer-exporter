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

package org.citydb.cli.options.vis;

import org.citydb.config.geometry.BoundingBox;
import org.citydb.config.project.kmlExporter.KmlTiling;
import org.citydb.config.project.kmlExporter.KmlTilingMode;
import org.citydb.plugin.cli.CliOption;
import org.citydb.plugin.cli.CliOptionBuilder;
import picocli.CommandLine;

public class BoundingBoxOption implements CliOption {
    @CommandLine.Option(names = {"-b", "--bbox"}, paramLabel = "<minx,miny,maxx,maxy[,srid]>", required = true,
            description = "Bounding box to use as spatial filter.")
    private String bbox;

    @CommandLine.Option(names = {"-g", "--bbox-tiling"}, paramLabel = "<rows,columns | auto>",
            description = "Tile the bounding box into a rows x columns grid or automatically.")
    private String tiling;

    private KmlTiling kmlTiling;

    public KmlTiling toKmlTiling() {
        return kmlTiling;
    }

    @Override
    public void preprocess(CommandLine commandLine) throws Exception {
        BoundingBox envelope = CliOptionBuilder.boundingBox(bbox, commandLine);
        if (envelope != null) {
            kmlTiling = new KmlTiling();
            kmlTiling.setExtent(envelope);

            if (tiling != null) {
                if ("auto".equalsIgnoreCase(tiling)) {
                    kmlTiling.setMode(KmlTilingMode.AUTOMATIC);
                } else {
                    String[] numbers = tiling.split(",");
                    if (numbers.length != 2) {
                        throw new CommandLine.ParameterException(commandLine,
                                "Error: The value for '--bbox-tiling' is expected to be 'rows,columns' or 'automatic' " +
                                        "but was " + tiling);
                    }

                    try {
                        kmlTiling.setMode(KmlTilingMode.MANUAL);
                        kmlTiling.setRows(Integer.parseInt(numbers[0]));
                        kmlTiling.setColumns(Integer.parseInt(numbers[1]));
                    } catch (NumberFormatException e) {
                        throw new CommandLine.ParameterException(commandLine,
                                "Error: The number of rows and columns for tiling must be integers but were " +
                                        String.join(",", numbers));
                    }
                }
            } else {
                kmlTiling.setMode(KmlTilingMode.NO_TILING);
            }
        }
    }
}

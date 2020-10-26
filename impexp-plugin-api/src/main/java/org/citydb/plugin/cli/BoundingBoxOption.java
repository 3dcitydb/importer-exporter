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

package org.citydb.plugin.cli;

import org.citydb.config.geometry.BoundingBox;
import picocli.CommandLine;

public class BoundingBoxOption implements CliOption {
    @CommandLine.Option(names = "--bbox", paramLabel = "<minx,miny,maxx,maxy[,srid]>",
            description = "Bounding box filter to use.")
    private String bbox;

    @CommandLine.Spec
    private CommandLine.Model.CommandSpec spec;

    private BoundingBox boundingBox;

    public String getBBox() {
        return bbox;
    }

    public BoundingBox toBoundingBox() {
        return boundingBox;
    }

    @Override
    public void preprocess() throws Exception {
        if (bbox != null) {
            String[] parts = bbox.split(",");
            if (parts.length == 4 || parts.length == 5) {
                boundingBox = new BoundingBox();
                try {
                    boundingBox.getLowerCorner().setX(Double.parseDouble(parts[0]));
                    boundingBox.getLowerCorner().setY(Double.parseDouble(parts[1]));
                    boundingBox.getUpperCorner().setX(Double.parseDouble(parts[2]));
                    boundingBox.getUpperCorner().setY(Double.parseDouble(parts[3]));
                } catch (NumberFormatException e) {
                    throw new CommandLine.ParameterException(spec.commandLine(),
                            "The coordinates of a bounding box must be floating point numbers but were " +
                                    String.join(",", parts[0], parts[1], parts[2], parts[3]) + ".");
                }

                if (parts.length == 5) {
                    try {
                        boundingBox.setSrs(Integer.parseInt(parts[4]));
                    } catch (NumberFormatException e) {
                        throw new CommandLine.ParameterException(spec.commandLine(),
                                "The SRID of a bounding box must be an integer but was " + parts[4] + ".");
                    }
                }
            } else {
                throw new CommandLine.ParameterException(spec.commandLine(),
                        "A bounding box should be in MINX,MINY,MAXX,MAXY[,SRID] format but was " + bbox + ".");
            }
        }
    }
}

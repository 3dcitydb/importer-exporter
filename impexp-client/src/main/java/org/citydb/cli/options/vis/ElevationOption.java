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

import org.citydb.config.project.kmlExporter.AltitudeMode;
import org.citydb.config.project.kmlExporter.AltitudeOffsetMode;
import org.citydb.config.project.kmlExporter.Elevation;
import org.citydb.plugin.cli.CliOption;
import picocli.CommandLine;

public class ElevationOption implements CliOption {
    @CommandLine.Option(names = {"-A", "--altitude-mode"}, paramLabel = "<mode>", defaultValue = "absolute",
            description = "Altitude mode: ${COMPLETION-CANDIDATES} (default: ${DEFAULT-VALUE}).")
    private Mode mode;

    @CommandLine.Option(names = {"-O", "--altitude-offset"}, paramLabel = "<number|globe|generic>",
            description = "Apply offset to height values. Provide a <number> as constant offset, <globe> for zero " +
                    "elevation or <generic> to use the generic attribute GE_LoDn_zOffset as per-feature offset.")
    private String offsetOption;

    @CommandLine.Option(names = "--google-elevation-api", paramLabel = "<api-key>",
            description = "Query the Google elevation API when no GE_LoDn_zOffset attribute is available. " +
                    "Requires an API key.")
    private String googleApiKey;

    @CommandLine.Option(names = {"--transform-height"},
            description = "Transform height to WGS84 ellipsoid height (default: keep original height values).")
    private boolean transformHeight;

    private AltitudeOffsetMode offsetMode = AltitudeOffsetMode.NO_OFFSET;
    private double offset;

    public Elevation toElevation() {
        Elevation elevation = new Elevation();
        elevation.setAltitudeMode(mode.type);
        elevation.setUseOriginalZCoords(!transformHeight);
        elevation.setAltitudeOffsetMode(offsetMode);
        elevation.setAltitudeOffsetValue(offset);
        elevation.setCallGElevationService(offsetMode == AltitudeOffsetMode.GENERIC_ATTRIBUTE && googleApiKey != null);

        return elevation;
    }

    public String getGoogleApiKey() {
        return googleApiKey;
    }

    enum Mode {
        absolute(AltitudeMode.ABSOLUTE),
        relative(AltitudeMode.RELATIVE),
        clamp(AltitudeMode.CLAMP_TO_GROUND);

        private final AltitudeMode type;

        Mode(AltitudeMode type) {
            this.type = type;
        }
    }

    @Override
    public void preprocess(CommandLine commandLine) throws Exception {
        if (offsetOption != null) {
            if ("globe".equalsIgnoreCase(offsetOption)) {
                offsetMode = AltitudeOffsetMode.BOTTOM_ZERO;
            } else if ("generic".equalsIgnoreCase(offsetOption)) {
                offsetMode = AltitudeOffsetMode.GENERIC_ATTRIBUTE;
            } else {
                try {
                    offset = Double.parseDouble(offsetOption);
                    offsetMode = AltitudeOffsetMode.CONSTANT;
                } catch (NumberFormatException e) {
                    throw new CommandLine.ParameterException(commandLine, "Invalid value for option '--altitude-offset': " +
                            "expected a number as constant offset or one of [globe, generic] (case-insensitive) " +
                            "but was '" + offsetOption + "'");
                }
            }
        }

        if (googleApiKey != null && offsetMode != AltitudeOffsetMode.GENERIC_ATTRIBUTE) {
            throw new CommandLine.ParameterException(commandLine, "Error: --google-elevation-service requires " +
                    "--altitude-offset to be set to 'generic'.");
        }
    }
}

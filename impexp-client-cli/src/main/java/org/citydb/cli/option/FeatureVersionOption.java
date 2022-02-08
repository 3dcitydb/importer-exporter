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

package org.citydb.cli.option;

import org.citydb.config.project.query.simple.SimpleFeatureVersionFilter;
import org.citydb.config.project.query.simple.SimpleFeatureVersionFilterMode;
import org.citydb.core.registry.ObjectRegistry;
import picocli.CommandLine;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.LocalTime;
import java.time.ZonedDateTime;

public class FeatureVersionOption implements CliOption {
    @CommandLine.Option(names = {"-r", "--feature-version"}, required = true, defaultValue = "latest",
            description = "Feature version: ${COMPLETION-CANDIDATES} (default: ${DEFAULT-VALUE}).")
    private Version version;

    @CommandLine.Option(names = {"-R", "--feature-version-timestamp"}, paramLabel = "<timestamp[,timestamp]>",
            description = "Timestamp given as date <YYYY-MM-DD> or date-time <YYYY-MM-DDThh:mm:ss[(+|-)hh:mm]> with " +
                    "optional UTC offset. Use one timestamp with 'at' and 'terminated_at', and two timestamps " +
                    "defining a time range with 'between'.")
    private String timestamp;

    private XMLGregorianCalendar startDateTime;
    private XMLGregorianCalendar endDateTime;
    private SimpleFeatureVersionFilter featureVersionFilter;

    public static SimpleFeatureVersionFilter defaultFeatureVersionFilter() {
        return new SimpleFeatureVersionFilter();
    }

    public SimpleFeatureVersionFilter toFeatureVersionFilter() {
        if (featureVersionFilter != null) {
            if (startDateTime != null) {
                featureVersionFilter.setStartDate(withTimeZone(startDateTime));
            }

            if (endDateTime != null) {
                featureVersionFilter.setEndDate(withTimeZone(endDateTime));
            }
        }

        return featureVersionFilter;
    }

    @Deprecated
    public SimpleFeatureVersionFilter toFeatureVersionFilter(DatatypeFactory datatypeFactory) {
        return toFeatureVersionFilter();
    }

    enum Version {
        latest(SimpleFeatureVersionFilterMode.LATEST),
        at(SimpleFeatureVersionFilterMode.AT),
        between(SimpleFeatureVersionFilterMode.BETWEEN),
        terminated(SimpleFeatureVersionFilterMode.TERMINATED),
        terminated_at(SimpleFeatureVersionFilterMode.TERMINATED_AT),
        all(null);

        private final SimpleFeatureVersionFilterMode mode;

        Version(SimpleFeatureVersionFilterMode mode) {
            this.mode = mode;
        }
    }

    @Override
    public void preprocess(CommandLine commandLine) throws Exception {
        if (version == Version.all) {
            if (timestamp != null) {
                throw new CommandLine.ParameterException(commandLine,
                        "Error: The feature version '" + version + "' does not take a timestamp");
            } else {
                // no filter required to query all feature versions
                return;
            }
        }

        featureVersionFilter = new SimpleFeatureVersionFilter();
        featureVersionFilter.setMode(version.mode);

        if (timestamp != null) {
            if (version == Version.latest || version == Version.terminated) {
                throw new CommandLine.ParameterException(commandLine,
                        "Error: The feature version '" + version + "' does not take a timestamp");
            }

            String[] timestamps = timestamp.split(",");

            if ((version == Version.at || version == Version.terminated_at) && timestamps.length != 1) {
                throw new CommandLine.ParameterException(commandLine,
                        "Error: The feature version '" + version + "' requires only one timestamp");
            } else if (version == Version.between && timestamps.length != 2) {
                throw new CommandLine.ParameterException(commandLine,
                        "Error: The feature version '" + version + "' requires two timestamps defining a time range");
            }

            DatatypeFactory factory = ObjectRegistry.getInstance().getDatatypeFactory();
            for (int i = 0; i < timestamps.length; i++) {
                XMLGregorianCalendar dateTime;
                try {
                    dateTime = factory.newXMLGregorianCalendar(timestamps[i]);
                    if (dateTime.getXMLSchemaType() == DatatypeConstants.DATE) {
                        dateTime.setTime(LocalTime.MAX.getHour(),
                                LocalTime.MAX.getMinute(),
                                LocalTime.MAX.getSecond());
                    }
                } catch (Exception e) {
                    throw new CommandLine.ParameterException(commandLine,
                            "A feature version timestamp must be in YYYY-MM-DD or YYYY-MM-DDThh:mm:ss[(+|-)hh:mm] " +
                                    "format but was '" + timestamps[i] + "'");
                }

                if (i == 0) {
                    startDateTime = dateTime;
                } else {
                    ZonedDateTime before = startDateTime.toGregorianCalendar().toZonedDateTime();
                    ZonedDateTime after = dateTime.toGregorianCalendar().toZonedDateTime();

                    if (!after.isAfter(before)) {
                        throw new CommandLine.ParameterException(commandLine,
                                "Error: The start timestamp must be lesser than the end timestamp");
                    }

                    endDateTime = dateTime;
                }
            }
        } else {
            if (version == Version.at) {
                throw new CommandLine.ParameterException(commandLine,
                        "Error: The feature version '" + version + "' requires a timestamp");
            } else if (version == Version.between) {
                throw new CommandLine.ParameterException(commandLine,
                        "Error: The feature version '" + version + "' requires a start and an end timestamp");
            }
        }
    }

    private XMLGregorianCalendar withTimeZone(XMLGregorianCalendar dateTime) {
        if (dateTime.getTimezone() == DatatypeConstants.FIELD_UNDEFINED) {
            dateTime.setTimezone(0);
        }

        return dateTime;
    }
}

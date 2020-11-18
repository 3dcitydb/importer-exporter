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
import org.citydb.config.project.query.filter.counter.CounterFilter;
import org.citydb.config.project.query.filter.selection.id.DatabaseIdOperator;
import org.citydb.config.project.query.filter.selection.id.ResourceIdOperator;
import org.citydb.config.project.query.filter.selection.sql.SelectOperator;
import org.citydb.config.project.query.filter.type.FeatureTypeFilter;
import org.citydb.config.project.resources.ThreadPool;
import org.citygml4j.model.module.Module;
import org.citygml4j.model.module.Modules;
import org.citygml4j.model.module.citygml.CityGMLVersion;
import org.citygml4j.xml.CityGMLNamespaceContext;
import picocli.CommandLine;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class CliOptionBuilder {

    public static List<Path> inputFiles(String[] files, Path baseDir) throws IOException {
        if (files != null) {
            List<Path> inputFiles = new ArrayList<>();

            for (String file : files) {
                LinkedList<String> elements = parseInputFile(file, baseDir);
                Path path = Paths.get(elements.pop());

                if (elements.isEmpty()) {
                    inputFiles.add(path);
                } else {
                    // construct a glob pattern from the path and the truncated elements
                    StringBuilder glob = new StringBuilder("glob:").append(path.toAbsolutePath().normalize());
                    glob.append(File.separator).append(String.join(File.separator, elements));

                    PathMatcher matcher = FileSystems.getDefault().getPathMatcher(glob.toString().replace("\\", "\\\\"));
                    try (Stream<Path> stream = Files.walk(path)) {
                        stream.forEach(p -> {
                            if (matcher.matches(p.toAbsolutePath().normalize())) {
                                inputFiles.add(p);
                            }
                        });
                    }
                }
            }

            return inputFiles;
        }

        return Collections.emptyList();
    }

    public static BoundingBox boundingBox(String bbox, CommandLine commandLine) {
        if (bbox != null) {
            String[] parts = bbox.split(",");
            if (parts.length == 4 || parts.length == 5) {
                BoundingBox boundingBox = new BoundingBox();
                try {
                    boundingBox.getLowerCorner().setX(Double.parseDouble(parts[0]));
                    boundingBox.getLowerCorner().setY(Double.parseDouble(parts[1]));
                    boundingBox.getUpperCorner().setX(Double.parseDouble(parts[2]));
                    boundingBox.getUpperCorner().setY(Double.parseDouble(parts[3]));
                } catch (NumberFormatException e) {
                    throw new CommandLine.ParameterException(commandLine,
                            "Error: The coordinates of a bounding box must be floating point numbers but were '" +
                                    String.join(",", parts[0], parts[1], parts[2], parts[3]) + "'");
                }

                if (parts.length == 5) {
                    try {
                        boundingBox.setSrs(Integer.parseInt(parts[4]));
                    } catch (NumberFormatException e) {
                        throw new CommandLine.ParameterException(commandLine,
                                "Error: The SRID of a bounding box must be an integer but was '" + parts[4] + "'");
                    }
                }

                return boundingBox;
            } else {
                throw new CommandLine.ParameterException(commandLine,
                        "A bounding box must be in MINX,MINY,MAXX,MAXY[,SRID] format but was '" + bbox + "'");
            }
        }

        return null;
    }

    public static NamespaceContext namespaceContext(Map<String, String> namespaces) {
        CityGMLNamespaceContext namespaceContext = new CityGMLNamespaceContext();
        Modules.getADEModules().forEach(m -> namespaceContext.setPrefix(m.getNamespacePrefix(), m.getNamespaceURI()));
        namespaceContext.setPrefixes(CityGMLVersion.v2_0_0);

        if (namespaces != null) {
            namespaces.forEach(namespaceContext::setPrefix);
        }

        return namespaceContext;
    }

    public static FeatureTypeFilter featureTypeFilter(String[] typeNames, NamespaceContext namespaceContext, CommandLine commandLine) {
        if (typeNames != null) {
            FeatureTypeFilter featureTypeFilter = new FeatureTypeFilter();
            for (String typeName : typeNames) {
                String[] parts = typeName.split(":");
                if (parts.length == 2) {
                    String namespace = namespaceContext.getNamespaceURI(parts[0]);
                    if (namespace.equals(XMLConstants.NULL_NS_URI)) {
                        throw new CommandLine.ParameterException(commandLine, "Error: Unknown prefix '" + parts[0] + "'");
                    }

                    Module module = Modules.getModule(namespace);
                    if (module == null || !module.hasFeature(parts[1])) {
                        throw new CommandLine.ParameterException(commandLine, "Error: Unknown type name '" + typeName + "'");
                    }

                    featureTypeFilter.addTypeName(new QName(namespace, parts[1]));
                } else if (parts.length == 1) {
                    Stream.concat(CityGMLVersion.v2_0_0.getCityGMLModules().stream(),
                            Modules.getADEModules().stream())
                            .filter(m -> m.hasFeature(parts[0]))
                            .forEach(m -> featureTypeFilter.addTypeName(new QName(m.getNamespaceURI(), parts[0])));

                    if (featureTypeFilter.isEmpty()) {
                        throw new CommandLine.ParameterException(commandLine,
                                "Error: Unknown type name '" + typeName + "'. Maybe use a prefix?");
                    }
                } else {
                    throw new CommandLine.ParameterException(commandLine,
                            "A type name must be in [PREFIX:]NAME format but was '" + typeName + "'");
                }
            }

            if (!featureTypeFilter.isEmpty()) {
                return featureTypeFilter;
            }
        }

        return null;
    }

    public static ResourceIdOperator resourceIdOperator(String[] ids) {
        if (ids != null) {
            ResourceIdOperator idOperator = new ResourceIdOperator();
            Arrays.stream(ids).forEach(idOperator::addResourceId);
            return idOperator;
        }

        return null;
    }

    public static DatabaseIdOperator databaseIdOperator(Long[] ids, CommandLine commandLine) {
        if (ids != null) {
            DatabaseIdOperator idOperator = new DatabaseIdOperator();
            for (Long id : ids) {
                if (id <= 0) {
                    throw new CommandLine.ParameterException(commandLine,
                            "Error: A database ID must be a positive integer but was '" + id + "'");
                }

                idOperator.addDatabaseId(id);
            }

            return idOperator;
        }

        return null;
    }

    public static ThreadPool threadPool(String threads, CommandLine commandLine) {
        if (threads != null) {
            String[] limits = threads.split(",");
            if (limits.length == 0 || limits.length > 2) {
                throw new CommandLine.ParameterException(commandLine,
                        "Error: The number of threads must be in THREADS[,MAX] format but was '" + threads + "'");
            }

            int i = 0, lower, upper;
            try {
                lower = Integer.parseInt(limits[i]);
                upper = limits.length == 2 ? Integer.parseInt(limits[++i]) : lower;
            } catch (NumberFormatException e) {
                throw new CommandLine.ParameterException(commandLine,
                        "Error: The number of threads must be a positive integer but was '" + limits[i] + "'");
            }

            if (lower <= 0) {
                throw new CommandLine.ParameterException(commandLine,
                        "Error: The number of threads must be a positive integer but was '" + lower + "'");
            }

            if (upper < lower) {
                throw new CommandLine.ParameterException(commandLine,
                        "Error: The upper limit of threads must be greater than or equal to the lower limit");
            }

            ThreadPool threadPool = new ThreadPool();
            threadPool.setMinThreads(lower);
            threadPool.setMaxThreads(upper);
            return threadPool;
        }

        return null;
    }

    public static SelectOperator selectOperator(String select) {
        if (select != null) {
            SelectOperator selectOperator = new SelectOperator();
            selectOperator.setValue(select);
            return selectOperator;
        }

        return null;
    }

    public static CounterFilter counterFilter(Long count, Long startIndex, CommandLine commandLine) {
        if (count != null || startIndex != null) {
            CounterFilter counterFilter = new CounterFilter();
            if (count != null) {
                if (count < 0) {
                    throw new CommandLine.ParameterException(commandLine,
                            "Error: Count must be a non-negative integer but was '" + count + "'");
                }

                counterFilter.setCount(count);
            }

            if (startIndex != null) {
                if (startIndex < 0) {
                    throw new CommandLine.ParameterException(commandLine,
                            "Error: Start index must be a non-negative integer but was '" + startIndex + "'");
                }

                counterFilter.setStartIndex(startIndex);
            }

            return counterFilter;
        }

        return null;
    }

    private static LinkedList<String> parseInputFile(String file, Path baseDir) {
        LinkedList<String> elements = new LinkedList<>();
        Path path = null;

        do {
            try {
                path = Paths.get(file);
            } catch (Exception e) {
                // the file is not a valid path, possibly because of glob patterns.
                // so, let's iteratively truncate the last path element and try again.
                int index = file.lastIndexOf(File.separator);
                String pathElement = file.substring(index + 1);
                file = file.substring(0, index != -1 ? index : 0);

                // remember the truncated element
                elements.addFirst(pathElement);
            }
        } while (path == null && file.length() > 0);

        // resolve path against the working directory
        path = path == null ? baseDir : baseDir.resolve(path);
        elements.addFirst(path.toAbsolutePath().toString());

        return elements;
    }
}

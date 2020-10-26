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

import org.citygml4j.model.module.Module;
import org.citygml4j.model.module.ModuleContext;
import org.citygml4j.model.module.Modules;
import org.citygml4j.model.module.citygml.CityGMLVersion;
import org.citygml4j.xml.CityGMLNamespaceContext;
import picocli.CommandLine;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class QueryOptions {
    @CommandLine.Option(names = "--type-names", split = ",", paramLabel = "<[prefix:]name>",
            description = "Names of the top-level features to be exported.")
    private String[] typeNames;

    @CommandLine.Option(names = "--namespaces", split = ",", paramLabel = "<prefix=name>",
            description = "Namespaces and their prefixes used in the query.")
    private Map<String, String> namespaces;

    @CommandLine.Spec
    private CommandLine.Model.CommandSpec spec;

    private final Set<QName> featureTypes = new HashSet<>();

    public void validate() throws CommandLine.ParameterException {
        if (typeNames != null) {
            CityGMLNamespaceContext namespaceContext = processNamespaces();
            processTypeNames(namespaceContext);
        }
    }

    private CityGMLNamespaceContext processNamespaces() {
        CityGMLNamespaceContext namespaceContext = new CityGMLNamespaceContext();
        namespaceContext.setPrefixes(new ModuleContext(CityGMLVersion.v2_0_0));
        if (namespaces != null) {
            namespaces.forEach(namespaceContext::setPrefix);
        }

        return namespaceContext;
    }

    private void processTypeNames(CityGMLNamespaceContext namespaceContext) {
        for (String typeName : typeNames) {
            String[] parts = typeName.split(":");
            if (parts.length == 2) {
                String namespace = namespaceContext.getNamespaceURI(parts[0]);
                if (namespace.equals(XMLConstants.NULL_NS_URI)) {
                    throw new CommandLine.ParameterException(spec.commandLine(), "Failed to map the prefix " + parts[0] +
                            " to a namespace.\nPossible solutions: --namespaces");
                }

                featureTypes.add(new QName(namespace, parts[1]));
            } else if (parts.length == 1) {
                Module module = Stream.concat(CityGMLVersion.v2_0_0.getCityGMLModules().stream(),
                        Modules.getADEModules().stream())
                        .filter(m -> m.hasFeature(parts[0]))
                        .findFirst()
                        .orElseThrow(() -> new CommandLine.ParameterException(spec.commandLine(), "Failed to find " +
                                "a namespace for the type name " + typeName + "\nPossible solutions: --namespaces"));

                featureTypes.add(new QName(module.getNamespaceURI(), parts[0]));
            } else {
                throw new CommandLine.ParameterException(spec.commandLine(), "A typename should be in [PREFIX:]NAME " +
                        "format but was " + typeName + ".");
            }
        }
    }
}

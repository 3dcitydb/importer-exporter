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

import org.citydb.config.project.query.filter.type.FeatureTypeFilter;
import org.citygml4j.model.module.Module;
import org.citygml4j.model.module.Modules;
import org.citygml4j.model.module.citygml.CityGMLVersion;
import org.citygml4j.xml.CityGMLNamespaceContext;
import picocli.CommandLine;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import java.util.Map;
import java.util.stream.Stream;

public class TypeNamesOption implements CliOption {
    @CommandLine.Option(names = "--type-names", split = ",", paramLabel = "<[prefix:]name>",
            description = "Names of the top-level features to process.")
    private String[] typeNames;

    @CommandLine.Option(names = "--namespaces", split = ",", paramLabel = "<prefix=name>",
            description = "Definition of namespaces and their prefixes.")
    private Map<String, String> namespaces;

    @CommandLine.Spec
    private CommandLine.Model.CommandSpec spec;

    private FeatureTypeFilter featureTypeFilter;
    private CityGMLNamespaceContext namespaceContext;

    public String[] getTypeNames() {
        return typeNames;
    }

    public Map<String, String> getNamespaces() {
        return namespaces;
    }

    private NamespaceContext toNamespaceContext() {
        return namespaceContext;
    }

    public FeatureTypeFilter toFeatureTypeFilter() {
        return featureTypeFilter;
    }

    @Override
    public void preprocess() throws Exception {
        namespaceContext = new CityGMLNamespaceContext();
        Modules.getADEModules().forEach(m -> namespaceContext.setPrefix(m.getNamespacePrefix(), m.getNamespaceURI()));
        namespaceContext.setPrefixes(CityGMLVersion.v2_0_0);

        if (namespaces != null) {
            namespaces.forEach(namespaceContext::setPrefix);
        }

        if (typeNames != null) {
            FeatureTypeFilter featureTypeFilter = new FeatureTypeFilter();
            for (String typeName : typeNames) {
                String[] parts = typeName.split(":");
                if (parts.length == 2) {
                    String namespace = namespaceContext.getNamespaceURI(parts[0]);
                    if (namespace.equals(XMLConstants.NULL_NS_URI)) {
                        throw new CommandLine.ParameterException(spec.commandLine(),
                                "Unknown prefix: " + parts[0] + "\nPossible solutions: --namespaces");
                    }

                    Module module = Modules.getModule(namespace);
                    if (module == null || !module.hasFeature(parts[1])) {
                        throw new CommandLine.ParameterException(spec.commandLine(), "Unknown type name: " + typeName);
                    }

                    featureTypeFilter.addTypeName(new QName(namespace, parts[1]));
                } else if (parts.length == 1) {
                    Module module = Stream.concat(CityGMLVersion.v2_0_0.getCityGMLModules().stream(),
                            Modules.getADEModules().stream())
                            .filter(m -> m.hasFeature(parts[0]))
                            .findFirst()
                            .orElseThrow(() -> new CommandLine.ParameterException(spec.commandLine(),
                                    "Unknown type name: " + typeName + "\nPossible solutions: --namespaces"));

                    featureTypeFilter.addTypeName(new QName(module.getNamespaceURI(), parts[0]));
                } else {
                    throw new CommandLine.ParameterException(spec.commandLine(),
                            "A type name should be in [PREFIX:]NAME format but was " + typeName + ".");
                }
            }

            if (!featureTypeFilter.isEmpty()) {
                this.featureTypeFilter = featureTypeFilter;
            }
        }
    }
}

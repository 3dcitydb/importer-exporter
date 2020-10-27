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

import org.citydb.config.ConfigUtil;
import org.citydb.config.geometry.BoundingBox;
import org.citydb.config.project.query.QueryConfig;
import org.citydb.config.project.query.filter.type.FeatureTypeFilter;
import org.citydb.config.util.QueryWrapper;
import org.citygml4j.model.module.Module;
import org.citygml4j.model.module.ModuleContext;
import org.citygml4j.model.module.Modules;
import org.citygml4j.model.module.citygml.CityGMLVersion;
import org.citygml4j.xml.CityGMLNamespaceContext;
import org.xml.sax.SAXException;
import picocli.CommandLine;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.StringReader;
import java.util.Map;
import java.util.stream.Stream;

public class CliOptionBuilder {

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
                            "The coordinates of a bounding box must be floating point numbers but were " +
                                    String.join(",", parts[0], parts[1], parts[2], parts[3]) + ".");
                }

                if (parts.length == 5) {
                    try {
                        boundingBox.setSrs(Integer.parseInt(parts[4]));
                    } catch (NumberFormatException e) {
                        throw new CommandLine.ParameterException(commandLine,
                                "The SRID of a bounding box must be an integer but was " + parts[4] + ".");
                    }
                }

                return boundingBox;
            } else {
                throw new CommandLine.ParameterException(commandLine,
                        "A bounding box should be in MINX,MINY,MAXX,MAXY[,SRID] format but was " + bbox + ".");
            }
        }

        return null;
    }

    public static NamespaceContext namespaceContext(Map<String, String> namespaces, CommandLine commandLine) {
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
                        throw new CommandLine.ParameterException(commandLine, "Unknown prefix: " + parts[0]);
                    }

                    Module module = Modules.getModule(namespace);
                    if (module == null || !module.hasFeature(parts[1])) {
                        throw new CommandLine.ParameterException(commandLine, "Unknown type name: " + typeName);
                    }

                    featureTypeFilter.addTypeName(new QName(namespace, parts[1]));
                } else if (parts.length == 1) {
                    Module module = Stream.concat(CityGMLVersion.v2_0_0.getCityGMLModules().stream(),
                            Modules.getADEModules().stream())
                            .filter(m -> m.hasFeature(parts[0]))
                            .findFirst()
                            .orElseThrow(() -> new CommandLine.ParameterException(commandLine,
                                    "Unknown type name: " + typeName + ". Maybe use a prefix?"));

                    featureTypeFilter.addTypeName(new QName(module.getNamespaceURI(), parts[0]));
                } else {
                    throw new CommandLine.ParameterException(commandLine,
                            "A type name should be in [PREFIX:]NAME format but was " + typeName + ".");
                }
            }

            if (!featureTypeFilter.isEmpty()) {
                return featureTypeFilter;
            }
        }

        return null;
    }

    public static QueryConfig xmlQueryConfig(String xmlQuery, CommandLine commandLine) {
        if (xmlQuery != null) {
            try {
                SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                Schema schema = schemaFactory.newSchema(CliOptionBuilder.class.getResource("/org/citydb/config/schema/query.xsd"));
                Unmarshaller unmarshaller = ConfigUtil.getInstance().getJAXBContext().createUnmarshaller();
                unmarshaller.setSchema(schema);

                StringBuilder wrapper = new StringBuilder("<wrapper xmlns=\"")
                        .append(ConfigUtil.CITYDB_CONFIG_NAMESPACE_URI).append("\" ");

                ModuleContext context = new ModuleContext(CityGMLVersion.v2_0_0);
                for (Module module : context.getModules()) {
                    wrapper.append("xmlns:")
                            .append(module.getNamespacePrefix()).append("=\"")
                            .append(module.getNamespaceURI()).append("\" ");
                }

                wrapper.append(">\n")
                        .append(xmlQuery)
                        .append("</wrapper>");

                Object object = unmarshaller.unmarshal(new StringReader(wrapper.toString()));
                if (object instanceof QueryWrapper) {
                    return ((QueryWrapper) object).getQueryConfig();
                }
            } catch (JAXBException | SAXException e) {
                throw new CommandLine.ParameterException(commandLine,
                        "An XML query must validate against the XML schema definition.");
            }
        }

        return null;
    }
}

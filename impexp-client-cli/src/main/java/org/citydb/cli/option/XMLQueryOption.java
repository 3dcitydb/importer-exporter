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

import org.citydb.util.config.ConfigUtil;
import org.citydb.config.project.query.QueryConfig;
import org.citydb.config.util.ConfigConstants;
import org.citydb.config.project.query.QueryWrapper;
import org.citygml4j.model.module.Module;
import org.citygml4j.model.module.ModuleContext;
import org.citygml4j.model.module.citygml.CityGMLVersion;
import org.xml.sax.SAXException;
import picocli.CommandLine;

import javax.xml.XMLConstants;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.StringReader;

public class XMLQueryOption implements CliOption {
    @CommandLine.Option(names = {"-q", "--xml-query"}, paramLabel = "<xml>",
            description = "XML query expression to use as database query.")
    private String xmlQuery;

    private QueryConfig queryConfig;

    public String getXMLQuery() {
        return xmlQuery;
    }

    public QueryConfig toQueryConfig() {
        try {
            Unmarshaller unmarshaller = ConfigUtil.getInstance().getJAXBContext().createUnmarshaller();
            Object object = unmarshaller.unmarshal(new StringReader(wrapQuery(xmlQuery)));
            if (object instanceof QueryWrapper) {
                return ((QueryWrapper) object).getQueryConfig();
            }
        } catch (Exception e) {
            //
        }

        return null;
    }

    @Override
    public void preprocess(CommandLine commandLine) throws Exception {
        try {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(getClass().getResource("/org/citydb/config/schema/query.xsd"));
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(new StringReader(wrapQuery(xmlQuery))));
        } catch (SAXException | IOException e) {
            throw new CommandLine.ParameterException(commandLine,
                    "Error: An XML query must validate against the XML schema definition.");
        }
    }

    private String wrapQuery(String query) {
        StringBuilder wrapper = new StringBuilder("<wrapper xmlns=\"")
                .append(ConfigConstants.CITYDB_CONFIG_NAMESPACE_URI)
                .append("\" ");

        ModuleContext context = new ModuleContext(CityGMLVersion.v2_0_0);
        for (Module module : context.getModules()) {
            wrapper.append("xmlns:")
                    .append(module.getNamespacePrefix()).append("=\"")
                    .append(module.getNamespaceURI()).append("\" ");
        }

        return wrapper.append(">\n")
                .append(query)
                .append("</wrapper>").toString();
    }
}

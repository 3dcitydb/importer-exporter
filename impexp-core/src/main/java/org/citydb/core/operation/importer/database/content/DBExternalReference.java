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
package org.citydb.core.operation.importer.database.content;

import org.citydb.config.Config;
import org.citydb.core.database.schema.SequenceEnum;
import org.citydb.core.database.schema.TableEnum;
import org.citydb.core.operation.common.property.ComplexProperty;
import org.citydb.core.operation.common.property.StringProperty;
import org.citydb.core.operation.common.property.UriProperty;
import org.citydb.core.operation.importer.CityGMLImportException;
import org.citygml4j.model.citygml.core.ExternalObject;
import org.citygml4j.model.citygml.core.ExternalReference;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class DBExternalReference implements DBImporter {
	private final CityGMLImportManager importer;
	private final DBProperty propertyImporter;

	public DBExternalReference(Connection batchConn, Config config, CityGMLImportManager importer) throws SQLException, CityGMLImportException {
		this.importer = importer;
		this.propertyImporter = importer.getImporter(DBProperty.class);
	}

	protected void doImport(ExternalReference externalReference, long cityObjectId) throws CityGMLImportException, SQLException {
		ComplexProperty externalReferenceProperty = new ComplexProperty();
		externalReferenceProperty.setName("externalReference");
		externalReferenceProperty.setDataType("ExternalReference");
		externalReferenceProperty.setNamespace("core");

		// core:informationSystem
		if (externalReference.isSetInformationSystem()){
			UriProperty informationSystem = new UriProperty();
			informationSystem.setName("informationSystem");
			informationSystem.setNamespace("core");
			informationSystem.setDataType("xs:anyUri");
			informationSystem.setValue(externalReference.getInformationSystem());
			externalReferenceProperty.addChild(informationSystem);
		}

		// core:externalObject
		if (externalReference.isSetExternalObject()) {
			ExternalObject externalObject = externalReference.getExternalObject();
			ComplexProperty externalObjectProperty = new ComplexProperty();
			externalObjectProperty.setName("externalObject");
			externalObjectProperty.setDataType("ExternalObjectReference");
			externalObjectProperty.setNamespace("core");
			externalReferenceProperty.addChild(externalObjectProperty);

			// core:name
			if (externalObject.isSetName()) {
				StringProperty externalObjectName = new StringProperty();
				externalObjectName.setName("name");
				externalObjectName.setNamespace("core");
				externalObjectName.setDataType("xs:string");
				externalObjectName.setValue(externalObject.getName());
				externalObjectProperty.addChild(externalObjectName);
			}

			// core:uri
			if (externalObject.isSetUri()) {
				UriProperty externalObjectName = new UriProperty();
				externalObjectName.setName("uri");
				externalObjectName.setNamespace("core");
				externalObjectName.setDataType("xs:string");
				externalObjectName.setValue(externalObject.getUri());
				externalObjectProperty.addChild(externalObjectName);
			}
		}

		// cityObjectId
		propertyImporter.doImport(externalReferenceProperty, cityObjectId);
	}

	@Override
	public void executeBatch() throws CityGMLImportException, SQLException {
		// nothing to do...
	}

	@Override
	public void close() throws CityGMLImportException, SQLException {
		// nothing to do...
	}

}

/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2017
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
package org.citydb.citygml.importer.database.content;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.citydb.citygml.common.database.xlink.DBXlinkBasic;
import org.citydb.citygml.importer.CityGMLImportException;
import org.citydb.config.Config;
import org.citydb.database.schema.TableEnum;
import org.citydb.database.schema.mapping.FeatureType;
import org.citygml4j.model.citygml.relief.AbstractReliefComponent;
import org.citygml4j.model.citygml.relief.RasterRelief;
import org.citygml4j.model.citygml.relief.ReliefComponentProperty;
import org.citygml4j.model.citygml.relief.ReliefFeature;

public class DBReliefFeature implements DBImporter {
	private final CityGMLImportManager importer;

	private PreparedStatement psReliefFeature;
	private DBCityObject cityObjectImporter;
	private DBReliefComponent reliefComponentImporter;

	private boolean hasObjectClassIdColumn;
	private int batchCounter;

	public DBReliefFeature(Connection batchConn, Config config, CityGMLImportManager importer) throws CityGMLImportException, SQLException {
		this.importer = importer;

		String schema = importer.getDatabaseAdapter().getConnectionDetails().getSchema();
		hasObjectClassIdColumn = importer.getDatabaseAdapter().getConnectionMetaData().getCityDBVersion().compareTo(4, 0, 0) >= 0;

		String stmt = "insert into " + schema + ".relief_feature (id, lod" +
				(hasObjectClassIdColumn ? ", objectclass_id) " : ") ") +
				"values (?, ?" +
				(hasObjectClassIdColumn ? ", ?)" : ")");
		psReliefFeature = batchConn.prepareStatement(stmt);

		cityObjectImporter = importer.getImporter(DBCityObject.class);
		reliefComponentImporter = importer.getImporter(DBReliefComponent.class);
	}

	protected long doImport(ReliefFeature reliefFeature) throws CityGMLImportException, SQLException {
		FeatureType featureType = importer.getFeatureType(reliefFeature);
		if (featureType == null)
			throw new SQLException("Failed to retrieve feature type.");

		// import city object information
		long reliefFeatureId = cityObjectImporter.doImport(reliefFeature, featureType);

		// import relief feature information
		// primary id
		psReliefFeature.setLong(1, reliefFeatureId);

		// dem:lod
		psReliefFeature.setInt(2, reliefFeature.getLod());

		// objectclass id
		if (hasObjectClassIdColumn)
			psReliefFeature.setLong(3, featureType.getObjectClassId());

		psReliefFeature.addBatch();
		if (++batchCounter == importer.getDatabaseAdapter().getMaxBatchSize())
			importer.executeBatch(TableEnum.RELIEF_FEATURE);

		// dem:eliefComponent
		if (reliefFeature.isSetReliefComponent()) {
			for (ReliefComponentProperty property : reliefFeature.getReliefComponent()) {
				AbstractReliefComponent component = property.getReliefComponent();

				if (component != null) {
					if (component instanceof RasterRelief)
						importer.logOrThrowErrorMessage(importer.getObjectSignature(reliefFeature) +
								": Raster relief components are not supported.");
					else
						reliefComponentImporter.doImport(component, reliefFeature, reliefFeatureId);

					property.unsetReliefComponent();
				} else {
					String href = property.getHref();
					if (href != null && href.length() != 0) {
						importer.propagateXlink(new DBXlinkBasic(
								TableEnum.RELIEF_FEAT_TO_REL_COMP.getName(),
								reliefFeatureId,
								"RELIEF_FEATURE_ID",
								href,
								"RELIEF_COMPONENT_ID"));
					}
				}
			}
		}
		
		// ADE-specific extensions
		if (importer.hasADESupport())
			importer.delegateToADEImporter(reliefFeature, reliefFeatureId, featureType);

		return reliefFeatureId;
	}

	@Override
	public void executeBatch() throws CityGMLImportException, SQLException {
		if (batchCounter > 0) {
			psReliefFeature.executeBatch();
			batchCounter = 0;
		}
	}

	@Override
	public void close() throws CityGMLImportException, SQLException {
		psReliefFeature.close();
	}

}

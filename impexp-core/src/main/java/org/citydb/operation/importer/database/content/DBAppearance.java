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
package org.citydb.operation.importer.database.content;

import org.citydb.operation.common.xlink.DBXlinkBasic;
import org.citydb.operation.importer.CityGMLImportException;
import org.citydb.operation.importer.util.AttributeValueJoiner;
import org.citydb.operation.importer.util.LocalAppearanceHandler;
import org.citydb.config.Config;
import org.citydb.database.schema.SequenceEnum;
import org.citydb.database.schema.TableEnum;
import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.util.CoreConstants;
import org.citygml4j.model.citygml.appearance.AbstractSurfaceData;
import org.citygml4j.model.citygml.appearance.Appearance;
import org.citygml4j.model.citygml.appearance.SurfaceDataProperty;
import org.citygml4j.model.citygml.texturedsurface._AbstractAppearance;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.geometry.primitives.AbstractSurface;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map.Entry;

public class DBAppearance implements DBImporter {
	private final CityGMLImportManager importer;

	private PreparedStatement psAppearance;
	private DBSurfaceData surfaceDataImporter;
	private TexturedSurfaceConverter texturedSurfaceConverter;
	private AttributeValueJoiner valueJoiner;

	private int batchCounter;
	private boolean replaceGmlId;

	public DBAppearance(Connection batchConn, Config config, CityGMLImportManager importer) throws CityGMLImportException, SQLException {
		this.importer = importer;

		replaceGmlId = config.getImportConfig().getResourceId().isUUIDModeReplace();
		String schema = importer.getDatabaseAdapter().getConnectionDetails().getSchema();

		String gmlIdCodespace = importer.getInternalConfig().getCurrentGmlIdCodespace();
		if (gmlIdCodespace != null)
			gmlIdCodespace = "'" + gmlIdCodespace + "', ";

		String stmt = "insert into " + schema + ".appearance (id, gmlid, " + (gmlIdCodespace != null ? "gmlid_codespace, " : "") +
				"name, name_codespace, description, theme, citymodel_id, cityobject_id) values " +
				"(?, ?, " + (gmlIdCodespace != null ? gmlIdCodespace : "") + "?, ?, ?, ?, ?, ?)";
		psAppearance = batchConn.prepareStatement(stmt);

		surfaceDataImporter = importer.getImporter(DBSurfaceData.class);
		texturedSurfaceConverter = new TexturedSurfaceConverter(this, config, importer);
		valueJoiner = importer.getAttributeValueJoiner();
	}

	public long doImport(Appearance appearance, long parentId, boolean isLocalAppearance) throws CityGMLImportException, SQLException {
		long appearanceId = importer.getNextSequenceValue(SequenceEnum.APPEARANCE_ID_SEQ.getName());

		FeatureType featureType = importer.getFeatureType(appearance);
		if (featureType == null)
			throw new SQLException("Failed to retrieve feature type.");

		// primary id
		psAppearance.setLong(1, appearanceId);

		// gml:id
		String origGmlId = appearance.getId();
		if (origGmlId != null)
			appearance.setLocalProperty(CoreConstants.OBJECT_ORIGINAL_GMLID, origGmlId);
		
		if (replaceGmlId) {
			String gmlId = importer.generateNewGmlId();

			// mapping entry
			if (appearance.isSetId())
				importer.putObjectId(appearance.getId(), appearanceId, gmlId, featureType.getObjectClassId());

			appearance.setId(gmlId);

		} else {
			if (appearance.isSetId())
				importer.putObjectId(appearance.getId(), appearanceId, featureType.getObjectClassId());
			else
				appearance.setId(importer.generateNewGmlId());
		}

		psAppearance.setString(2, appearance.getId());

		// gml:name
		if (appearance.isSetName()) {
			valueJoiner.join(appearance.getName(), Code::getValue, Code::getCodeSpace);
			psAppearance.setString(3, valueJoiner.result(0));
			psAppearance.setString(4, valueJoiner.result(1));
		} else {
			psAppearance.setNull(3, Types.VARCHAR);
			psAppearance.setNull(4, Types.VARCHAR);
		}

		// gml:description
		if (appearance.isSetDescription()) {
			String description = appearance.getDescription().getValue();
			if (description != null)
				description = description.trim();

			psAppearance.setString(5, description);
		} else {
			psAppearance.setNull(5, Types.VARCHAR);
		}

		// app:theme
		psAppearance.setString(6, appearance.getTheme());

		// cityobject or citymodel id
		if (isLocalAppearance) {
			psAppearance.setNull(7, Types.NULL);
			psAppearance.setLong(8, parentId);
		} else {
			psAppearance.setNull(7, Types.NULL);
			psAppearance.setNull(8, Types.NULL);
		}

		psAppearance.addBatch();
		if (++batchCounter == importer.getDatabaseAdapter().getMaxBatchSize())
			importer.executeBatch(TableEnum.APPEARANCE);

		// surfaceData members
		if (appearance.isSetSurfaceDataMember()) {
			for (SurfaceDataProperty property : appearance.getSurfaceDataMember()) {
				AbstractSurfaceData surfaceData = property.getSurfaceData();

				if (surfaceData != null) {
					surfaceDataImporter.doImport(surfaceData, appearanceId, isLocalAppearance);
					property.unsetSurfaceData();
				} else {
					String href = property.getHref();
					if (href != null && href.length() != 0) {
						importer.propagateXlink(new DBXlinkBasic(
								TableEnum.APPEAR_TO_SURFACE_DATA.getName(),
								appearanceId,
								"APPEARANCE_ID",
								href,
								"SURFACE_DATA_ID"));
					}
				}
			}
		}
		
		// ADE-specific extensions
		if (importer.hasADESupport())
			importer.delegateToADEImporter(appearance, appearanceId, featureType);

		importer.updateObjectCounter(appearance, featureType, appearanceId);
		return appearanceId;
	}

	protected void importLocalAppearance() throws CityGMLImportException, SQLException {
		LocalAppearanceHandler handler = importer.getLocalAppearanceHandler();

		if (handler != null) {
			if (handler.hasAppearances()) {
				for (Entry<Long, List<Appearance>> entry : handler.getAppearances().entrySet()) {
					for (Appearance appearance : entry.getValue())
						doImport(appearance, entry.getKey(), true);
				}
			}

			// reset appearance handler
			handler.reset();
		}
	}

	protected void importTexturedSurface(_AbstractAppearance _appearance, AbstractSurface abstractSurface, long parentId, boolean isFront, String target) throws CityGMLImportException, SQLException {
		texturedSurfaceConverter.convertTexturedSurface(_appearance, abstractSurface, parentId, isFront, target);	
	}

	protected void importTexturedSurfaceXlink(String href, long surfaceGeometryId, long parentId) throws CityGMLImportException, SQLException {
		texturedSurfaceConverter.convertTexturedSurfaceXlink(href, surfaceGeometryId, parentId);
	}

	@Override
	public void executeBatch() throws CityGMLImportException, SQLException {
		texturedSurfaceConverter.flush();

		if (batchCounter > 0) {
			psAppearance.executeBatch();
			batchCounter = 0;
		}
	}

	@Override
	public void close() throws CityGMLImportException, SQLException {
		psAppearance.close();
	}

}

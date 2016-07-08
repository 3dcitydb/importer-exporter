/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2016
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
package org.citydb.modules.citygml.importer.database.content;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import org.citydb.api.geometry.GeometryObject;
import org.citydb.config.Config;
import org.citydb.config.project.importer.CreationDateMode;
import org.citydb.config.project.importer.TerminationDateMode;
import org.citydb.database.DatabaseConnectionPool;
import org.citydb.database.TableEnum;
import org.citydb.log.Logger;
import org.citydb.modules.citygml.common.database.xlink.DBXlinkBasic;
import org.citydb.modules.citygml.importer.util.LocalGeometryXlinkResolver;
import org.citydb.util.Util;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.appearance.AppearanceProperty;
import org.citygml4j.model.citygml.appearance.ParameterizedTexture;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.citygml.core.ExternalObject;
import org.citygml4j.model.citygml.core.ExternalReference;
import org.citygml4j.model.citygml.core.GeneralizationRelation;
import org.citygml4j.model.citygml.generics.AbstractGenericAttribute;
import org.citygml4j.model.gml.geometry.primitives.Envelope;
import org.citygml4j.util.gmlid.DefaultGMLIdManager;
import org.citygml4j.util.walker.FeatureFunctionWalker;

public class DBCityObject implements DBImporter {
	private final Logger LOG = Logger.getInstance();

	private final Connection batchConn;
	private final DBImporterManager dbImporterManager;

	private PreparedStatement psCityObject;
	private DBCityObjectGenericAttrib genericAttributeImporter;
	private DBExternalReference externalReferenceImporter;
	private DBAppearance appearanceImporter;
	private LocalGeometryXlinkResolver resolver;

	private String gmlIdCodespace;
	private String updatingPerson;
	private String reasonForUpdate;
	private String lineage;
	private String importFileName;
	private int dbSrid;
	private boolean replaceGmlId;
	private boolean rememberGmlId;
	private boolean importAppearance;
	private boolean affineTransformation;
	private CreationDateMode creationDateMode;
	private TerminationDateMode terminationDateMode;
	private int batchCounter;

	public DBCityObject(Connection batchConn, Config config, DBImporterManager dbImporterManager) throws SQLException {
		this.batchConn = batchConn;
		this.dbImporterManager = dbImporterManager;

		gmlIdCodespace = config.getInternal().getCurrentGmlIdCodespace();
		replaceGmlId = config.getProject().getImporter().getGmlId().isUUIDModeReplace();
		rememberGmlId = config.getProject().getImporter().getGmlId().isSetKeepGmlIdAsExternalReference();
		affineTransformation = config.getProject().getImporter().getAffineTransformation().isSetUseAffineTransformation();
		dbSrid = DatabaseConnectionPool.getInstance().getActiveDatabaseAdapter().getConnectionMetaData().getReferenceSystem().getSrid();
		importAppearance = config.getProject().getImporter().getAppearances().isSetImportAppearance();
		reasonForUpdate = config.getProject().getImporter().getContinuation().getReasonForUpdate();
		lineage = config.getProject().getImporter().getContinuation().getLineage();
		creationDateMode = config.getProject().getImporter().getContinuation().getCreationDateMode();
		terminationDateMode = config.getProject().getImporter().getContinuation().getTerminationDateMode();

		if (gmlIdCodespace != null && gmlIdCodespace.length() > 0)
			gmlIdCodespace = "'" + gmlIdCodespace + "', ";
		else
			gmlIdCodespace = null;
		
		if (replaceGmlId && rememberGmlId)
			importFileName = config.getInternal().getCurrentImportFile().getAbsolutePath();

		if (config.getProject().getImporter().getContinuation().isUpdatingPersonModeDatabase())
			updatingPerson = config.getProject().getDatabase().getActiveConnection().getUser();
		else
			updatingPerson = config.getProject().getImporter().getContinuation().getUpdatingPerson();

		if (reasonForUpdate != null && reasonForUpdate.length() > 0)
			reasonForUpdate = "'" + reasonForUpdate + "'";
		else
			reasonForUpdate = null;

		if (lineage != null && lineage.length() > 0)
			lineage = "'" + lineage + "'";
		else
			lineage = null;

		if (updatingPerson != null && updatingPerson.length() > 0)
			updatingPerson = "'" + updatingPerson + "'";
		else
			updatingPerson = null;

		init();
	}

	private void init() throws SQLException {
		StringBuilder stmt = new StringBuilder()
		.append("insert into CITYOBJECT (ID, OBJECTCLASS_ID, GMLID, ").append(gmlIdCodespace != null ? "GMLID_CODESPACE, " : "").append("NAME, NAME_CODESPACE, DESCRIPTION, ENVELOPE, CREATION_DATE, TERMINATION_DATE, ")
		.append("RELATIVE_TO_TERRAIN, RELATIVE_TO_WATER, LAST_MODIFICATION_DATE, UPDATING_PERSON, REASON_FOR_UPDATE, LINEAGE, XML_SOURCE) values ")
		.append("(?, ?, ?, ").append(gmlIdCodespace != null ? gmlIdCodespace : "").append("?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ")
		.append(updatingPerson).append(", ")
		.append(reasonForUpdate).append(", ")
		.append(lineage).append(", null)");
		psCityObject = batchConn.prepareStatement(stmt.toString());

		genericAttributeImporter = (DBCityObjectGenericAttrib)dbImporterManager.getDBImporter(DBImporterEnum.CITYOBJECT_GENERICATTRIB);
		externalReferenceImporter = (DBExternalReference)dbImporterManager.getDBImporter(DBImporterEnum.EXTERNAL_REFERENCE);
		appearanceImporter = (DBAppearance)dbImporterManager.getDBImporter(DBImporterEnum.APPEARANCE);
		resolver = new LocalGeometryXlinkResolver();
	}

	public long insert(AbstractCityObject cityObject, long cityObjectId) throws SQLException {
		return insert(cityObject, cityObjectId, false);
	}

	public long insert(AbstractCityObject cityObject, long cityObjectId, boolean isTopLevelFeature) throws SQLException {
		// ID
		psCityObject.setLong(1, cityObjectId);

		// OBJECTCLASS_ID
		psCityObject.setInt(2, Util.cityObject2classId(cityObject.getCityGMLClass()));

		// gml:id
		String origGmlId = cityObject.getId();
		if (replaceGmlId) {
			String gmlId = DefaultGMLIdManager.getInstance().generateUUID();

			// mapping entry
			if (cityObject.isSetId()) {
				dbImporterManager.putUID(cityObject.getId(), cityObjectId, -1, false, gmlId, cityObject.getCityGMLClass());

				if (rememberGmlId) {	
					ExternalReference externalReference = new ExternalReference();
					externalReference.setInformationSystem(importFileName);

					ExternalObject externalObject = new ExternalObject();
					externalObject.setName(cityObject.getId());

					externalReference.setExternalObject(externalObject);
					cityObject.addExternalReference(externalReference);
				}
			}

			cityObject.setId(gmlId);

		} else {
			if (cityObject.isSetId())
				dbImporterManager.putUID(cityObject.getId(), cityObjectId, cityObject.getCityGMLClass());
			else
				cityObject.setId(DefaultGMLIdManager.getInstance().generateUUID());
		}

		psCityObject.setString(3, cityObject.getId());

		// gml:name
		if (cityObject.isSetName()) {
			String[] dbGmlName = Util.codeList2string(cityObject.getName());
			psCityObject.setString(4, dbGmlName[0]);
			psCityObject.setString(5, dbGmlName[1]);
		} else {
			psCityObject.setNull(4, Types.VARCHAR);
			psCityObject.setNull(5, Types.VARCHAR);
		}

		// gml:description
		if (cityObject.isSetDescription()) {
			String description = cityObject.getDescription().getValue();
			if (description != null)
				description = description.trim();

			psCityObject.setString(6, description);
		} else {
			psCityObject.setNull(6, Types.VARCHAR);
		}

		// gml:boundedBy
		if (!cityObject.isSetBoundedBy() || !cityObject.getBoundedBy().isSetEnvelope())
			cityObject.calcBoundedBy(true);
		else if (!cityObject.getBoundedBy().getEnvelope().isSetLowerCorner() ||
				!cityObject.getBoundedBy().getEnvelope().isSetUpperCorner()) {
			Envelope envelope = cityObject.getBoundedBy().getEnvelope().convert3d();
			if (envelope != null) {
				cityObject.getBoundedBy().setEnvelope(envelope);
			} else {
				cityObject.unsetBoundedBy();
				cityObject.calcBoundedBy(true);
			}
		}

		if (cityObject.isSetBoundedBy()) {			
			List<Double> points = new ArrayList<Double>(6);
			points.addAll(cityObject.getBoundedBy().getEnvelope().getLowerCorner().getValue());
			points.addAll(cityObject.getBoundedBy().getEnvelope().getUpperCorner().getValue());

			if (affineTransformation)
				dbImporterManager.getAffineTransformer().transformCoordinates(points);

			double[] coordinates = new double[15];
			coordinates[0] = points.get(0);
			coordinates[1] = points.get(1);
			coordinates[2] = points.get(2);
			coordinates[3] = points.get(3);
			coordinates[4] = points.get(1);
			coordinates[5] = points.get(2);
			coordinates[6] = points.get(3);
			coordinates[7] = points.get(4);
			coordinates[8] = points.get(5);
			coordinates[9] = points.get(0);
			coordinates[10] = points.get(4);
			coordinates[11] = points.get(5);
			coordinates[12] = points.get(0);
			coordinates[13] = points.get(1);
			coordinates[14] = points.get(2);

			GeometryObject envelope = GeometryObject.createPolygon(coordinates, 3, dbSrid);
			psCityObject.setObject(7, dbImporterManager.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(envelope, batchConn));
		} else {
			psCityObject.setNull(7, dbImporterManager.getDatabaseAdapter().getGeometryConverter().getNullGeometryType(), 
					dbImporterManager.getDatabaseAdapter().getGeometryConverter().getNullGeometryTypeName());
		}

		// creationDate (null is not allowed)
		java.util.Date creationDate = null;
		if (creationDateMode == CreationDateMode.INHERIT || creationDateMode == CreationDateMode.COMPLEMENT) {
			GregorianCalendar gc = Util.getCreationDate(cityObject, creationDateMode == CreationDateMode.INHERIT);
			if (gc != null) {
				// get creationDate from cityObject (or parents)
				creationDate = gc.getTime();
			}
		}

		if (creationDate == null) {
			// creationDate is not set: use current date
			creationDate = new java.util.Date();
		}

		psCityObject.setTimestamp(8, new Timestamp(creationDate.getTime()));

		// terminationDate (null is allowed)
		java.util.Date terminationDate = null;
		if (terminationDateMode == TerminationDateMode.INHERIT || terminationDateMode == TerminationDateMode.COMPLEMENT) {
			GregorianCalendar gc = Util.getTerminationDate(cityObject, terminationDateMode == TerminationDateMode.INHERIT);
			if (gc != null) {
				// get terminationDate from cityObject (or parents)
				terminationDate = gc.getTime();
			}
		}

		if (terminationDate == null) {
			psCityObject.setNull(9, Types.TIMESTAMP);
		} else {
			psCityObject.setTimestamp(9, new Timestamp(terminationDate.getTime()));
		}

		// relativeToTerrain
		if (cityObject.isSetRelativeToTerrain())
			psCityObject.setString(10, cityObject.getRelativeToTerrain().getValue());
		else
			psCityObject.setNull(10, Types.VARCHAR);

		// relativeToWater
		if (cityObject.isSetRelativeToWater())
			psCityObject.setString(11, cityObject.getRelativeToWater().getValue());
		else
			psCityObject.setNull(11, Types.VARCHAR);

		// resolve local xlinks to geometry objects
		if (isTopLevelFeature) {
			boolean success = resolver.resolveGeometryXlinks(cityObject);
			if (!success) {
				StringBuilder msg = new StringBuilder(Util.getFeatureSignature(
						cityObject.getCityGMLClass(), 
						origGmlId));
				msg.append(": Skipped due to circular reference of geometry XLinks.");
				LOG.error(msg.toString());
				LOG.error("The targets causing the circular reference are:");
				for (String target : resolver.getCircularReferences())
					LOG.print(target);

				return 0;
			}
		}

		psCityObject.addBatch();
		if (++batchCounter == dbImporterManager.getDatabaseAdapter().getMaxBatchSize())
			dbImporterManager.executeBatch(DBImporterEnum.CITYOBJECT);

		// genericAttributes
		if (cityObject.isSetGenericAttribute())
			for (AbstractGenericAttribute genericAttribute : cityObject.getGenericAttribute())
				genericAttributeImporter.insert(genericAttribute, cityObjectId);

		// externalReference
		if (cityObject.isSetExternalReference())
			for (ExternalReference externalReference : cityObject.getExternalReference())
				externalReferenceImporter.insert(externalReference, cityObjectId);

		// generalizesTo
		if (cityObject.isSetGeneralizesTo()) {
			for (GeneralizationRelation generalizesTo : cityObject.getGeneralizesTo()) {
				if (generalizesTo.isSetCityObject()) {
					StringBuilder msg = new StringBuilder(Util.getFeatureSignature(
							cityObject.getCityGMLClass(), 
							origGmlId));

					msg.append(": XML read error while parsing generalizesTo element.");
					LOG.error(msg.toString());
				} else {
					// xlink
					String href = generalizesTo.getHref();

					if (href != null && href.length() != 0) {
						dbImporterManager.propagateXlink(new DBXlinkBasic(
								cityObjectId,
								TableEnum.CITYOBJECT,
								href,
								TableEnum.CITYOBJECT
								));
					}
				}
			}
		}		

		// reset local texture coordinates resolver
		if (importAppearance && isTopLevelFeature) {
			// reset local texture coordinates resolver
			dbImporterManager.getLocalTextureCoordinatesResolver().reset();

			// check whether we have at least one local appearance
			FeatureFunctionWalker<Boolean> walker = new FeatureFunctionWalker<Boolean>() {
				public Boolean apply(ParameterizedTexture parameterizedTexture) {
					return true;
				}
			};

			dbImporterManager.getLocalTextureCoordinatesResolver().setActive(cityObject.accept(walker) != null);
		}

		dbImporterManager.updateFeatureCounter(cityObject.getCityGMLClass(), cityObjectId, origGmlId, isTopLevelFeature);
		return cityObjectId;
	}

	public void insertAppearance(AbstractCityObject cityObject, long cityObjectId) throws SQLException {
		if (importAppearance && cityObject.isSetAppearance()) {
			for (AppearanceProperty appearanceProperty : cityObject.getAppearance()) {
				if (appearanceProperty.isSetAppearance()) {
					String gmlId = appearanceProperty.getAppearance().getId();
					long id = appearanceImporter.insert(appearanceProperty.getAppearance(), CityGMLClass.ABSTRACT_CITY_OBJECT, cityObjectId);

					if (id == 0) {
						StringBuilder msg = new StringBuilder(Util.getFeatureSignature(
								cityObject.getCityGMLClass(), 
								cityObject.getId()));
						msg.append(": Failed to write ");
						msg.append(Util.getFeatureSignature(
								CityGMLClass.APPEARANCE, 
								gmlId));

						LOG.error(msg.toString());
					}

					// free memory of nested feature
					appearanceProperty.unsetAppearance();
				} else {
					// xlink
					String href = appearanceProperty.getHref();

					if (href != null && href.length() != 0) {
						LOG.error("XLink reference '" + href + "' to " + CityGMLClass.APPEARANCE + " feature is not supported.");
					}
				}
			}
		}
	}

	@Override
	public void executeBatch() throws SQLException {
		psCityObject.executeBatch();
		batchCounter = 0;
	}

	@Override
	public void close() throws SQLException {
		psCityObject.close();
	}

	@Override
	public DBImporterEnum getDBImporterType() {
		return DBImporterEnum.CITYOBJECT;
	}
}

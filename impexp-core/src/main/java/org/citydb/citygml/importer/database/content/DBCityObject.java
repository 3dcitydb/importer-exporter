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

import org.citydb.citygml.common.database.xlink.DBXlinkBasic;
import org.citydb.citygml.importer.CityGMLImportException;
import org.citydb.citygml.importer.util.AttributeValueJoiner;
import org.citydb.citygml.importer.util.LocalGeometryXlinkResolver;
import org.citydb.citygml.importer.util.LocalAppearanceHandler;
import org.citydb.config.Config;
import org.citydb.config.geometry.GeometryObject;
import org.citydb.config.project.importer.CreationDateMode;
import org.citydb.config.project.importer.TerminationDateMode;
import org.citydb.database.connection.DatabaseConnectionPool;
import org.citydb.database.schema.SequenceEnum;
import org.citydb.database.schema.TableEnum;
import org.citydb.database.schema.mapping.AbstractObjectType;
import org.citydb.util.CoreConstants;
import org.citydb.util.Util;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.citygml.core.ExternalObject;
import org.citygml4j.model.citygml.core.ExternalReference;
import org.citygml4j.model.citygml.core.GeneralizationRelation;
import org.citygml4j.model.citygml.generics.AbstractGenericAttribute;
import org.citygml4j.model.gml.base.AbstractGML;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.feature.AbstractFeature;
import org.citygml4j.model.gml.feature.BoundingShape;
import org.citygml4j.util.bbox.BoundingBoxOptions;
import org.citygml4j.util.gmlid.DefaultGMLIdManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class DBCityObject implements DBImporter {
	private final Connection batchConn;
	private final CityGMLImportManager importer;

	private PreparedStatement psCityObject;
	private DBCityObjectGenericAttrib genericAttributeImporter;
	private DBExternalReference externalReferenceImporter;
	private LocalGeometryXlinkResolver resolver;
	private AttributeValueJoiner valueJoiner;
	private int batchCounter;

	private String reasonForUpdate;
	private String importFileName;
	private int dbSrid;
	private boolean replaceGmlId;
	private boolean rememberGmlId;
	private boolean importAppearance;
	private boolean affineTransformation;
	private CreationDateMode creationDateMode;
	private TerminationDateMode terminationDateMode;
	private BoundingBoxOptions bboxOptions;

	public DBCityObject(Connection batchConn, Config config, CityGMLImportManager importer) throws CityGMLImportException, SQLException {
		this.batchConn = batchConn;	
		this.importer = importer;

		String gmlIdCodespace = config.getInternal().getCurrentGmlIdCodespace();
		replaceGmlId = config.getProject().getImporter().getGmlId().isUUIDModeReplace();
		rememberGmlId = config.getProject().getImporter().getGmlId().isSetKeepGmlIdAsExternalReference();
		affineTransformation = config.getProject().getImporter().getAffineTransformation().isSetUseAffineTransformation();
		dbSrid = DatabaseConnectionPool.getInstance().getActiveDatabaseAdapter().getConnectionMetaData().getReferenceSystem().getSrid();
		importAppearance = config.getProject().getImporter().getAppearances().isSetImportAppearance();
		reasonForUpdate = config.getProject().getImporter().getContinuation().getReasonForUpdate();
		String lineage = config.getProject().getImporter().getContinuation().getLineage();
		creationDateMode = config.getProject().getImporter().getContinuation().getCreationDateMode();
		terminationDateMode = config.getProject().getImporter().getContinuation().getTerminationDateMode();

		if (gmlIdCodespace != null && gmlIdCodespace.length() > 0)
			gmlIdCodespace = "'" + gmlIdCodespace + "', ";
		else
			gmlIdCodespace = null;

		if (replaceGmlId && rememberGmlId)
			importFileName = config.getInternal().getCurrentImportFile().getAbsolutePath();

		String updatingPerson = null;
		if (config.getProject().getImporter().getContinuation().isUpdatingPersonModeDatabase())
			updatingPerson = importer.getDatabaseAdapter().getConnectionDetails().getUser();
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

		String schema = importer.getDatabaseAdapter().getConnectionDetails().getSchema();
		bboxOptions = BoundingBoxOptions.defaults()
				.useExistingEnvelopes(true)
				.assignResultToFeatures(true)
				.useReferencePointAsFallbackForImplicitGeometries(true);

		StringBuilder stmt = new StringBuilder()
				.append("insert into ").append(schema).append(".cityobject (id, objectclass_id, gmlid, ").append(gmlIdCodespace != null ? "gmlid_codespace, " : "").append("name, name_codespace, description, envelope, creation_date, termination_date, ")
				.append("relative_to_terrain, relative_to_water, last_modification_date, updating_person, reason_for_update, lineage, xml_source) values ")
				.append("(?, ?, ?, ").append(gmlIdCodespace != null ? gmlIdCodespace : "").append("?, ?, ?, ?, ?, ?, ?, ?, current_timestamp, ")
				.append(updatingPerson).append(", ")
				.append(reasonForUpdate).append(", ")
				.append(lineage).append(", null)");
		psCityObject = batchConn.prepareStatement(stmt.toString());

		genericAttributeImporter = importer.getImporter(DBCityObjectGenericAttrib.class);
		externalReferenceImporter = importer.getImporter(DBExternalReference.class);
		resolver = new LocalGeometryXlinkResolver(importer);
		valueJoiner = importer.getAttributeValueJoiner();
	}

	protected long doImport(AbstractGML object) throws CityGMLImportException, SQLException {
		AbstractObjectType<?> objectType = importer.getAbstractObjectType(object);
		if (objectType == null)
			throw new SQLException("Failed to retrieve object type.");

		long objectId = doImport(object, objectType);

		// ADE-specific extensions
		if (importer.hasADESupport())
			importer.delegateToADEImporter(object, objectId, objectType);
		
		return objectId;
	}

	protected long doImport(AbstractGML object, AbstractObjectType<?> objectType) throws CityGMLImportException, SQLException {
		boolean isFeature = object instanceof AbstractFeature;
		boolean isCityObject = object instanceof AbstractCityObject;
		boolean isGlobal = !object.isSetParent();

		// primary id
		long objectId = importer.getNextSequenceValue(SequenceEnum.CITYOBJECT_ID_SEQ.getName());
		psCityObject.setLong(1, objectId);

		// object class id
		psCityObject.setInt(2, objectType.getObjectClassId());

		// gml:id
		String origGmlId = object.getId();
		if (origGmlId != null)
			object.setLocalProperty(CoreConstants.OBJECT_ORIGINAL_GMLID, origGmlId);

		if (replaceGmlId) {
			String gmlId = DefaultGMLIdManager.getInstance().generateUUID();

			// mapping entry
			if (object.isSetId()) {
				importer.putObjectUID(object.getId(), objectId, gmlId, objectType.getObjectClassId());

				if (rememberGmlId && isCityObject) {	
					ExternalReference externalReference = new ExternalReference();
					externalReference.setInformationSystem(importFileName);

					ExternalObject externalObject = new ExternalObject();
					externalObject.setName(object.getId());

					externalReference.setExternalObject(externalObject);
					((AbstractCityObject)object).addExternalReference(externalReference);
				}
			}

			object.setId(gmlId);
		} else {
			if (object.isSetId())
				importer.putObjectUID(object.getId(), objectId, objectType.getObjectClassId());
			else
				object.setId(DefaultGMLIdManager.getInstance().generateUUID());
		}

		psCityObject.setString(3, object.getId());

		// gml:name
		if (object.isSetName()) {
			valueJoiner.join(object.getName(), Code::getValue, Code::getCodeSpace);
			psCityObject.setString(4, valueJoiner.result(0));
			psCityObject.setString(5, valueJoiner.result(1));
		} else {
			psCityObject.setNull(4, Types.VARCHAR);
			psCityObject.setNull(5, Types.VARCHAR);
		}

		// gml:description
		if (object.isSetDescription()) {
			String description = object.getDescription().getValue();
			if (description != null)
				description = description.trim();

			psCityObject.setString(6, description);
		} else {
			psCityObject.setNull(6, Types.VARCHAR);
		}

		// gml:boundedBy
		BoundingShape boundedBy = null;
		if (isFeature)
			boundedBy = ((AbstractFeature)object).calcBoundedBy(bboxOptions);

		if (boundedBy != null && boundedBy.isSetEnvelope()) {			
			List<Double> points = new ArrayList<Double>(6);
			points.addAll(boundedBy.getEnvelope().getLowerCorner().getValue());
			points.addAll(boundedBy.getEnvelope().getUpperCorner().getValue());

			if (affineTransformation)
				importer.getAffineTransformer().transformCoordinates(points);

			double[] coordinates = new double[]{
					points.get(0), points.get(1), points.get(2),
					points.get(3), points.get(1), points.get(2),
					points.get(3), points.get(4), points.get(5),
					points.get(0), points.get(4), points.get(5),
					points.get(0), points.get(1), points.get(2)
			};

			GeometryObject envelope = GeometryObject.createPolygon(coordinates, 3, dbSrid);
			psCityObject.setObject(7, importer.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(envelope, batchConn));
		} else {
			psCityObject.setNull(7, importer.getDatabaseAdapter().getGeometryConverter().getNullGeometryType(), 
					importer.getDatabaseAdapter().getGeometryConverter().getNullGeometryTypeName());
		}

		// core:creationDate 
		Date creationDate = null;
		if (isCityObject && (creationDateMode == CreationDateMode.INHERIT || creationDateMode == CreationDateMode.COMPLEMENT)) {
			GregorianCalendar gc = Util.getCreationDate((AbstractCityObject)object, creationDateMode == CreationDateMode.INHERIT);
			if (gc != null)
				creationDate = gc.getTime();
		}

		if (creationDate == null)
			creationDate = new Date();

		psCityObject.setTimestamp(8, new Timestamp(creationDate.getTime()));

		// core:terminationDate
		Date terminationDate = null;
		if (isCityObject && (terminationDateMode == TerminationDateMode.INHERIT || terminationDateMode == TerminationDateMode.COMPLEMENT)) {
			GregorianCalendar gc = Util.getTerminationDate((AbstractCityObject)object, terminationDateMode == TerminationDateMode.INHERIT);
			if (gc != null)
				terminationDate = gc.getTime();
		}

		if (terminationDate == null) {
			psCityObject.setNull(9, Types.TIMESTAMP);
		} else {
			psCityObject.setTimestamp(9, new Timestamp(terminationDate.getTime()));
		}

		// core:relativeToTerrain
		if (isCityObject && ((AbstractCityObject)object).isSetRelativeToTerrain())
			psCityObject.setString(10, ((AbstractCityObject)object).getRelativeToTerrain().getValue());
		else
			psCityObject.setNull(10, Types.VARCHAR);

		// core:relativeToWater
		if (isCityObject && ((AbstractCityObject)object).isSetRelativeToWater())
			psCityObject.setString(11, ((AbstractCityObject)object).getRelativeToWater().getValue());
		else
			psCityObject.setNull(11, Types.VARCHAR);

		// resolve local xlinks to geometry objects
		if (isGlobal) {
			boolean success = resolver.resolveGeometryXlinks(object);
			if (!success) {
				importer.logOrThrowErrorMessage(new StringBuilder(importer.getObjectSignature(object, origGmlId))
						.append(": Skipping import due to circular reference of the following geometry XLinks:\n")
						.append(String.join("\n", resolver.getCircularReferences())).toString());
				return 0;
			}
		}

		psCityObject.addBatch();
		if (++batchCounter == importer.getDatabaseAdapter().getMaxBatchSize())
			importer.executeBatch(TableEnum.CITYOBJECT);

		// work on city object related information
		if (isCityObject) {
			AbstractCityObject cityObject = (AbstractCityObject)object;

			// core:_genericAttribute
			if (cityObject.isSetGenericAttribute()) {
				for (AbstractGenericAttribute genericAttribute : cityObject.getGenericAttribute())
					genericAttributeImporter.doImport(genericAttribute, objectId);
			}

			// core:externalReferences
			if (cityObject.isSetExternalReference()) {
				for (ExternalReference externalReference : cityObject.getExternalReference())
					externalReferenceImporter.doImport(externalReference, objectId);
			}

			// core:generalizesTo
			if (cityObject.isSetGeneralizesTo()) {
				for (GeneralizationRelation generalizesTo : cityObject.getGeneralizesTo()) {
					if (generalizesTo.isSetCityObject()) {
						importer.logOrThrowErrorMessage(new StringBuilder(importer.getObjectSignature(object))
								.append(": Failed to correctly process generalizesTo element.").toString());
					} else {
						String href = generalizesTo.getHref();
						if (href != null && href.length() != 0) {
							importer.propagateXlink(new DBXlinkBasic(
									TableEnum.GENERALIZATION.getName(),
									objectId,
									"CITYOBJECT_ID",
									href,
									"GENERALIZES_TO_ID"));
						}
					}
				}
			}		

			// handle local appearances
			if (importAppearance) {
				LocalAppearanceHandler handler = importer.getLocalAppearanceHandler();

				// reset handler for top-level features
				if (isGlobal)
					handler.reset();

				if (cityObject.isSetAppearance())
					handler.registerAppearances(cityObject, objectId);
			}
		}

		importer.updateObjectCounter(object, objectType, objectId);
		return objectId;
	}

	@Override
	public void executeBatch() throws CityGMLImportException, SQLException {
		if (batchCounter > 0) {
			psCityObject.executeBatch();
			batchCounter = 0;
		}
	}

	@Override
	public void close() throws CityGMLImportException, SQLException {
		psCityObject.close();
	}

}

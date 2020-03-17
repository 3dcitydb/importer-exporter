/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2019
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

import org.citydb.ade.model.LineageProperty;
import org.citydb.ade.model.ReasonForUpdateProperty;
import org.citydb.ade.model.UpdatingPersonProperty;
import org.citydb.citygml.common.database.xlink.DBXlinkBasic;
import org.citydb.citygml.importer.CityGMLImportException;
import org.citydb.citygml.importer.util.AttributeValueJoiner;
import org.citydb.citygml.importer.util.LocalAppearanceHandler;
import org.citydb.citygml.importer.util.LocalGeometryXlinkResolver;
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
import org.citygml4j.geometry.BoundingBox;
import org.citygml4j.model.citygml.ade.ADEComponent;
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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
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

	private String importFileName;
	private int dbSrid;
	private boolean replaceGmlId;
	private boolean rememberGmlId;
	private boolean importAppearance;
	private boolean affineTransformation;

	private boolean importCityDBMetadata;
	private String updatingPerson;
	private String reasonForUpdate;
	private String lineage;
	private CreationDateMode creationDateMode;
	private TerminationDateMode terminationDateMode;
	private BoundingBoxOptions bboxOptions;

	public DBCityObject(Connection batchConn, Config config, CityGMLImportManager importer) throws CityGMLImportException, SQLException {
		this.batchConn = batchConn;	
		this.importer = importer;

		affineTransformation = config.getProject().getImporter().getAffineTransformation().isEnabled();
		dbSrid = DatabaseConnectionPool.getInstance().getActiveDatabaseAdapter().getConnectionMetaData().getReferenceSystem().getSrid();
		importAppearance = config.getProject().getImporter().getAppearances().isSetImportAppearance();
		creationDateMode = config.getProject().getImporter().getContinuation().getCreationDateMode();
		terminationDateMode = config.getProject().getImporter().getContinuation().getTerminationDateMode();

		importCityDBMetadata = config.getProject().getImporter().getContinuation().isImportCityDBMetadata();
		reasonForUpdate = config.getProject().getImporter().getContinuation().getReasonForUpdate();
		lineage = config.getProject().getImporter().getContinuation().getLineage();
		updatingPerson = config.getProject().getImporter().getContinuation().isUpdatingPersonModeDatabase() ?
				importer.getDatabaseAdapter().getConnectionDetails().getUser() :
				config.getProject().getImporter().getContinuation().getUpdatingPerson();

		if (reasonForUpdate != null && reasonForUpdate.trim().isEmpty())
			reasonForUpdate = null;

		if (lineage != null && lineage.trim().isEmpty())
			lineage = null;

		if (updatingPerson != null && updatingPerson.trim().isEmpty())
			updatingPerson = null;

		String gmlIdCodespace = config.getInternal().getCurrentGmlIdCodespace();
		if (gmlIdCodespace != null)
			gmlIdCodespace = "'" + gmlIdCodespace + "', ";

		replaceGmlId = config.getProject().getImporter().getGmlId().isUUIDModeReplace();
		rememberGmlId = config.getProject().getImporter().getGmlId().isSetKeepGmlIdAsExternalReference();
		if (replaceGmlId && rememberGmlId && importer.getInputFile() != null)
			importFileName = importer.getInputFile().getFile().toString();

		String schema = importer.getDatabaseAdapter().getConnectionDetails().getSchema();
		bboxOptions = BoundingBoxOptions.defaults()
				.useExistingEnvelopes(true)
				.assignResultToFeatures(true)
				.useReferencePointAsFallbackForImplicitGeometries(true);

		String stmt = "insert into " + schema + ".cityobject (id, objectclass_id, gmlid, " + (gmlIdCodespace != null ? "gmlid_codespace, " : "") +
				"name, name_codespace, description, envelope, creation_date, termination_date, relative_to_terrain, relative_to_water, " +
				"last_modification_date, updating_person, reason_for_update, lineage) values " +
				"(?, ?, ?, " + (gmlIdCodespace != null ? gmlIdCodespace : "") + "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		psCityObject = batchConn.prepareStatement(stmt);

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
		ZonedDateTime now = ZonedDateTime.now();

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
			String gmlId = importer.generateNewGmlId();

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
				object.setId(importer.generateNewGmlId());
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
			BoundingBox bbox = boundedBy.getEnvelope().toBoundingBox();
			List<Double> points = bbox.toList();

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
		ZonedDateTime creationDate = null;
		if (isCityObject && (creationDateMode == CreationDateMode.INHERIT || creationDateMode == CreationDateMode.COMPLEMENT)) {
			creationDate = Util.getCreationDate((AbstractCityObject) object, creationDateMode == CreationDateMode.INHERIT);
			if (creationDate != null)
				creationDate = creationDate.toLocalDate().atTime(LocalTime.MIDNIGHT).atZone(ZoneOffset.UTC);
		}

		if (creationDate == null)
			creationDate = now;

		psCityObject.setObject(8, creationDate.toOffsetDateTime());

		// core:terminationDate
		ZonedDateTime terminationDate = null;
		if (isCityObject && (terminationDateMode == TerminationDateMode.INHERIT || terminationDateMode == TerminationDateMode.COMPLEMENT)) {
			terminationDate = Util.getTerminationDate((AbstractCityObject) object, terminationDateMode == TerminationDateMode.INHERIT);
			if (terminationDate != null)
				terminationDate = terminationDate.toLocalDate().atTime(LocalTime.MIDNIGHT).atZone(ZoneOffset.UTC);
		}

		if (terminationDate == null)
			psCityObject.setNull(9, Types.TIMESTAMP);
		else
			psCityObject.setObject(9, terminationDate.toOffsetDateTime());

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

		// 3DCityDB metadata
		String updatingPerson = this.updatingPerson;
		String reasonForUpdate = this.reasonForUpdate;
		String lineage = this.lineage;

		if (isCityObject && importCityDBMetadata) {
			for (ADEComponent adeComponent : ((AbstractCityObject) object).getGenericApplicationPropertyOfCityObject()) {
				if (adeComponent instanceof UpdatingPersonProperty)
					updatingPerson = ((UpdatingPersonProperty) adeComponent).getValue();
				else if (adeComponent instanceof ReasonForUpdateProperty)
					reasonForUpdate = ((ReasonForUpdateProperty) adeComponent).getValue();
				else if (adeComponent instanceof LineageProperty)
					lineage = ((LineageProperty) adeComponent).getValue();
			}
		}

		// citydb:lastModificationDate
		psCityObject.setObject(12, now.toOffsetDateTime());

		// citydb:updatingPerson
		psCityObject.setString(13, updatingPerson);

		// citydb:reasonForUpdate
		psCityObject.setString(14, reasonForUpdate);

		// citydb:lineage
		psCityObject.setString(15, lineage);

		// resolve local xlinks to geometry objects
		if (isGlobal) {
			boolean success = resolver.resolveGeometryXlinks(object);
			if (!success) {
				importer.logOrThrowErrorMessage(importer.getObjectSignature(object, origGmlId) +
						": Skipping import due to circular reference of the following geometry XLinks:\n" +
						String.join("\n", resolver.getCircularReferences()));
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
						importer.logOrThrowErrorMessage(importer.getObjectSignature(object) +
								": Failed to correctly process generalizesTo element.");
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

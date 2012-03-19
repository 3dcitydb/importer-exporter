/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2012
 * Institute for Geodesy and Geoinformation Science
 * Technische Universitaet Berlin, Germany
 * http://www.gis.tu-berlin.de/
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see 
 * <http://www.gnu.org/licenses/>.
 * 
 * The development of the 3D City Database Importer/Exporter has 
 * been financially supported by the following cooperation partners:
 * 
 * Business Location Center, Berlin <http://www.businesslocationcenter.de/>
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * Berlin Senate of Business, Technology and Women <http://www.berlin.de/sen/wtf/>
 */
package de.tub.citydb.modules.citygml.importer.database.content;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import oracle.spatial.geometry.JGeometry;
import oracle.spatial.geometry.SyncJGeometry;
import oracle.sql.STRUCT;

import org.citygml4j.impl.citygml.core.ExternalObjectImpl;
import org.citygml4j.impl.citygml.core.ExternalReferenceImpl;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.appearance.AppearanceProperty;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.citygml.core.ExternalObject;
import org.citygml4j.model.citygml.core.ExternalReference;
import org.citygml4j.model.citygml.core.GeneralizationRelation;
import org.citygml4j.model.citygml.generics.AbstractGenericAttribute;
import org.citygml4j.model.gml.geometry.primitives.Envelope;
import org.citygml4j.util.gmlid.DefaultGMLIdManager;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.database.DatabaseConnectionPool;
import de.tub.citydb.database.TableEnum;
import de.tub.citydb.log.Logger;
import de.tub.citydb.modules.citygml.common.database.xlink.DBXlinkBasic;
import de.tub.citydb.modules.citygml.importer.util.LocalGeometryXlinkResolver;
import de.tub.citydb.util.Util;

public class DBCityObject implements DBImporter {
	private final Logger LOG = Logger.getInstance();

	private final Connection batchConn;
	private final Config config;
	private final DBImporterManager dbImporterManager;

	private PreparedStatement psCityObject;
	private DBCityObjectGenericAttrib genericAttributeImporter;
	private DBExternalReference externalReferenceImporter;
	private DBAppearance appearanceImporter;
	private LocalGeometryXlinkResolver resolver;

	private String updatingPerson;
	private String reasonForUpdate;
	private String lineage;
	private String importFileName;
	private int dbSrid;
	private boolean replaceGmlId;
	private boolean rememberGmlId;
	private boolean importAppearance;
	private boolean affineTransformation;
	private int batchCounter;

	public DBCityObject(Connection batchConn, Config config, DBImporterManager dbImporterManager) throws SQLException {
		this.batchConn = batchConn;
		this.config = config;
		this.dbImporterManager = dbImporterManager;

		init();
	}

	private void init() throws SQLException {
		replaceGmlId = config.getProject().getImporter().getGmlId().isUUIDModeReplace();
		rememberGmlId = config.getProject().getImporter().getGmlId().isSetKeepGmlIdAsExternalReference();
		affineTransformation = config.getProject().getImporter().getAffineTransformation().isSetUseAffineTransformation();
		dbSrid = DatabaseConnectionPool.getInstance().getActiveConnectionMetaData().getReferenceSystem().getSrid();
		importAppearance = config.getProject().getImporter().getAppearances().isSetImportAppearance();
		String gmlIdCodespace = config.getInternal().getCurrentGmlIdCodespace();

		if (rememberGmlId)
			importFileName = config.getInternal().getCurrentImportFile().getAbsolutePath();

		reasonForUpdate = config.getProject().getImporter().getContinuation().getReasonForUpdate();
		lineage = config.getProject().getImporter().getContinuation().getLineage();
		if (config.getProject().getImporter().getContinuation().isUpdatingPersonModeDatabase())
			updatingPerson = config.getProject().getDatabase().getActiveConnection().getUser();
		else
			updatingPerson = config.getProject().getImporter().getContinuation().getUpdatingPerson();

		if (reasonForUpdate != null && reasonForUpdate.length() != 0)
			reasonForUpdate = "'" + reasonForUpdate + "'";
		else
			reasonForUpdate = null;

		if (lineage != null && lineage.length() != 0)
			lineage = "'" + lineage + "'";
		else
			lineage = null;

		if (updatingPerson != null && updatingPerson.length() != 0)
			updatingPerson = "'" + updatingPerson + "'";
		else
			updatingPerson = null;

		if (gmlIdCodespace != null && gmlIdCodespace.length() != 0)
			gmlIdCodespace = "'" + gmlIdCodespace + "'";
		else
			gmlIdCodespace = "null";

		psCityObject = batchConn.prepareStatement("insert into CITYOBJECT (ID, CLASS_ID, GMLID, GMLID_CODESPACE, ENVELOPE, CREATION_DATE, TERMINATION_DATE, LAST_MODIFICATION_DATE, UPDATING_PERSON, REASON_FOR_UPDATE, LINEAGE, XML_SOURCE) values " +
				"(?, ?, ?, " + gmlIdCodespace + ", ?, SYSDATE, null, SYSDATE, " + updatingPerson + ", " + reasonForUpdate + ", " + lineage + ", null)");

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

		// CLASS_ID
		int classId = Util.cityObject2classId(cityObject.getCityGMLClass());
		psCityObject.setInt(2, classId);

		// gml:id
		String origGmlId = cityObject.getId();
		if (replaceGmlId) {
			String gmlId = DefaultGMLIdManager.getInstance().generateUUID();

			// mapping entry
			if (cityObject.isSetId()) {
				dbImporterManager.putGmlId(cityObject.getId(), cityObjectId, -1, false, gmlId, cityObject.getCityGMLClass());

				if (rememberGmlId) {	
					ExternalReference externalReference = new ExternalReferenceImpl();
					externalReference.setInformationSystem(importFileName);

					ExternalObject externalObject = new ExternalObjectImpl();
					externalObject.setName(cityObject.getId());

					externalReference.setExternalObject(externalObject);
					cityObject.addExternalReference(externalReference);
				}
			}

			cityObject.setId(gmlId);

		} else {
			if (cityObject.isSetId())
				dbImporterManager.putGmlId(cityObject.getId(), cityObjectId, cityObject.getCityGMLClass());
			else
				cityObject.setId(DefaultGMLIdManager.getInstance().generateUUID());
		}

		psCityObject.setString(3, cityObject.getId());

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
			List<Double> points = new ArrayList<Double>(15);
			points.addAll(cityObject.getBoundedBy().getEnvelope().getLowerCorner().getValue());
			points.add(cityObject.getBoundedBy().getEnvelope().getUpperCorner().getValue().get(0));
			points.add(cityObject.getBoundedBy().getEnvelope().getLowerCorner().getValue().get(1));
			points.add(cityObject.getBoundedBy().getEnvelope().getLowerCorner().getValue().get(2));
			points.addAll(cityObject.getBoundedBy().getEnvelope().getUpperCorner().getValue());
			points.add(cityObject.getBoundedBy().getEnvelope().getLowerCorner().getValue().get(0));
			points.add(cityObject.getBoundedBy().getEnvelope().getUpperCorner().getValue().get(1));
			points.add(cityObject.getBoundedBy().getEnvelope().getUpperCorner().getValue().get(2));
			points.addAll(cityObject.getBoundedBy().getEnvelope().getLowerCorner().getValue());

			if (affineTransformation)
				dbImporterManager.getAffineTransformer().transformCoordinates(points);

			double[] ordinates = new double[points.size()];
			int i = 0;
			for (Double point : points)
				ordinates[i++] = point.doubleValue();

			JGeometry boundedBy = JGeometry.createLinearPolygon(ordinates, 3, dbSrid);
			STRUCT obj = SyncJGeometry.syncStore(boundedBy, batchConn);

			psCityObject.setObject(4, obj);
		} else {
			psCityObject.setNull(4, Types.STRUCT, "MDSYS.SDO_GEOMETRY");
		}

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
		if (++batchCounter == Internal.ORACLE_MAX_BATCH_SIZE)
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

		// appearances
		if (importAppearance) {
			if (cityObject.isSetAppearance()) {
				for (AppearanceProperty appearanceProperty : cityObject.getAppearance()) {
					if (appearanceProperty.isSetAppearance()) {
						String gmlId = appearanceProperty.getAppearance().getId();
						long id = appearanceImporter.insert(appearanceProperty.getAppearance(), CityGMLClass.ABSTRACT_CITY_OBJECT, cityObjectId);

						if (id == 0) {
							StringBuilder msg = new StringBuilder(Util.getFeatureSignature(
									cityObject.getCityGMLClass(), 
									origGmlId));
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
							LOG.error("XLink reference '" + href + "' to Appearance feature is not supported.");
						}
					}
				}
			}
		}

		dbImporterManager.updateFeatureCounter(cityObject.getCityGMLClass());
		return cityObjectId;
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

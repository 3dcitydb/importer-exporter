package de.tub.citydb.db.importer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import oracle.spatial.geometry.JGeometry;
import oracle.spatial.geometry.SyncJGeometry;
import oracle.sql.STRUCT;

import org.citygml4j.factory.CityGMLFactory;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.CityGMLModuleType;
import org.citygml4j.model.citygml.appearance.AppearanceProperty;
import org.citygml4j.model.citygml.core.CityObject;
import org.citygml4j.model.citygml.core.CoreModule;
import org.citygml4j.model.citygml.core.ExternalObject;
import org.citygml4j.model.citygml.core.ExternalReference;
import org.citygml4j.model.citygml.core.GeneralizationRelation;
import org.citygml4j.model.citygml.generics.GenericAttribute;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.db.DBTableEnum;
import de.tub.citydb.db.xlink.DBXlinkBasic;
import de.tub.citydb.log.Logger;
import de.tub.citydb.util.UUIDManager;
import de.tub.citydb.util.Util;

public class DBCityObject implements DBImporter {
	private final Logger LOG = Logger.getInstance();
	
	private final Connection batchConn;
	private final CityGMLFactory cityGMLFactory;
	private final Config config;
	private final DBImporterManager dbImporterManager;

	private PreparedStatement psCityObject;
	private DBCityObjectGenericAttrib genericAttributeImporter;
	private DBExternalReference externalReferenceImporter;
	private DBAppearance appearanceImporter;

	private String updatingPerson;
	private String reasonForUpdate;
	private String lineage;
	private String importFileName;
	private int dbSrid;
	private boolean replaceGmlId;
	private boolean rememberGmlId;
	private boolean importAppearance;
	private int batchCounter;

	public DBCityObject(Connection batchConn, CityGMLFactory cityGMLFactory, Config config, DBImporterManager dbImporterManager) throws SQLException {
		this.batchConn = batchConn;
		this.cityGMLFactory = cityGMLFactory;
		this.config = config;
		this.dbImporterManager = dbImporterManager;

		init();
	}

	private void init() throws SQLException {
		replaceGmlId = config.getProject().getImporter().getGmlId().isUUIDModeReplace();
		rememberGmlId = config.getProject().getImporter().getGmlId().isSetKeepGmlIdAsExternalReference();
		dbSrid = config.getInternal().getOpenConnection().getMetaData().getSrid();
		importAppearance = config.getProject().getImporter().getAppearances().isSetImportAppearance();
		String gmlIdCodespace = config.getInternal().getCurrentGmlIdCodespace();

		if (rememberGmlId)
			importFileName = config.getInternal().getCurrentImportFileName();
		
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
	}

	public long insert(CityObject cityObject, long cityObjectId) throws SQLException {
		// ID
		psCityObject.setLong(1, cityObjectId);

		// CLASS_ID
		int classId = Util.cityObject2classId(cityObject.getCityGMLClass());
		psCityObject.setInt(2, classId);

		// gml:id
		String origGmlId = cityObject.getId();
		if (replaceGmlId) {
			String gmlId = UUIDManager.randomUUID();

			// mapping entry
			if (cityObject.isSetId()) {
				dbImporterManager.putGmlId(cityObject.getId(), cityObjectId, -1, false, gmlId, cityObject.getCityGMLClass());

				if (rememberGmlId) {	
					CoreModule core = (CoreModule)cityObject.getCityGMLModule().getModuleDependencies().getModule(CityGMLModuleType.CORE);

					ExternalReference externalReference = cityGMLFactory.createExternalReference(core);
					externalReference.setInformationSystem(importFileName);

					ExternalObject externalObject = cityGMLFactory.createExternalObject(core);
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
				cityObject.setId(UUIDManager.randomUUID());
		}

		psCityObject.setString(3, cityObject.getId());

		// gml:boundedBy
		if (cityObject.isSetBoundedBy() && (!cityObject.getBoundedBy().isSetEnvelope() ||
				!cityObject.getBoundedBy().getEnvelope().isSetLowerCorner() ||
				!cityObject.getBoundedBy().getEnvelope().isSetUpperCorner()))
			cityObject.getBoundedBy().convertEnvelope();
		else 
			cityObject.calcBoundedBy();

		if (cityObject.isSetBoundedBy()) {
			int[] elemInfo = { 1, 1003, 3 };

			// re-check this. we are assuming to find an envelope with upper and
			// lower corner set...
			List<Double> points = new ArrayList<Double>();
			points.addAll(cityObject.getBoundedBy().getEnvelope().getLowerCorner().getValue());
			points.addAll(cityObject.getBoundedBy().getEnvelope().getUpperCorner().getValue());

			double[] ordinates = new double[points.size()];
			int i = 0;
			for (Double point : points)
				ordinates[i++] = point.doubleValue();

			JGeometry boundedBy = new JGeometry(3003, dbSrid, elemInfo, ordinates);
			STRUCT obj = SyncJGeometry.syncStore(boundedBy, batchConn);

			psCityObject.setObject(4, obj);
		} else {
			psCityObject.setNull(4, Types.STRUCT, "MDSYS.SDO_GEOMETRY");
		}

		psCityObject.addBatch();
		if (++batchCounter == Internal.ORACLE_MAX_BATCH_SIZE)
			dbImporterManager.executeBatch(DBImporterEnum.CITYOBJECT);

		// genericAttributes
		if (cityObject.isSetGenericAttribute())
			for (GenericAttribute genericAttribute : cityObject.getGenericAttribute())
				genericAttributeImporter.insert(genericAttribute, cityObjectId);

		// externalReference
		if (cityObject.isSetExternalReference())
			for (ExternalReference externalReference : cityObject.getExternalReference())
				externalReferenceImporter.insert(externalReference, cityObjectId);

		// generalizesTo
		if (cityObject.isSetGeneralizesTo()) {
			for (GeneralizationRelation generalizesTo : cityObject.getGeneralizesTo()) {
				if (generalizesTo.isSetObject()) {
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
								DBTableEnum.CITYOBJECT,
								href,
								DBTableEnum.CITYOBJECT
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
						long id = appearanceImporter.insert(appearanceProperty.getAppearance(), CityGMLClass.CITYOBJECT, cityObjectId);
						
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

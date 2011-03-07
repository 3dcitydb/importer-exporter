package de.tub.citydb.db.importer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import oracle.spatial.geometry.JGeometry;
import oracle.spatial.geometry.SyncJGeometry;
import oracle.sql.STRUCT;

import org.citygml4j.model.citygml.building.BuildingFurniture;
import org.citygml4j.model.citygml.core.ImplicitGeometry;
import org.citygml4j.model.citygml.core.ImplicitRepresentationProperty;
import org.citygml4j.model.gml.GeometryProperty;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.db.DBTableEnum;
import de.tub.citydb.db.xlink.DBXlinkBasic;
import de.tub.citydb.util.Util;

public class DBBuildingFurniture implements DBImporter {
	private final Connection batchConn;
	private final Config config;
	private final DBImporterManager dbImporterManager;

	private PreparedStatement psBuildingFurniture;
	private DBCityObject cityObjectImporter;
	private DBSurfaceGeometry surfaceGeometryImporter;
	private DBImplicitGeometry implicitGeometryImporter;
	private DBSdoGeometry sdoGeometry;

	private String gmlNameDelimiter;
	private int batchCounter;

	public DBBuildingFurniture(Connection batchConn, Config config, DBImporterManager dbImporterManager) throws SQLException {
		this.batchConn = batchConn;
		this.config = config;
		this.dbImporterManager = dbImporterManager;

		init();
	}

	private void init() throws SQLException {
		gmlNameDelimiter = config.getInternal().getGmlNameDelimiter();

		psBuildingFurniture = batchConn.prepareStatement("insert into BUILDING_FURNITURE (ID, NAME, NAME_CODESPACE, DESCRIPTION, CLASS, FUNCTION, USAGE, ROOM_ID, LOD4_GEOMETRY_ID, " +
				"LOD4_IMPLICIT_REP_ID, LOD4_IMPLICIT_REF_POINT, LOD4_IMPLICIT_TRANSFORMATION) values " +
		"(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

		surfaceGeometryImporter = (DBSurfaceGeometry)dbImporterManager.getDBImporter(DBImporterEnum.SURFACE_GEOMETRY);
		cityObjectImporter = (DBCityObject)dbImporterManager.getDBImporter(DBImporterEnum.CITYOBJECT);
		implicitGeometryImporter = (DBImplicitGeometry)dbImporterManager.getDBImporter(DBImporterEnum.IMPLICIT_GEOMETRY);
		sdoGeometry = (DBSdoGeometry)dbImporterManager.getDBImporter(DBImporterEnum.SDO_GEOMETRY);
	}

	public long insert(BuildingFurniture buildingFurniture, long roomId) throws SQLException {
		long buildingFurnitureId = dbImporterManager.getDBId(DBSequencerEnum.CITYOBJECT_SEQ);
		if (buildingFurnitureId == 0)
			return 0;

		// CityObject
		cityObjectImporter.insert(buildingFurniture, buildingFurnitureId);

		// BuildingFurniture
		// ID
		psBuildingFurniture.setLong(1, buildingFurnitureId);

		// gml:name
		if (buildingFurniture.isSetName()) {
			String[] dbGmlName = Util.gmlName2dbString(buildingFurniture, gmlNameDelimiter);

			psBuildingFurniture.setString(2, dbGmlName[0]);
			psBuildingFurniture.setString(3, dbGmlName[1]);
		} else {
			psBuildingFurniture.setNull(2, Types.VARCHAR);
			psBuildingFurniture.setNull(3, Types.VARCHAR);
		}

		// gml:description
		if (buildingFurniture.isSetDescription()) {
			String description = buildingFurniture.getDescription().getValue();

			if (description != null)
				description = description.trim();

			psBuildingFurniture.setString(4, description);
		} else {
			psBuildingFurniture.setNull(4, Types.VARCHAR);
		}

		// citygml:class
		if (buildingFurniture.isSetClazz())
			psBuildingFurniture.setString(5, buildingFurniture.getClazz().trim());
		else
			psBuildingFurniture.setNull(5, Types.VARCHAR);

		// citygml:function
		if (buildingFurniture.isSetFunction()) {
			psBuildingFurniture.setString(6, Util.collection2string(buildingFurniture.getFunction(), " "));
		} else {
			psBuildingFurniture.setNull(6, Types.VARCHAR);
		}

		// citygml:usage
		if (buildingFurniture.isSetUsage()) {
			psBuildingFurniture.setString(7, Util.collection2string(buildingFurniture.getUsage(), " "));
		} else {
			psBuildingFurniture.setNull(7, Types.VARCHAR);
		}

		// ROOM_ID
		psBuildingFurniture.setLong(8, roomId);

		// Geometry		
		long geometryId = 0;
		if (buildingFurniture.isSetLod4Geometry()) {
			GeometryProperty geometryProperty = buildingFurniture.getLod4Geometry();
			
			if (geometryProperty.isSetGeometry()) {
				geometryId = surfaceGeometryImporter.insert(geometryProperty.getGeometry(), buildingFurnitureId);
			} else {
				// xlink
				String href = geometryProperty.getHref();

				if (href != null && href.length() != 0) {
					DBXlinkBasic xlink = new DBXlinkBasic(
							buildingFurnitureId,
							DBTableEnum.BUILDING_FURNITURE,
							href,
							DBTableEnum.SURFACE_GEOMETRY
					);

					xlink.setAttrName("LOD4_GEOMETRY_ID");
					dbImporterManager.propagateXlink(xlink);
				}
			}
		}

		if (geometryId != 0)
			psBuildingFurniture.setLong(9, geometryId);
		else
			psBuildingFurniture.setNull(9, 0);

		// implicit geometry
		JGeometry pointGeom = null;
		String matrixString = null;
		long implicitId = 0;

		if (buildingFurniture.isSetLoD4ImplicitRepresentation()) {
			ImplicitRepresentationProperty implicit = buildingFurniture.getLoD4ImplicitRepresentation();

			if (implicit.isSetObject()) {
				ImplicitGeometry geometry = implicit.getObject();

				// reference Point
				if (geometry.isSetReferencePoint())
					pointGeom = sdoGeometry.getPoint(geometry.getReferencePoint());

				// transformation matrix
				if (geometry.isSetTransformationMatrix())
					matrixString = Util.collection2string(geometry.getTransformationMatrix().getMatrix().toRowPackedList(), " ");
				
				// reference to IMPLICIT_GEOMETRY
				implicitId = implicitGeometryImporter.insert(geometry, buildingFurnitureId);
			}
		}

		if (implicitId != 0)
			psBuildingFurniture.setLong(10, implicitId);
		else
			psBuildingFurniture.setNull(10, 0);

		if (pointGeom != null) {
			STRUCT obj = SyncJGeometry.syncStore(pointGeom, batchConn);
			psBuildingFurniture.setObject(11, obj);
		} else
			psBuildingFurniture.setNull(11, Types.STRUCT, "MDSYS.SDO_GEOMETRY");

		if (matrixString != null)
			psBuildingFurniture.setString(12, matrixString);
		else
			psBuildingFurniture.setNull(12, Types.VARCHAR);

		psBuildingFurniture.addBatch();
		if (++batchCounter == Internal.ORACLE_MAX_BATCH_SIZE)
			dbImporterManager.executeBatch(DBImporterEnum.BUILDING_FURNITURE);
		
		return buildingFurnitureId;
	}

	@Override
	public void executeBatch() throws SQLException {
		psBuildingFurniture.executeBatch();
		batchCounter = 0;
	}

	@Override
	public void close() throws SQLException {
		psBuildingFurniture.close();
	}

	@Override
	public DBImporterEnum getDBImporterType() {
		return DBImporterEnum.BUILDING_FURNITURE;
	}

}

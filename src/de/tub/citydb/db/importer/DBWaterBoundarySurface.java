package de.tub.citydb.db.importer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.waterbody.WaterBoundarySurface;
import org.citygml4j.model.citygml.waterbody.WaterSurface;
import org.citygml4j.model.gml.SurfaceProperty;

import de.tub.citydb.db.DBTableEnum;
import de.tub.citydb.db.xlink.DBXlinkBasic;
import de.tub.citydb.util.Util;

public class DBWaterBoundarySurface implements DBImporter {
	private final Connection batchConn;
	private final DBImporterManager dbImporterManager;

	private PreparedStatement psWaterBoundarySurface;
	private DBCityObject cityObjectImporter;
	private DBSurfaceGeometry surfaceGeometryImporter;
	private DBWaterBodToWaterBndSrf bodyToSurfaceImporter;
	
	public DBWaterBoundarySurface(Connection batchConn, DBImporterManager dbImporterManager) throws SQLException {
		this.batchConn = batchConn;
		this.dbImporterManager = dbImporterManager;

		init();
	}

	private void init() throws SQLException {		
		psWaterBoundarySurface = batchConn.prepareStatement("insert into WATERBOUNDARY_SURFACE (ID, NAME, NAME_CODESPACE, DESCRIPTION, TYPE, WATER_LEVEL, " +
				"LOD2_SURFACE_ID, LOD3_SURFACE_ID, LOD4_SURFACE_ID) values " +
				"(?, ?, ?, ?, ?, ?, ?, ?, ?)");

		surfaceGeometryImporter = (DBSurfaceGeometry)dbImporterManager.getDBImporter(DBImporterEnum.SURFACE_GEOMETRY);
		cityObjectImporter = (DBCityObject)dbImporterManager.getDBImporter(DBImporterEnum.CITYOBJECT);
		bodyToSurfaceImporter = (DBWaterBodToWaterBndSrf)dbImporterManager.getDBImporter(DBImporterEnum.WATERBOD_TO_WATERBND_SRF);
	}

	public long insert(WaterBoundarySurface waterBoundarySurface, long parentId) throws SQLException {
		long waterBoundarySurfaceId = dbImporterManager.getDBId(DBSequencerEnum.CITYOBJECT_SEQ);
    	if (waterBoundarySurfaceId == 0)
    		return 0;

		// CityObject
    	cityObjectImporter.insert(waterBoundarySurface, waterBoundarySurfaceId);

		// BoundarySurface
        // ID
    	psWaterBoundarySurface.setLong(1, waterBoundarySurfaceId);

		// gml:name
    	if (waterBoundarySurface.isSetName()) {
			String[] dbGmlName = Util.gmlName2dbString(waterBoundarySurface);

			psWaterBoundarySurface.setString(2, dbGmlName[0]);
			psWaterBoundarySurface.setString(3, dbGmlName[1]);
		} else {
			psWaterBoundarySurface.setNull(2, Types.VARCHAR);
			psWaterBoundarySurface.setNull(3, Types.VARCHAR);
		}

		// gml:description
		if (waterBoundarySurface.isSetDescription()) {
			String description = waterBoundarySurface.getDescription().getValue();
			psWaterBoundarySurface.setString(4, description);
		} else {
			psWaterBoundarySurface.setNull(4, Types.VARCHAR);
		}

		// TYPE
		psWaterBoundarySurface.setString(5, waterBoundarySurface.getCityGMLClass().toString());

		// waterLevel
		if (waterBoundarySurface.getCityGMLClass() == CityGMLClass.WATERSURFACE)
			psWaterBoundarySurface.setString(6, ((WaterSurface)waterBoundarySurface).getWaterLevel());
		else
			psWaterBoundarySurface.setNull(6, 0);

		// Geometry
        for (int lod = 2; lod < 5; lod++) {
        	SurfaceProperty surfaceProperty = null;
        	long abstractSurfaceId = 0;

    		switch (lod) {
    		case 2:
    			surfaceProperty = waterBoundarySurface.getLod2Surface();
    			break;
    		case 3:
    			surfaceProperty = waterBoundarySurface.getLod3Surface();
    			break;
    		case 4:
    			surfaceProperty = waterBoundarySurface.getLod4Surface();
    			break;
    		}

    		if (surfaceProperty != null) {
    			if (surfaceProperty.isSetSurface()) {
    				abstractSurfaceId = surfaceGeometryImporter.insert(surfaceProperty.getSurface(), waterBoundarySurfaceId);
    			} else {
    				// xlink
					String href = surfaceProperty.getHref();

        			if (href != null && href.length() != 0) {
        				DBXlinkBasic xlink = new DBXlinkBasic(
        						waterBoundarySurfaceId,
        						DBTableEnum.WATERBOUNDARY_SURFACE,
        						href,
        						DBTableEnum.SURFACE_GEOMETRY
        				);

        				xlink.setAttrName("LOD" + lod + "_SURFACE_ID");
        				dbImporterManager.propagateXlink(xlink);
        			}
    			}
    		}

    		switch (lod) {
        	case 2:
        		if (abstractSurfaceId != 0)
        			psWaterBoundarySurface.setLong(7, abstractSurfaceId);
        		else
        			psWaterBoundarySurface.setNull(7, 0);
        		break;
        	case 3:
        		if (abstractSurfaceId != 0)
        			psWaterBoundarySurface.setLong(8, abstractSurfaceId);
        		else
        			psWaterBoundarySurface.setNull(8, 0);
        		break;
        	case 4:
        		if (abstractSurfaceId != 0)
        			psWaterBoundarySurface.setLong(9, abstractSurfaceId);
        		else
        			psWaterBoundarySurface.setNull(9, 0);
        		break;
        	}
        }

        psWaterBoundarySurface.addBatch();

        // boundary surface to waterBody
        bodyToSurfaceImporter.insert(waterBoundarySurfaceId, parentId);

        return waterBoundarySurfaceId;
	}

	@Override
	public void executeBatch() throws SQLException {
		psWaterBoundarySurface.executeBatch();
	}

	@Override
	public void close() throws SQLException {
		psWaterBoundarySurface.close();
	}

	@Override
	public DBImporterEnum getDBImporterType() {
		return DBImporterEnum.WATERBOUNDARY_SURFACE;
	}

}

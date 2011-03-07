package de.tub.citydb.db.importer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import org.citygml4j.model.citygml.transportation.AuxiliaryTrafficArea;
import org.citygml4j.model.citygml.transportation.TrafficArea;
import org.citygml4j.model.gml.MultiSurfaceProperty;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.db.DBTableEnum;
import de.tub.citydb.db.xlink.DBXlinkBasic;
import de.tub.citydb.util.Util;

public class DBTrafficArea implements DBImporter {
	private final Connection batchConn;
	private final Config config;
	private final DBImporterManager dbImporterManager;

	private PreparedStatement psTrafficArea;
	private DBCityObject cityObjectImporter;
	private DBSurfaceGeometry surfaceGeometryImporter;

	private String gmlNameDelimiter;
	private int batchCounter;
	
	public DBTrafficArea(Connection batchConn, Config config, DBImporterManager dbImporterManager) throws SQLException {
		this.batchConn = batchConn;
		this.config = config;
		this.dbImporterManager = dbImporterManager;

		init();
	}

	private void init() throws SQLException {
		gmlNameDelimiter = config.getInternal().getGmlNameDelimiter();
		
		psTrafficArea = batchConn.prepareStatement("insert into TRAFFIC_AREA (ID, IS_AUXILIARY, NAME, NAME_CODESPACE, DESCRIPTION, FUNCTION, USAGE, " +
				"SURFACE_MATERIAL, LOD2_MULTI_SURFACE_ID, LOD3_MULTI_SURFACE_ID, LOD4_MULTI_SURFACE_ID, " +
				"TRANSPORTATION_COMPLEX_ID) values "+
				"(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

		surfaceGeometryImporter = (DBSurfaceGeometry)dbImporterManager.getDBImporter(DBImporterEnum.SURFACE_GEOMETRY);
		cityObjectImporter = (DBCityObject)dbImporterManager.getDBImporter(DBImporterEnum.CITYOBJECT);
	}

	public long insert(TrafficArea trafficArea, long parentId) throws SQLException {
		long trafficAreaId = dbImporterManager.getDBId(DBSequencerEnum.CITYOBJECT_SEQ);
		if (trafficAreaId == 0)
			return 0;

		// CityObject
		cityObjectImporter.insert(trafficArea, trafficAreaId);

		// TrafficArea
		// ID
		psTrafficArea.setLong(1, trafficAreaId);

		// isAuxiliary
		psTrafficArea.setLong(2, 0);

		// gml:name
		if (trafficArea.isSetName()) {
			String[] dbGmlName = Util.gmlName2dbString(trafficArea, gmlNameDelimiter);

			psTrafficArea.setString(3, dbGmlName[0]);
			psTrafficArea.setString(4, dbGmlName[1]);
		} else {
			psTrafficArea.setNull(3, Types.VARCHAR);
			psTrafficArea.setNull(4, Types.VARCHAR);
		}

		// gml:description
		if (trafficArea.isSetDescription()) {
			String description = trafficArea.getDescription().getValue();

			if (description != null)
				description = description.trim();

			psTrafficArea.setString(5, description);
		} else {
			psTrafficArea.setNull(5, Types.VARCHAR);
		}

		// citygml:function
		if (trafficArea.isSetFunction()) {
			psTrafficArea.setString(6, Util.collection2string(trafficArea.getFunction(), " "));
		} else {
			psTrafficArea.setNull(6, Types.VARCHAR);
		}

		// citygml:usage
		if (trafficArea.isSetUsage()) {
			psTrafficArea.setString(7, Util.collection2string(trafficArea.getUsage(), " "));
		} else {
			psTrafficArea.setNull(7, Types.VARCHAR);
		}

		// surface material
		if (trafficArea.isSetSurfaceMaterial())
			psTrafficArea.setString(8, trafficArea.getSurfaceMaterial());
		else
			psTrafficArea.setNull(8, Types.VARCHAR);

		// Geometry
        for (int lod = 2; lod < 5; lod++) {
        	MultiSurfaceProperty multiSurfaceProperty = null;
        	long multiSurfaceId = 0;

    		switch (lod) {
    		case 2:
    			multiSurfaceProperty = trafficArea.getLod2MultiSurface();
    			break;
    		case 3:
    			multiSurfaceProperty = trafficArea.getLod3MultiSurface();
    			break;
    		case 4:
    			multiSurfaceProperty = trafficArea.getLod4MultiSurface();
    			break;
    		}

    		if (multiSurfaceProperty != null) {
    			if (multiSurfaceProperty.isSetMultiSurface()) {
    				multiSurfaceId = surfaceGeometryImporter.insert(multiSurfaceProperty.getMultiSurface(), trafficAreaId);
    			} else {
    				// xlink
					String href = multiSurfaceProperty.getHref();

        			if (href != null && href.length() != 0) {
        				DBXlinkBasic xlink = new DBXlinkBasic(
        						trafficAreaId,
        						DBTableEnum.TRAFFIC_AREA,
        						href,
        						DBTableEnum.SURFACE_GEOMETRY
        				);

        				xlink.setAttrName("LOD" + lod + "_MULTI_SURFACE_ID");
        				dbImporterManager.propagateXlink(xlink);
        			}
    			}
    		}

    		switch (lod) {
    		case 2:
        		if (multiSurfaceId != 0)
        			psTrafficArea.setLong(9, multiSurfaceId);
        		else
        			psTrafficArea.setNull(9, 0);
        		break;
        	case 3:
        		if (multiSurfaceId != 0)
        			psTrafficArea.setLong(10, multiSurfaceId);
        		else
        			psTrafficArea.setNull(10, 0);
        		break;
        	case 4:
        		if (multiSurfaceId != 0)
        			psTrafficArea.setLong(11, multiSurfaceId);
        		else
        			psTrafficArea.setNull(11, 0);
        		break;
        	}
        }

        // reference to transportation complex
        psTrafficArea.setLong(12, parentId);

        psTrafficArea.addBatch();
        if (++batchCounter == Internal.ORACLE_MAX_BATCH_SIZE)
			dbImporterManager.executeBatch(DBImporterEnum.TRAFFIC_AREA);
        
		return trafficAreaId;
	}

	public long insert(AuxiliaryTrafficArea auxiliaryTrafficArea, long parentId) throws SQLException {
		long auxiliaryTrafficAreaId = dbImporterManager.getDBId(DBSequencerEnum.CITYOBJECT_SEQ);
		if (auxiliaryTrafficAreaId == 0)
			return 0;

		// CityObject
		cityObjectImporter.insert(auxiliaryTrafficArea, auxiliaryTrafficAreaId);

		// TrafficArea
		// ID
		psTrafficArea.setLong(1, auxiliaryTrafficAreaId);

		// isAuxiliary
		psTrafficArea.setLong(2, 1);

		// gml:name
		if (auxiliaryTrafficArea.isSetName()) {
			String[] dbGmlName = Util.gmlName2dbString(auxiliaryTrafficArea, gmlNameDelimiter);

			psTrafficArea.setString(3, dbGmlName[0]);
			psTrafficArea.setString(4, dbGmlName[1]);
		} else {
			psTrafficArea.setNull(3, Types.VARCHAR);
			psTrafficArea.setNull(4, Types.VARCHAR);
		}

		// gml:description
		if (auxiliaryTrafficArea.isSetDescription()) {
			String description = auxiliaryTrafficArea.getDescription().getValue();

			if (description != null)
				description = description.trim();

			psTrafficArea.setString(5, description);
		} else {
			psTrafficArea.setNull(5, Types.VARCHAR);
		}

		// citygml:function
		if (auxiliaryTrafficArea.isSetFunction()) {
			psTrafficArea.setString(6, Util.collection2string(auxiliaryTrafficArea.getFunction(), " "));
		} else {
			psTrafficArea.setNull(6, Types.VARCHAR);
		}

		// citygml:usage
		psTrafficArea.setNull(7, Types.VARCHAR);

		// surface material
		if (auxiliaryTrafficArea.isSetSurfaceMaterial())
			psTrafficArea.setString(8, auxiliaryTrafficArea.getSurfaceMaterial());
		else
			psTrafficArea.setNull(8, Types.VARCHAR);

		// Geometry
        for (int lod = 2; lod < 5; lod++) {
        	MultiSurfaceProperty multiSurfaceProperty = null;
        	long multiSurfaceId = 0;

    		switch (lod) {
    		case 2:
    			multiSurfaceProperty = auxiliaryTrafficArea.getLod2MultiSurface();
    			break;
    		case 3:
    			multiSurfaceProperty = auxiliaryTrafficArea.getLod3MultiSurface();
    			break;
    		case 4:
    			multiSurfaceProperty = auxiliaryTrafficArea.getLod4MultiSurface();
    			break;
    		}

    		if (multiSurfaceProperty != null) {
    			if (multiSurfaceProperty.isSetMultiSurface()) {
    				multiSurfaceId = surfaceGeometryImporter.insert(multiSurfaceProperty.getMultiSurface(), auxiliaryTrafficAreaId);
    			} else {
    				// xlink
					String href = multiSurfaceProperty.getHref();

        			if (href != null && href.length() != 0) {
        				DBXlinkBasic xlink = new DBXlinkBasic(
        						auxiliaryTrafficAreaId,
        						DBTableEnum.TRAFFIC_AREA,
        						href,
        						DBTableEnum.SURFACE_GEOMETRY
        				);

        				xlink.setAttrName("LOD" + lod + "_MULTI_SURFACE_ID");
        				dbImporterManager.propagateXlink(xlink);
        			}
    			}
    		}

    		switch (lod) {
    		case 2:
        		if (multiSurfaceId != 0)
        			psTrafficArea.setLong(9, multiSurfaceId);
        		else
        			psTrafficArea.setNull(9, 0);
        		break;
        	case 3:
        		if (multiSurfaceId != 0)
        			psTrafficArea.setLong(10, multiSurfaceId);
        		else
        			psTrafficArea.setNull(10, 0);
        		break;
        	case 4:
        		if (multiSurfaceId != 0)
        			psTrafficArea.setLong(11, multiSurfaceId);
        		else
        			psTrafficArea.setNull(11, 0);
        		break;
        	}
        }

        // reference to transportation complex
        psTrafficArea.setLong(12, parentId);

        psTrafficArea.addBatch();
        if (++batchCounter == Internal.ORACLE_MAX_BATCH_SIZE)
			dbImporterManager.executeBatch(DBImporterEnum.TRAFFIC_AREA);
        
		return auxiliaryTrafficAreaId;
	}

	@Override
	public void executeBatch() throws SQLException {
		psTrafficArea.executeBatch();
		batchCounter = 0;
	}

	@Override
	public void close() throws SQLException {
		psTrafficArea.close();
	}

	@Override
	public DBImporterEnum getDBImporterType() {
		return DBImporterEnum.TRAFFIC_AREA;
	}

}

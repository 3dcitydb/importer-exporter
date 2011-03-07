package de.tub.citydb.db.importer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import oracle.spatial.geometry.JGeometry;
import oracle.spatial.geometry.SyncJGeometry;
import oracle.sql.STRUCT;
import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.db.DBTableEnum;
import de.tub.citydb.db.xlink.DBXlinkBasic;
import de.tub.citydb.event.info.LogMessageEnum;
import de.tub.citydb.event.info.LogMessageEvent;
import de.tub.citydb.util.Util;
import de.tub.citygml4j.implementation.gml._3_1_1.GeometricComplexImpl;
import de.tub.citygml4j.implementation.gml._3_1_1.GeometricPrimitivePropertyImpl;
import de.tub.citygml4j.model.citygml.transportation.AuxiliaryTrafficAreaProperty;
import de.tub.citygml4j.model.citygml.transportation.TrafficAreaProperty;
import de.tub.citygml4j.model.citygml.transportation.TransportationComplex;
import de.tub.citygml4j.model.gml.AbstractGeometricPrimitive;
import de.tub.citygml4j.model.gml.GeometricComplex;
import de.tub.citygml4j.model.gml.GeometricComplexProperty;
import de.tub.citygml4j.model.gml.GeometricPrimitiveProperty;
import de.tub.citygml4j.model.gml.MultiSurfaceProperty;

public class DBTransportationComplex implements DBImporter {
	private final Connection batchConn;
	private final Config config;
	private final DBImporterManager dbImporterManager;

	private PreparedStatement psTransComplex;
	private DBCityObject cityObjectImporter;
	private DBSurfaceGeometry surfaceGeometryImporter;
	private DBTrafficArea trafficAreaImporter;
	private DBSdoGeometry sdoGeometry;
	
	private String gmlNameDelimiter;
	private int batchCounter;

	public DBTransportationComplex(Connection batchConn, Config config, DBImporterManager dbImporterManager) throws SQLException {
		this.batchConn = batchConn;
		this.config = config;
		this.dbImporterManager = dbImporterManager;

		init();
	}

	private void init() throws SQLException {
		gmlNameDelimiter = config.getInternal().getGmlNameDelimiter();
		
		psTransComplex = batchConn.prepareStatement("insert into TRANSPORTATION_COMPLEX (ID, NAME, NAME_CODESPACE, DESCRIPTION, FUNCTION, USAGE, " +
				"TYPE, LOD1_MULTI_SURFACE_ID, LOD2_MULTI_SURFACE_ID, LOD3_MULTI_SURFACE_ID, LOD4_MULTI_SURFACE_ID, LOD0_NETWORK) values " +
				"(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

		surfaceGeometryImporter = (DBSurfaceGeometry)dbImporterManager.getDBImporter(DBImporterEnum.SURFACE_GEOMETRY);
		cityObjectImporter = (DBCityObject)dbImporterManager.getDBImporter(DBImporterEnum.CITYOBJECT);
		trafficAreaImporter = (DBTrafficArea)dbImporterManager.getDBImporter(DBImporterEnum.TRAFFIC_AREA);
		sdoGeometry = (DBSdoGeometry)dbImporterManager.getDBImporter(DBImporterEnum.SDO_GEOMETRY);
	}

	public long insert(TransportationComplex transComplex) throws SQLException {
		long transComplexId = dbImporterManager.getDBId(DBSequencerEnum.CITYOBJECT_SEQ);
		boolean success = false;

		if (transComplexId != 0)
			success = insert(transComplex, transComplexId);

		if (success)
			return transComplexId;
		else
			return 0;
	}

	private boolean insert(TransportationComplex transComplex, long transComplexId) throws SQLException {
		// CityObject
		long cityObjectId = cityObjectImporter.insert(transComplex, transComplexId);
		if (cityObjectId == 0)
			return false;

		// TransportationComplex
		// ID
		psTransComplex.setLong(1, transComplexId);

		// gml:name
		if (transComplex.getName() != null) {
			String[] dbGmlName = Util.gmlName2dbString(transComplex, gmlNameDelimiter);

			psTransComplex.setString(2, dbGmlName[0]);
			psTransComplex.setString(3, dbGmlName[1]);
		} else {
			psTransComplex.setNull(2, Types.VARCHAR);
			psTransComplex.setNull(3, Types.VARCHAR);
		}

		// gml:description
		if (transComplex.getDescription() != null) {
			String description = transComplex.getDescription().getValue();

			if (description != null)
				description = description.trim();

			psTransComplex.setString(4, description);
		} else {
			psTransComplex.setNull(4, Types.VARCHAR);
		}

		// citygml:function
		if (transComplex.getFunction() != null) {
			List<String> functionList = transComplex.getFunction();
			psTransComplex.setString(5, Util.collection2string(functionList, " "));
		} else {
			psTransComplex.setNull(5, Types.VARCHAR);
		}

		// citygml:usage
		if (transComplex.getUsage() != null) {
			List<String> usageList = transComplex.getUsage();
			psTransComplex.setString(6, Util.collection2string(usageList, " "));
		} else {
			psTransComplex.setNull(6, Types.VARCHAR);
		}

		// TYPE
		psTransComplex.setString(7, transComplex.getCityGMLClass().toString());

		// Geometry
        for (int lod = 1; lod < 5; lod++) {
        	MultiSurfaceProperty multiSurfaceProperty = null;
        	long multiSurfaceId = 0;

    		switch (lod) {
    		case 1:
    			multiSurfaceProperty = transComplex.getLod1MultiSurface();
    			break;
    		case 2:
    			multiSurfaceProperty = transComplex.getLod2MultiSurface();
    			break;
    		case 3:
    			multiSurfaceProperty = transComplex.getLod3MultiSurface();
    			break;
    		case 4:
    			multiSurfaceProperty = transComplex.getLod4MultiSurface();
    			break;
    		}

    		if (multiSurfaceProperty != null) {
    			if (multiSurfaceProperty.getMultiSurface() != null) {
    				multiSurfaceId = surfaceGeometryImporter.insert(multiSurfaceProperty.getMultiSurface(), transComplexId);
    			} else {
    				// xlink
					String href = multiSurfaceProperty.getHref();

        			if (href != null && href.length() != 0) {
        				DBXlinkBasic xlink = new DBXlinkBasic(
        						transComplexId,
        						DBTableEnum.TRANSPORTATION_COMPLEX,
        						href,
        						DBTableEnum.SURFACE_GEOMETRY
        				);

        				xlink.setAttrName("LOD" + lod + "_MULTI_SURFACE_ID");
        				dbImporterManager.propagateXlink(xlink);
        			}
    			}
    		}

    		switch (lod) {
    		case 1:
        		if (multiSurfaceId != 0)
        			psTransComplex.setLong(8, multiSurfaceId);
        		else
        			psTransComplex.setNull(8, 0);
        		break;
    		case 2:
        		if (multiSurfaceId != 0)
        			psTransComplex.setLong(9, multiSurfaceId);
        		else
        			psTransComplex.setNull(9, 0);
        		break;
        	case 3:
        		if (multiSurfaceId != 0)
        			psTransComplex.setLong(10, multiSurfaceId);
        		else
        			psTransComplex.setNull(10, 0);
        		break;
        	case 4:
        		if (multiSurfaceId != 0)
        			psTransComplex.setLong(11, multiSurfaceId);
        		else
        			psTransComplex.setNull(11, 0);
        		break;
        	}
        }

        // lod0Network
        List<GeometricComplexProperty> lod0Network = transComplex.getLod0Network();
        if (lod0Network != null) {
        	GeometricComplex aggregateComplex = new GeometricComplexImpl();
        	JGeometry multiCurveGeom = null;
        	
        	for (GeometricComplexProperty complexProperty : lod0Network) {
        		// for lod0Network we just consider appropriate curve geometries
        		
        		if (complexProperty.getCompositeCurve() != null) {
        			GeometricPrimitiveProperty primitiveProperty = new GeometricPrimitivePropertyImpl();
        			primitiveProperty.setGeometricPrimitive(complexProperty.getCompositeCurve());
        			
        			aggregateComplex.addElement(primitiveProperty);
        		} 
        		
        		else if (complexProperty.getGeometricComplex() != null) {
        			GeometricComplex complex = complexProperty.getGeometricComplex();        			
        			List<GeometricPrimitiveProperty> primitivePropertyList = complex.getElement();
        			
        			if (primitivePropertyList != null) {
        				for (GeometricPrimitiveProperty primitiveProperty : primitivePropertyList) {
        					AbstractGeometricPrimitive primitive = primitiveProperty.getGeometricPrimitive();
        					
        					if (primitive != null) {        						
        						switch (primitive.getGMLClass()) {
        						case LINESTRING:
        							aggregateComplex.addElement(primitiveProperty);
        							break;
        						case COMPOSITECURVE:
        							aggregateComplex.addElement(primitiveProperty);
        							break;
        						case ORIENTABLECURVE:
        							aggregateComplex.addElement(primitiveProperty);
        							break;
        						default:
        							// geometry type not supported by lod0Network
        						}
        					} else {
        						// xlinks are not supported
        					}
        				}
        			}
        		}
        		
        		else if (complexProperty.getHref() != null) {
        			// xlinks are not supported
        		}
        			
        		else {
        			// other geometry types are not supported for lod0Network
        		}
        	}
        	
        	if (aggregateComplex.getElement() != null && !aggregateComplex.getElement().isEmpty())      		
        		multiCurveGeom = sdoGeometry.getMultiCurve(aggregateComplex);
        	
        	if (multiCurveGeom != null) {
        		STRUCT multiCurveGeomObj = SyncJGeometry.syncStore(multiCurveGeom, batchConn);
				psTransComplex.setObject(12, multiCurveGeomObj);
        	} else
        		psTransComplex.setNull(12, Types.STRUCT, "MDSYS.SDO_GEOMETRY");
        	
        } else
        	psTransComplex.setNull(12, Types.STRUCT, "MDSYS.SDO_GEOMETRY");
        
        psTransComplex.addBatch();
		if (++batchCounter == Internal.ORACLE_MAX_BATCH_SIZE)
			dbImporterManager.executeBatch(DBImporterEnum.TRANSPORTATION_COMPLEX);
		        
        // AuxiliaryTrafficArea
        List<AuxiliaryTrafficAreaProperty> auxTrafficAreaPropertyList = transComplex.getAuxiliaryTrafficArea();
        if (auxTrafficAreaPropertyList != null) {
        	for (AuxiliaryTrafficAreaProperty auxTrafficAreaProperty : auxTrafficAreaPropertyList) {
        		if (auxTrafficAreaProperty.getObject() != null) {
        			long id = trafficAreaImporter.insert(auxTrafficAreaProperty.getObject(), transComplexId);
        			if (id == 0)
        				System.out.println("Could not write AuxiliaryTrafficArea");
        		} else {
        			// xlink
					String href = auxTrafficAreaProperty.getHref();

					if (href != null && href.length() != 0) {
						dbImporterManager.propagateEvent(new LogMessageEvent(
								"Xlink-Verweis '" + href + "' auf AuxiliaryTrafficArea wird nicht unterstützt.",
								LogMessageEnum.ERROR
						));
					}
        		}
        	}

        	auxTrafficAreaPropertyList = null;
        }

        // TrafficArea
        List<TrafficAreaProperty> trafficAreaPropertyList = transComplex.getTrafficArea();
        if (trafficAreaPropertyList != null) {
        	for (TrafficAreaProperty trafficAreaProperty : trafficAreaPropertyList) {
        		if (trafficAreaProperty.getObject() != null) {
        			long id = trafficAreaImporter.insert(trafficAreaProperty.getObject(), transComplexId);
        			if (id == 0)
        				System.out.println("Could not write TrafficArea");
        		} else {
        			// xlink
					String href = trafficAreaProperty.getHref();

					if (href != null && href.length() != 0) {
						dbImporterManager.propagateEvent(new LogMessageEvent(
								"Xlink-Verweis '" + href + "' auf TrafficArea wird nicht unterstützt.",
								LogMessageEnum.ERROR
						));
					}
        		}
        	}

        	trafficAreaPropertyList = null;
        }

		return true;
	}

	@Override
	public void executeBatch() throws SQLException {
		psTransComplex.executeBatch();
		batchCounter = 0;
	}

	@Override
	public DBImporterEnum getDBImporterType() {
		return DBImporterEnum.TRANSPORTATION_COMPLEX;
	}

}
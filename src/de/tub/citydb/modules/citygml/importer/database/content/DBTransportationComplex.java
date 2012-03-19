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

import oracle.spatial.geometry.JGeometry;
import oracle.spatial.geometry.SyncJGeometry;
import oracle.sql.STRUCT;

import org.citygml4j.impl.gml.geometry.complexes.GeometricComplexImpl;
import org.citygml4j.impl.gml.geometry.primitives.GeometricPrimitivePropertyImpl;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.transportation.AuxiliaryTrafficArea;
import org.citygml4j.model.citygml.transportation.AuxiliaryTrafficAreaProperty;
import org.citygml4j.model.citygml.transportation.TrafficArea;
import org.citygml4j.model.citygml.transportation.TrafficAreaProperty;
import org.citygml4j.model.citygml.transportation.TransportationComplex;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;
import org.citygml4j.model.gml.geometry.complexes.GeometricComplex;
import org.citygml4j.model.gml.geometry.complexes.GeometricComplexProperty;
import org.citygml4j.model.gml.geometry.primitives.AbstractGeometricPrimitive;
import org.citygml4j.model.gml.geometry.primitives.GeometricPrimitiveProperty;

import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.database.TableEnum;
import de.tub.citydb.database.TypeAttributeValueEnum;
import de.tub.citydb.log.Logger;
import de.tub.citydb.modules.citygml.common.database.xlink.DBXlinkBasic;
import de.tub.citydb.util.Util;

public class DBTransportationComplex implements DBImporter {
	private final Logger LOG = Logger.getInstance();
	
	private final Connection batchConn;
	private final DBImporterManager dbImporterManager;

	private PreparedStatement psTransComplex;
	private DBCityObject cityObjectImporter;
	private DBSurfaceGeometry surfaceGeometryImporter;
	private DBTrafficArea trafficAreaImporter;
	private DBSdoGeometry sdoGeometry;
	
	private int batchCounter;

	public DBTransportationComplex(Connection batchConn, DBImporterManager dbImporterManager) throws SQLException {
		this.batchConn = batchConn;
		this.dbImporterManager = dbImporterManager;

		init();
	}

	private void init() throws SQLException {		
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
		String origGmlId = transComplex.getId();
		
		// CityObject
		long cityObjectId = cityObjectImporter.insert(transComplex, transComplexId, true);
		if (cityObjectId == 0)
			return false;

		// TransportationComplex
		// ID
		psTransComplex.setLong(1, transComplexId);

		// gml:name
		if (transComplex.isSetName()) {
			String[] dbGmlName = Util.gmlName2dbString(transComplex);

			psTransComplex.setString(2, dbGmlName[0]);
			psTransComplex.setString(3, dbGmlName[1]);
		} else {
			psTransComplex.setNull(2, Types.VARCHAR);
			psTransComplex.setNull(3, Types.VARCHAR);
		}

		// gml:description
		if (transComplex.isSetDescription()) {
			String description = transComplex.getDescription().getValue();

			if (description != null)
				description = description.trim();

			psTransComplex.setString(4, description);
		} else {
			psTransComplex.setNull(4, Types.VARCHAR);
		}

		// citygml:function
		if (transComplex.isSetFunction()) {
			psTransComplex.setString(5, Util.collection2string(transComplex.getFunction(), " "));
		} else {
			psTransComplex.setNull(5, Types.VARCHAR);
		}

		// citygml:usage
		if (transComplex.isSetUsage()) {
			psTransComplex.setString(6, Util.collection2string(transComplex.getUsage(), " "));
		} else {
			psTransComplex.setNull(6, Types.VARCHAR);
		}

		// TYPE
		psTransComplex.setString(7, TypeAttributeValueEnum.fromCityGMLClass(transComplex.getCityGMLClass()).toString());

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
    			if (multiSurfaceProperty.isSetMultiSurface()) {
    				multiSurfaceId = surfaceGeometryImporter.insert(multiSurfaceProperty.getMultiSurface(), transComplexId);
    			} else {
    				// xlink
					String href = multiSurfaceProperty.getHref();

        			if (href != null && href.length() != 0) {
        				DBXlinkBasic xlink = new DBXlinkBasic(
        						transComplexId,
        						TableEnum.TRANSPORTATION_COMPLEX,
        						href,
        						TableEnum.SURFACE_GEOMETRY
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
        if (transComplex.isSetLod0Network()) {
        	GeometricComplex aggregateComplex = new GeometricComplexImpl();
        	JGeometry multiCurveGeom = null;
        	
        	for (GeometricComplexProperty complexProperty : transComplex.getLod0Network()) {
        		// for lod0Network we just consider appropriate curve geometries
        		
        		if (complexProperty.isSetCompositeCurve()) {
        			GeometricPrimitiveProperty primitiveProperty = new GeometricPrimitivePropertyImpl();
        			primitiveProperty.setGeometricPrimitive(complexProperty.getCompositeCurve());
        			
        			aggregateComplex.addElement(primitiveProperty);
        		} 
        		
        		else if (complexProperty.getGeometricComplex() != null) {
        			GeometricComplex complex = complexProperty.getGeometricComplex();        			
        			
        			if (complex.isSetElement()) {
        				for (GeometricPrimitiveProperty primitiveProperty : complex.getElement()) {        					
        					if (primitiveProperty.isSetGeometricPrimitive()) {        						
            					AbstractGeometricPrimitive primitive = primitiveProperty.getGeometricPrimitive();

        						switch (primitive.getGMLClass()) {
        						case LINE_STRING:
        							aggregateComplex.addElement(primitiveProperty);
        							break;
        						case COMPOSITE_CURVE:
        							aggregateComplex.addElement(primitiveProperty);
        							break;
        						case ORIENTABLE_CURVE:
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
        		
        		else if (complexProperty.isSetHref()) {
        			// xlinks are not supported
        		}
        			
        		else {
        			// other geometry types are not supported for lod0Network
        		}
        	}
        	
        	if (aggregateComplex.isSetElement() && !aggregateComplex.getElement().isEmpty())      		
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
        if (transComplex.isSetAuxiliaryTrafficArea()) {
        	for (AuxiliaryTrafficAreaProperty auxTrafficAreaProperty : transComplex.getAuxiliaryTrafficArea()) {
        		AuxiliaryTrafficArea auxArea = auxTrafficAreaProperty.getAuxiliaryTrafficArea();
        		
        		if (auxArea != null) {
        			String gmlId = auxArea.getId();
        			long id = trafficAreaImporter.insert(auxArea, transComplexId);
        			
        			if (id == 0) {
						StringBuilder msg = new StringBuilder(Util.getFeatureSignature(
								transComplex.getCityGMLClass(), 
								origGmlId));
						msg.append(": Failed to write ");
						msg.append(Util.getFeatureSignature(
								CityGMLClass.AUXILIARY_TRAFFIC_AREA, 
								gmlId));
						
						LOG.error(msg.toString());
					}
        			
        			// free memory of nested feature
        			auxTrafficAreaProperty.unsetAuxiliaryTrafficArea();
        		} else {
        			// xlink
					String href = auxTrafficAreaProperty.getHref();

					if (href != null && href.length() != 0) {
						LOG.error("XLink reference '" + href + "' to AuxiliaryTrafficArea feature is not supported.");
					}
        		}
        	}
        }

        // TrafficArea
        if (transComplex.isSetTrafficArea()) {
        	for (TrafficAreaProperty trafficAreaProperty : transComplex.getTrafficArea()) {
        		TrafficArea area = trafficAreaProperty.getTrafficArea();
        		
        		if (area != null) {
        			String gmlId = area.getId();
        			long id = trafficAreaImporter.insert(area, transComplexId);
        			
        			if (id == 0) {
						StringBuilder msg = new StringBuilder(Util.getFeatureSignature(
								transComplex.getCityGMLClass(), 
								origGmlId));
						msg.append(": Failed to write ");
						msg.append(Util.getFeatureSignature(
								CityGMLClass.TRAFFIC_AREA, 
								gmlId));
						
						LOG.error(msg.toString());
					}
        			
        			// free memory of nested feature
        			trafficAreaProperty.unsetTrafficArea();
        		} else {
        			// xlink
					String href = trafficAreaProperty.getHref();

					if (href != null && href.length() != 0) {
						LOG.error("XLink reference '" + href + "' to TrafficArea feature is not supported.");
					}
        		}
        	}
        }

		return true;
	}

	@Override
	public void executeBatch() throws SQLException {
		psTransComplex.executeBatch();
		batchCounter = 0;
	}

	@Override
	public void close() throws SQLException {
		psTransComplex.close();
	}

	@Override
	public DBImporterEnum getDBImporterType() {
		return DBImporterEnum.TRANSPORTATION_COMPLEX;
	}

}

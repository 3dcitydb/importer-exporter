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
import java.sql.Types;

import org.citydb.api.geometry.GeometryObject;
import org.citydb.database.TableEnum;
import org.citydb.log.Logger;
import org.citydb.modules.citygml.common.database.xlink.DBXlinkSurfaceGeometry;
import org.citydb.util.Util;
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

public class DBTransportationComplex implements DBImporter {
	private final Logger LOG = Logger.getInstance();

	private final Connection batchConn;
	private final DBImporterManager dbImporterManager;

	private PreparedStatement psTransComplex;
	private DBCityObject cityObjectImporter;
	private DBSurfaceGeometry surfaceGeometryImporter;
	private DBTrafficArea trafficAreaImporter;
	private DBOtherGeometry otherGeometryImporter;

	private int batchCounter;
	private int nullGeometryType;
	private String nullGeometryTypeName;

	public DBTransportationComplex(Connection batchConn, DBImporterManager dbImporterManager) throws SQLException {
		this.batchConn = batchConn;
		this.dbImporterManager = dbImporterManager;

		init();
	}

	private void init() throws SQLException {
		nullGeometryType = dbImporterManager.getDatabaseAdapter().getGeometryConverter().getNullGeometryType();
		nullGeometryTypeName = dbImporterManager.getDatabaseAdapter().getGeometryConverter().getNullGeometryTypeName();

		StringBuilder stmt = new StringBuilder()
		.append("insert into TRANSPORTATION_COMPLEX (ID, OBJECTCLASS_ID, CLASS, CLASS_CODESPACE, FUNCTION, FUNCTION_CODESPACE, USAGE, USAGE_CODESPACE, ")
		.append("LOD0_NETWORK, LOD1_MULTI_SURFACE_ID, LOD2_MULTI_SURFACE_ID, LOD3_MULTI_SURFACE_ID, LOD4_MULTI_SURFACE_ID) values ")
		.append("(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
		psTransComplex = batchConn.prepareStatement(stmt.toString());

		surfaceGeometryImporter = (DBSurfaceGeometry)dbImporterManager.getDBImporter(DBImporterEnum.SURFACE_GEOMETRY);
		cityObjectImporter = (DBCityObject)dbImporterManager.getDBImporter(DBImporterEnum.CITYOBJECT);
		trafficAreaImporter = (DBTrafficArea)dbImporterManager.getDBImporter(DBImporterEnum.TRAFFIC_AREA);
		otherGeometryImporter = (DBOtherGeometry)dbImporterManager.getDBImporter(DBImporterEnum.OTHER_GEOMETRY);
	}

	public long insert(TransportationComplex transComplex) throws SQLException {
		long transComplexId = dbImporterManager.getDBId(DBSequencerEnum.CITYOBJECT_ID_SEQ);
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

		// OBJECTCLASS_ID
		psTransComplex.setInt(2, Util.cityObject2classId(transComplex.getCityGMLClass()));

		// tran:class
		if (transComplex.isSetClazz() && transComplex.getClazz().isSetValue()) {
			psTransComplex.setString(3, transComplex.getClazz().getValue());
			psTransComplex.setString(4, transComplex.getClazz().getCodeSpace());
		} else {
			psTransComplex.setNull(3, Types.VARCHAR);
			psTransComplex.setNull(4, Types.VARCHAR);
		}

		// tran:function
		if (transComplex.isSetFunction()) {
			String[] function = Util.codeList2string(transComplex.getFunction());
			psTransComplex.setString(5, function[0]);
			psTransComplex.setString(6, function[1]);
		} else {
			psTransComplex.setNull(5, Types.VARCHAR);
			psTransComplex.setNull(6, Types.VARCHAR);
		}

		// tran:usage
		if (transComplex.isSetUsage()) {
			String[] usage = Util.codeList2string(transComplex.getUsage());
			psTransComplex.setString(7, usage[0]);
			psTransComplex.setString(8, usage[1]);
		} else {
			psTransComplex.setNull(7, Types.VARCHAR);
			psTransComplex.setNull(8, Types.VARCHAR);
		}

		// Geometry
		// lod0Network
		GeometryObject multiCurve = null;

		if (transComplex.isSetLod0Network()) {
			GeometricComplex aggregate = new GeometricComplex();

			for (GeometricComplexProperty complexProperty : transComplex.getLod0Network()) {
				// for lod0Network we just consider appropriate curve geometries

				if (complexProperty.isSetCompositeCurve()) {
					GeometricPrimitiveProperty primitiveProperty = new GeometricPrimitiveProperty(complexProperty.getCompositeCurve());
					aggregate.addElement(primitiveProperty);
				} 

				else if (complexProperty.getGeometricComplex() != null) {
					GeometricComplex complex = complexProperty.getGeometricComplex();        			

					if (complex.isSetElement()) {
						for (GeometricPrimitiveProperty primitiveProperty : complex.getElement()) {        					
							if (primitiveProperty.isSetGeometricPrimitive()) {        						
								AbstractGeometricPrimitive primitive = primitiveProperty.getGeometricPrimitive();

								switch (primitive.getGMLClass()) {
								case LINE_STRING:
								case COMPOSITE_CURVE:
								case ORIENTABLE_CURVE:
								case CURVE:
									aggregate.addElement(primitiveProperty);
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

				// we do not support XLinks or further geometry types so far
			}

			// free memory of geometry object
			transComplex.unsetLod0Network();

			if (aggregate.isSetElement() && !aggregate.getElement().isEmpty())   		
				multiCurve = otherGeometryImporter.getCurveGeometry(aggregate);
		}

		if (multiCurve != null)
			psTransComplex.setObject(9, dbImporterManager.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(multiCurve, batchConn));
		else
			psTransComplex.setNull(9, nullGeometryType, nullGeometryTypeName);

		// lodXMultiSurface
		for (int i = 0; i < 4; i++) {
			MultiSurfaceProperty multiSurfaceProperty = null;
			long multiGeometryId = 0;

			switch (i) {
			case 0:
				multiSurfaceProperty = transComplex.getLod1MultiSurface();
				break;
			case 1:
				multiSurfaceProperty = transComplex.getLod2MultiSurface();
				break;
			case 2:
				multiSurfaceProperty = transComplex.getLod3MultiSurface();
				break;
			case 3:
				multiSurfaceProperty = transComplex.getLod4MultiSurface();
				break;
			}

			if (multiSurfaceProperty != null) {
				if (multiSurfaceProperty.isSetMultiSurface()) {
					multiGeometryId = surfaceGeometryImporter.insert(multiSurfaceProperty.getMultiSurface(), transComplexId);
					multiSurfaceProperty.unsetMultiSurface();
				} else {
					// xlink
					String href = multiSurfaceProperty.getHref();

					if (href != null && href.length() != 0) {
						dbImporterManager.propagateXlink(new DBXlinkSurfaceGeometry(
								href, 
								transComplexId, 
								TableEnum.TRANSPORTATION_COMPLEX, 
								"LOD" + (i + 1) + "_MULTI_SURFACE_ID"));
					}
				}
			}

			if (multiGeometryId != 0)
				psTransComplex.setLong(10 + i, multiGeometryId);
			else
				psTransComplex.setNull(10 + i, Types.NULL);
		}		

		psTransComplex.addBatch();
		if (++batchCounter == dbImporterManager.getDatabaseAdapter().getMaxBatchSize())
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
						LOG.error("XLink reference '" + href + "' to " + CityGMLClass.AUXILIARY_TRAFFIC_AREA + " feature is not supported.");
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
						LOG.error("XLink reference '" + href + "' to " + CityGMLClass.TRAFFIC_AREA + " feature is not supported.");
					}
				}
			}
		}

		// insert local appearance
		cityObjectImporter.insertAppearance(transComplex, transComplexId);

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

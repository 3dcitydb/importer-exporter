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
import java.sql.SQLException;
import java.util.HashMap;

import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.gml.GMLClass;

import de.tub.citydb.api.concurrent.WorkerPool;
import de.tub.citydb.api.event.Event;
import de.tub.citydb.api.event.EventDispatcher;
import de.tub.citydb.config.Config;
import de.tub.citydb.modules.citygml.common.database.gmlid.DBGmlIdLookupServerManager;
import de.tub.citydb.modules.citygml.common.database.gmlid.GmlIdEntry;
import de.tub.citydb.modules.citygml.common.database.gmlid.GmlIdLookupServer;
import de.tub.citydb.modules.citygml.common.database.xlink.DBXlink;
import de.tub.citydb.modules.citygml.importer.util.AffineTransformer;

public class DBImporterManager {
	private final Connection batchConn;
	private final Connection commitConn;
	private final WorkerPool<DBXlink> tmpXlinkPool;
	private final DBGmlIdLookupServerManager lookupServerManager;
	private final EventDispatcher eventDipatcher;
	private final Config config;

	private final HashMap<DBImporterEnum, DBImporter> dbImporterMap;
	private final HashMap<CityGMLClass, Long> featureCounterMap;
	private final HashMap<GMLClass, Long> geometryCounterMap;
	private final DBSequencer dbSequencer;
	private AffineTransformer affineTransformer;

	public DBImporterManager(Connection batchConn,
			Connection commitConn,
			Config config,
			WorkerPool<DBXlink> tmpXlinkPool,
			DBGmlIdLookupServerManager lookupServerManager,
			EventDispatcher eventDipatcher) throws SQLException {
		this.batchConn = batchConn;
		this.commitConn = commitConn;
		this.config = config;
		this.lookupServerManager = lookupServerManager;
		this.tmpXlinkPool = tmpXlinkPool;
		this.eventDipatcher = eventDipatcher;

		dbImporterMap = new HashMap<DBImporterEnum, DBImporter>();
		featureCounterMap = new HashMap<CityGMLClass, Long>();
		geometryCounterMap = new HashMap<GMLClass, Long>();
		dbSequencer = new DBSequencer(batchConn);
		
		if (config.getProject().getImporter().getAffineTransformation().isSetUseAffineTransformation())
			affineTransformer = config.getInternal().getAffineTransformer();
	}

	public DBImporter getDBImporter(DBImporterEnum dbImporterType) throws SQLException {
		DBImporter dbImporter = dbImporterMap.get(dbImporterType);

		if (dbImporter == null) {
			// initialize DBImporter
			switch (dbImporterType) {
			case SURFACE_GEOMETRY:
				dbImporter = new DBSurfaceGeometry(batchConn, config, this);
				break;
			case IMPLICIT_GEOMETRY:
				dbImporter = new DBImplicitGeometry(batchConn, commitConn, config, this);
				break;
			case CITYOBJECT:
				dbImporter = new DBCityObject(batchConn, config, this);
				break;
			case CITYOBJECT_GENERICATTRIB:
				dbImporter = new DBCityObjectGenericAttrib(batchConn, this);
				break;
			case EXTERNAL_REFERENCE:
				dbImporter = new DBExternalReference(batchConn, this);
				break;
			case BUILDING:
				dbImporter = new DBBuilding(batchConn, this);
				break;
			case ROOM:
				dbImporter = new DBRoom(batchConn, this);
				break;
			case BUILDING_FURNITURE:
				dbImporter = new DBBuildingFurniture(batchConn, config, this);
				break;
			case BUILDING_INSTALLATION:
				dbImporter = new DBBuildingInstallation(batchConn, this);
				break;
			case THEMATIC_SURFACE:
				dbImporter = new DBThematicSurface(batchConn, this);
				break;
			case OPENING:
				dbImporter = new DBOpening(batchConn, this);
				break;
			case OPENING_TO_THEM_SURFACE:
				dbImporter = new DBOpeningToThemSurface(batchConn, this);
				break;
			case ADDRESS:
				dbImporter = new DBAddress(batchConn, this);
				break;
			case ADDRESS_TO_BUILDING:
				dbImporter = new DBAddressToBuilding(batchConn, this);
				break;
			case CITY_FURNITURE:
				dbImporter = new DBCityFurniture(batchConn, config, this);
				break;
			case LAND_USE:
				dbImporter = new DBLandUse(batchConn, this);
				break;
			case APPEARANCE:
				dbImporter = new DBAppearance(batchConn, config, this);
				break;
			case SURFACE_DATA:
				dbImporter = new DBSurfaceData(batchConn, config, this);
				break;
			case APPEAR_TO_SURFACE_DATA:
				dbImporter = new DBAppearToSurfaceData(batchConn, this);
				break;
			case DEPRECATED_MATERIAL_MODEL:
				dbImporter = new DBDeprecatedMaterialModel(config, this);
				break;
			case WATERBODY:
				dbImporter = new DBWaterBody(batchConn, this);
				break;
			case WATERBOUNDARY_SURFACE:
				dbImporter = new DBWaterBoundarySurface(batchConn, this);
				break;
			case WATERBOD_TO_WATERBND_SRF:
				dbImporter = new DBWaterBodToWaterBndSrf(batchConn, this);
				break;
			case PLANT_COVER:
				dbImporter = new DBPlantCover(batchConn, this);
				break;
			case SOLITARY_VEGETAT_OBJECT:
				dbImporter = new DBSolitaryVegetatObject(batchConn, config, this);
				break;
			case TRANSPORTATION_COMPLEX:
				dbImporter = new DBTransportationComplex(batchConn, this);
				break;
			case TRAFFIC_AREA:
				dbImporter = new DBTrafficArea(batchConn, this);
				break;
			case RELIEF_FEATURE:
				dbImporter = new DBReliefFeature(batchConn, this);
				break;
			case RELIEF_COMPONENT:
				dbImporter = new DBReliefComponent(batchConn, this);
				break;
			case RELIEF_FEAT_TO_REL_COMP:
				dbImporter = new DBReliefFeatToRelComp(batchConn, this);
				break;
			case GENERIC_CITYOBJECT:
				dbImporter = new DBGenericCityObject(batchConn, config, this);
				break;
			case CITYOBJECTGROUP:
				dbImporter = new DBCityObjectGroup(batchConn, this);
				break;
			case SDO_GEOMETRY:
				dbImporter = new DBSdoGeometry(config, this);
				break;
			}

			if (dbImporter != null)
				dbImporterMap.put(dbImporterType, dbImporter);
		}

		return dbImporter;
	}

	public long getDBId(DBSequencerEnum sequence) throws SQLException {
		return dbSequencer.getDBId(sequence);
	}

	public void putGmlId(String gmlId, long id, long rootId, boolean reverse, String mapping, CityGMLClass type) {
		GmlIdLookupServer lookupServer = lookupServerManager.getLookupServer(type);
		if (lookupServer != null)
			lookupServer.put(gmlId, id, rootId, reverse, mapping, type);
	}

	public void putGmlId(String gmlId, long id, CityGMLClass type) {
		putGmlId(gmlId, id, -1, false, null, type);
	}
	
	public long getDBId(String gmlId, CityGMLClass type) {
		GmlIdLookupServer lookupServer = lookupServerManager.getLookupServer(type);

		if (lookupServer != null) {
			GmlIdEntry entry = lookupServer.get(gmlId);
			if (entry != null && entry.getId() > 0)
				return entry.getId();
		}
		
		return 0;
	}

	public void propagateXlink(DBXlink xlink) {
		tmpXlinkPool.addWork(xlink);
	}

	public void propagateEvent(Event event) {
		eventDipatcher.triggerEvent(event);
	}

	public void updateFeatureCounter(CityGMLClass featureType) {
		Long counter = featureCounterMap.get(featureType);
		if (counter == null)
			featureCounterMap.put(featureType, new Long(1));
		else
			featureCounterMap.put(featureType, counter + 1);
	}

	public AffineTransformer getAffineTransformer() {
		return affineTransformer;
	}
	
	public HashMap<CityGMLClass, Long> getFeatureCounter() {
		return featureCounterMap;
	}

	public void updateGeometryCounter(GMLClass geometryType) {
		Long counter = geometryCounterMap.get(geometryType);
		if (counter == null)
			geometryCounterMap.put(geometryType, new Long(1));
		else
			geometryCounterMap.put(geometryType, counter + 1);
	}

	public HashMap<GMLClass, Long> getGeometryCounter() {
		return geometryCounterMap;
	}

	public void executeBatch(DBImporterEnum type) throws SQLException {
		for (DBImporterEnum key : DBImporterEnum.getExecutionPlan(type)) {
			DBImporter importer = dbImporterMap.get(key);
			if (importer != null)
				importer.executeBatch();
		}
	}
	
	public void executeBatch() throws SQLException {
		for (DBImporterEnum key : DBImporterEnum.EXECUTION_PLAN) {
			DBImporter importer = dbImporterMap.get(key);
			if (importer != null)
				importer.executeBatch();
		}
	}
	
	public void close() throws SQLException {
		dbSequencer.close();

		for (DBImporter importer : dbImporterMap.values())
			importer.close();
	}
}

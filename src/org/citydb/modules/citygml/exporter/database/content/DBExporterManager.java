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
package org.citydb.modules.citygml.exporter.database.content;

import java.io.Reader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.HashMap;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.citydb.api.concurrent.WorkerPool;
import org.citydb.api.event.Event;
import org.citydb.api.event.EventDispatcher;
import org.citydb.config.Config;
import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.modules.citygml.common.database.cache.CacheTable;
import org.citydb.modules.citygml.common.database.cache.CacheTableManager;
import org.citydb.modules.citygml.common.database.cache.model.CacheTableModelEnum;
import org.citydb.modules.citygml.common.database.uid.UIDCache;
import org.citydb.modules.citygml.common.database.uid.UIDCacheManager;
import org.citydb.modules.citygml.common.database.xlink.DBXlink;
import org.citydb.modules.citygml.common.xal.AddressExportFactory;
import org.citydb.modules.citygml.exporter.util.FeatureProcessException;
import org.citydb.modules.citygml.exporter.util.FeatureProcessor;
import org.citydb.modules.common.filter.ExportFilter;
import org.citygml4j.builder.jaxb.JAXBBuilder;
import org.citygml4j.builder.jaxb.unmarshal.JAXBUnmarshaller;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.gml.GMLClass;
import org.citygml4j.model.gml.feature.AbstractFeature;
import org.citygml4j.xml.io.reader.MissingADESchemaException;
import org.xml.sax.SAXException;

public class DBExporterManager {
	private final Connection connection;
	private final AbstractDatabaseAdapter databaseAdapter;
	private final JAXBBuilder jaxbBuilder;
	private final FeatureProcessor featureProcessor;
	private final WorkerPool<DBXlink> xlinkExporterPool;
	private final UIDCacheManager uidCacheManager;
	private final CacheTableManager cacheTableManager;
	private final ExportFilter exportFilter;
	private final Config config;
	private final EventDispatcher eventDispatcher;

	private final JAXBUnmarshaller jaxbUnmarshaller;
	private AddressExportFactory addressExportFactory;
	private EnumMap<DBExporterEnum, DBExporter> dbExporterMap;
	private HashMap<CityGMLClass, Long> featureCounterMap;
	private HashMap<GMLClass, Long> geometryCounterMap;

	public DBExporterManager(Connection connection,
			AbstractDatabaseAdapter databaseAdapter,
			JAXBBuilder jaxbBuilder,
			FeatureProcessor featureProcessor,
			WorkerPool<DBXlink> xlinkExporterPool,
			UIDCacheManager uidCacheManager,
			CacheTableManager cacheTableManager,
			ExportFilter exportFilter,
			Config config,
			EventDispatcher eventDispatcher) throws SAXException {
		this.connection = connection;
		this.databaseAdapter = databaseAdapter;
		this.jaxbBuilder = jaxbBuilder;
		this.featureProcessor = featureProcessor;
		this.xlinkExporterPool = xlinkExporterPool;
		this.uidCacheManager = uidCacheManager;
		this.cacheTableManager = cacheTableManager;
		this.exportFilter = exportFilter;
		this.config = config;
		this.eventDispatcher = eventDispatcher;

		dbExporterMap = new EnumMap<DBExporterEnum, DBExporter>(DBExporterEnum.class);
		featureCounterMap = new HashMap<CityGMLClass, Long>();
		geometryCounterMap = new HashMap<GMLClass, Long>();

		jaxbUnmarshaller = jaxbBuilder.createJAXBUnmarshaller();
		jaxbUnmarshaller.setThrowMissingADESchema(false);
		jaxbUnmarshaller.setParseSchema(false);
	}

	public DBExporter getDBExporter(DBExporterEnum dbExporterType) throws SQLException {
		DBExporter dbExporter = dbExporterMap.get(dbExporterType);
		CacheTable globalAppTempTable = null;

		if (dbExporter == null) {
			switch (dbExporterType) {
			case SURFACE_GEOMETRY:
				if (config.getInternal().isExportGlobalAppearances())
					globalAppTempTable = cacheTableManager.getCacheTable(CacheTableModelEnum.GLOBAL_APPEARANCE);

				dbExporter = new DBSurfaceGeometry(connection, globalAppTempTable, config, this);
				break;
			case IMPLICIT_GEOMETRY:
				dbExporter = new DBImplicitGeometry(connection, this);
				break;
			case CITYOBJECT:
				dbExporter = new DBCityObject(connection, exportFilter, config, this);
				break;
			case CITYOBJECT_GENERICATTRIB:
				dbExporter = new DBCityObjectGenericAttrib(connection);
				break;
			case BUILDING:
				dbExporter = new DBBuilding(connection, exportFilter, config, this);
				break;
			case ROOM:
				dbExporter = new DBRoom(connection, this);
				break;
			case BUILDING_FURNITURE:
				dbExporter = new DBBuildingFurniture(connection, config, this);
				break;
			case BUILDING_INSTALLATION:
				dbExporter = new DBBuildingInstallation(connection, config, this);
				break;
			case THEMATIC_SURFACE:
				dbExporter = new DBThematicSurface(connection, config, this);
				break;
			case BRIDGE:
				dbExporter = new DBBridge(connection, exportFilter, config, this);
				break;
			case BRIDGE_INSTALLATION:
				dbExporter = new DBBridgeInstallation(connection, config, this);
				break;
			case BRIDGE_CONSTR_ELEMENT:
				dbExporter = new DBBridgeConstrElement(connection, config, this);
				break;
			case BRIDGE_THEMATIC_SURFACE:
				dbExporter = new DBBridgeThematicSurface(connection, config, this);
				break;
			case BRIDGE_ROOM:
				dbExporter = new DBBridgeRoom(connection, this);
				break;
			case BRIDGE_FURNITURE:
				dbExporter = new DBBridgeFurniture(connection, config, this);
				break;
			case TUNNEL:
				dbExporter = new DBTunnel(connection, exportFilter, config, this);
				break;
			case TUNNEL_THEMATIC_SURFACE:
				dbExporter = new DBTunnelThematicSurface(connection, config, this);
				break;
			case TUNNEL_INSTALLATION:
				dbExporter = new DBTunnelInstallation(connection, config, this);
				break;
			case TUNNEL_HOLLOW_SPACE:
				dbExporter = new DBTunnelHollowSpace(connection, this);
				break;
			case TUNNEL_FURNITURE:
				dbExporter = new DBTunnelFurniture(connection, config, this);
				break;
			case CITY_FURNITURE:
				dbExporter = new DBCityFurniture(connection, exportFilter, config, this);
				break;
			case LAND_USE:
				dbExporter = new DBLandUse(connection, exportFilter, config, this);
				break;
			case WATERBODY:
				dbExporter = new DBWaterBody(connection, exportFilter, config, this);
				break;
			case PLANT_COVER:
				dbExporter = new DBPlantCover(connection, exportFilter, config, this);
				break;
			case SOLITARY_VEGETAT_OBJECT:
				dbExporter = new DBSolitaryVegetatObject(connection, exportFilter, config, this);
				break;
			case TRANSPORTATION_COMPLEX:
				dbExporter = new DBTransportationComplex(connection, exportFilter, config, this);
				break;
			case RELIEF_FEATURE:
				dbExporter = new DBReliefFeature(connection, exportFilter, config, this);
				break;
			case LOCAL_APPEARANCE:
			case GLOBAL_APPEARANCE:
				dbExporter = new DBAppearance(dbExporterType, connection, config, this);
				break;
			case LOCAL_APPEARANCE_TEXTUREPARAM:
			case GLOBAL_APPEARANCE_TEXTUREPARAM:
				if (config.getInternal().isExportGlobalAppearances())
					globalAppTempTable = cacheTableManager.getCacheTable(CacheTableModelEnum.GLOBAL_APPEARANCE);

				dbExporter = new DBTextureParam(dbExporterType, connection, globalAppTempTable, this);
				break;
			case GENERIC_CITYOBJECT:
				dbExporter = new DBGenericCityObject(connection, exportFilter, config, this);
				break;
			case CITYOBJECTGROUP:
				dbExporter = new DBCityObjectGroup(connection, exportFilter, config, this);
				break;
			case GENERALIZATION:
				dbExporter = new DBGeneralization(connection, exportFilter, config, this);
				break;
			case OTHER_GEOMETRY:
				dbExporter = new DBOtherGeometry(config);
				break;
			}

			if (dbExporter != null)
				dbExporterMap.put(dbExporterType, dbExporter);
		}

		return dbExporter;
	}

	public boolean lookupAndPutGmlId(String gmlId, long id, CityGMLClass type) {
		UIDCache cache = uidCacheManager.getCache(type);

		if (cache != null)
			return cache.lookupAndPut(gmlId, id, type);
		else
			return false;
	}

	public void putUID(String gmlId, long id, long rootId, boolean reverse, String mapping, CityGMLClass type) {
		UIDCache cache = uidCacheManager.getCache(type);

		if (cache != null)
			cache.put(gmlId, id, rootId, reverse, mapping, type);
	}

	public void putUID(String gmlId, long id, CityGMLClass type) {
		putUID(gmlId, id, -1, false, null, type);
	}

	public String getUID(long id, CityGMLClass type) {
		UIDCache cache = uidCacheManager.getCache(type);

		if (cache != null)
			return cache.get(id, type);
		else
			return null;
	}

	public void propagateEvent(Event event) {
		eventDispatcher.triggerEvent(event);
	}

	public void propagateXlink(DBXlink xlink) {
		xlinkExporterPool.addWork(xlink);
	}

	public AddressExportFactory getAddressExportFactory() {
		if (addressExportFactory == null)
			addressExportFactory = new AddressExportFactory(config);

		return addressExportFactory;
	}

	public void updateFeatureCounter(CityGMLClass featureType) {
		Long counter = featureCounterMap.get(featureType);
		if (counter == null)
			featureCounterMap.put(featureType, new Long(1));
		else
			featureCounterMap.put(featureType, counter + 1);
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
	
	public AbstractDatabaseAdapter getDatabaseAdapter() {
		return databaseAdapter;
	}

	public void close() throws SQLException {
		for (DBExporter exporter : dbExporterMap.values())
			exporter.close();
	}

	public void processFeature(AbstractFeature abstractFeature) throws FeatureProcessException {
		featureProcessor.process(abstractFeature);		
	}

	public Object unmarshal(Reader reader) {
		Object object = null;

		try {
			Unmarshaller unmarshaller = jaxbBuilder.getJAXBContext().createUnmarshaller();
			object = unmarshaller.unmarshal(reader);
			if (object != null)
				object = jaxbUnmarshaller.unmarshal(object);
		} catch (JAXBException e) {
			object = null;
		} catch (MissingADESchemaException e) {
			object = null;
		}

		return object;
	}
}

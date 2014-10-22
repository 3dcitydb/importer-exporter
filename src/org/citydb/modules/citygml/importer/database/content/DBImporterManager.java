/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2013
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
package org.citydb.modules.citygml.importer.database.content;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.citydb.api.concurrent.WorkerPool;
import org.citydb.api.event.Event;
import org.citydb.api.event.EventDispatcher;
import org.citydb.config.Config;
import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.modules.citygml.common.database.uid.UIDCache;
import org.citydb.modules.citygml.common.database.uid.UIDCacheEntry;
import org.citydb.modules.citygml.common.database.uid.UIDCacheManager;
import org.citydb.modules.citygml.common.database.xlink.DBXlink;
import org.citydb.modules.citygml.importer.util.AffineTransformer;
import org.citydb.modules.citygml.importer.util.ImportLogger.ImportLogEntry;
import org.citydb.modules.citygml.importer.util.LocalTextureCoordinatesResolver;
import org.citygml4j.builder.jaxb.JAXBBuilder;
import org.citygml4j.builder.jaxb.marshal.JAXBMarshaller;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.gml.GMLClass;
import org.citygml4j.model.module.ModuleType;
import org.citygml4j.model.module.citygml.CityGMLVersion;
import org.citygml4j.util.xml.SAXWriter;
import org.citygml4j.xml.CityGMLNamespaceContext;
import org.xml.sax.SAXException;

public class DBImporterManager {
	private final Connection batchConn;
	private final AbstractDatabaseAdapter databaseAdapter;
	private final JAXBBuilder jaxbBuilder;
	private final WorkerPool<DBXlink> tmpXlinkPool;
	private final UIDCacheManager uidCacheManager;
	private final EventDispatcher eventDipatcher;
	private final Config config;

	private final HashMap<DBImporterEnum, DBImporter> dbImporterMap;
	private final HashMap<CityGMLClass, Long> featureCounterMap;
	private final HashMap<GMLClass, Long> geometryCounterMap;
	private final List<ImportLogEntry> importedFeatures;
	private final DBSequencer dbSequencer;

	private AffineTransformer affineTransformer;
	private LocalTextureCoordinatesResolver localTexCoordResolver;
	private CityGMLVersion cityGMLVersion;
	private JAXBMarshaller jaxbMarshaller;
	private SAXWriter saxWriter;
	private boolean isLogImportedFeatures;

	public DBImporterManager(Connection batchConn,
			AbstractDatabaseAdapter databaseAdapter,
			JAXBBuilder jaxbBuilder,
			Config config,
			WorkerPool<DBXlink> tmpXlinkPool,
			UIDCacheManager uidCacheManager,
			EventDispatcher eventDipatcher) throws SQLException {
		this.batchConn = batchConn;
		this.databaseAdapter = databaseAdapter;
		this.jaxbBuilder = jaxbBuilder;
		this.config = config;
		this.uidCacheManager = uidCacheManager;
		this.tmpXlinkPool = tmpXlinkPool;
		this.eventDipatcher = eventDipatcher;

		dbImporterMap = new HashMap<DBImporterEnum, DBImporter>();
		featureCounterMap = new HashMap<CityGMLClass, Long>();
		geometryCounterMap = new HashMap<GMLClass, Long>();
		importedFeatures = new ArrayList<ImportLogEntry>();
		dbSequencer = new DBSequencer(batchConn, databaseAdapter);

		if (config.getProject().getImporter().getAffineTransformation().isSetUseAffineTransformation())
			affineTransformer = config.getInternal().getAffineTransformer();

		if (config.getProject().getImporter().getAppearances().isSetImportAppearance())
			localTexCoordResolver = new LocalTextureCoordinatesResolver();

		if (config.getProject().getImporter().getAddress().isSetImportXAL()) {
			cityGMLVersion = CityGMLVersion.DEFAULT;
			jaxbMarshaller = jaxbBuilder.createJAXBMarshaller(cityGMLVersion);
			saxWriter = new SAXWriter();
		}
		
		isLogImportedFeatures = config.getProject().getImporter().getImportLog().isSetLogImportedFeatures();
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
				dbImporter = new DBImplicitGeometry(batchConn, this);
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
				dbImporter = new DBBuildingInstallation(batchConn, config, this);
				break;
			case THEMATIC_SURFACE:
				dbImporter = new DBThematicSurface(batchConn, this);
				break;
			case OPENING:
				dbImporter = new DBOpening(batchConn, config, this);
				break;
			case OPENING_TO_THEM_SURFACE:
				dbImporter = new DBOpeningToThemSurface(batchConn, this);
				break;
			case BRIDGE:
				dbImporter = new DBBridge(batchConn, this);
				break;
			case BRIDGE_CONSTR_ELEMENT:
				dbImporter = new DBBridgeConstrElement(batchConn, config, this);
				break;
			case BRIDGE_INSTALLATION:
				dbImporter = new DBBridgeInstallation(batchConn, config, this);
				break;
			case BRIDGE_THEMATIC_SURFACE:
				dbImporter = new DBBridgeThematicSurface(batchConn, this);
				break;
			case BRIDGE_OPENING:
				dbImporter = new DBBridgeOpening(batchConn, config, this);
				break;
			case BRIDGE_OPEN_TO_THEM_SRF:
				dbImporter = new DBBridgeOpenToThemSrf(batchConn, this);
				break;
			case BRIDGE_ROOM:
				dbImporter = new DBBridgeRoom(batchConn, this);
				break;
			case BRIDGE_FURNITURE:
				dbImporter = new DBBridgeFurniture(batchConn, config, this);
				break;
			case TUNNEL:
				dbImporter = new DBTunnel(batchConn, this);
				break;
			case TUNNEL_THEMATIC_SURFACE:
				dbImporter = new DBTunnelThematicSurface(batchConn, this);
				break;
			case TUNNEL_OPENING:
				dbImporter = new DBTunnelOpening(batchConn, config, this);
				break;
			case TUNNEL_OPEN_TO_THEM_SRF:
				dbImporter = new DBTunnelOpenToThemSrf(batchConn, this);
				break;
			case TUNNEL_INSTALLATION:
				dbImporter = new DBTunnelInstallation(batchConn, config, this);
				break;
			case TUNNEL_HOLLOW_SPACE:
				dbImporter = new DBTunnelHollowSpace(batchConn, this);
				break;
			case TUNNEL_FURNITURE:
				dbImporter = new DBTunnelFurniture(batchConn, config, this);
				break;
			case ADDRESS:
				dbImporter = new DBAddress(batchConn, config, this);
				break;
			case ADDRESS_TO_BUILDING:
				dbImporter = new DBAddressToBuilding(batchConn, this);
				break;
			case ADDRESS_TO_BRIDGE:
				dbImporter = new DBAddressToBridge(batchConn, this);
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
			case TEXTURE_PARAM:
				dbImporter = new DBTextureParam(batchConn, this);
				break;
			case TEX_IMAGE:
				dbImporter = new DBTexImage(batchConn, config, this);
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
			case OTHER_GEOMETRY:
				dbImporter = new DBOtherGeometry(config, this);
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

	public void putUID(String gmlId, long id, long rootId, boolean reverse, String mapping, CityGMLClass type) {
		UIDCache cache = uidCacheManager.getCache(type);
		if (cache != null)
			cache.put(gmlId, id, rootId, reverse, mapping, type);
	}

	public void putUID(String gmlId, long id, CityGMLClass type) {
		putUID(gmlId, id, -1, false, null, type);
	}

	public boolean lookupAndPutUID(String gmlId, long id, CityGMLClass type) {
		UIDCache cache = uidCacheManager.getCache(type);

		if (cache != null)
			return cache.lookupAndPut(gmlId, id, type);
		else
			return false;
	}

	public long getDBId(String gmlId, CityGMLClass type) {
		UIDCache cache = uidCacheManager.getCache(type);

		if (cache != null) {
			UIDCacheEntry entry = cache.get(gmlId);
			if (entry != null && entry.getId() > 0)
				return entry.getId();
		}

		return 0;
	}

	public long getDBIdFromMemory(String gmlId, CityGMLClass type) {
		UIDCache cache = uidCacheManager.getCache(type);

		if (cache != null) {
			UIDCacheEntry entry = cache.getFromMemory(gmlId);
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

	public AffineTransformer getAffineTransformer() {
		return affineTransformer;
	}

	public LocalTextureCoordinatesResolver getLocalTextureCoordinatesResolver() {
		return localTexCoordResolver;
	}
	
	public void updateFeatureCounter(CityGMLClass featureType, long id, String gmlId, boolean isTopLevel) {
		Long counter = featureCounterMap.get(featureType);
		if (counter == null)
			featureCounterMap.put(featureType, new Long(1));
		else
			featureCounterMap.put(featureType, counter + 1);
		
		if (isLogImportedFeatures && isTopLevel)
			importedFeatures.add(new ImportLogEntry(featureType, id, gmlId));
	}

	public HashMap<CityGMLClass, Long> getAndResetFeatureCounter() {
		HashMap<CityGMLClass, Long> tmp = new HashMap<CityGMLClass, Long>(featureCounterMap);
		featureCounterMap.clear();
		return tmp;
	}

	public void updateGeometryCounter(GMLClass geometryType) {
		Long counter = geometryCounterMap.get(geometryType);
		if (counter == null)
			geometryCounterMap.put(geometryType, new Long(1));
		else
			geometryCounterMap.put(geometryType, counter + 1);
	}

	public HashMap<GMLClass, Long> getAndResetGeometryCounter() {
		HashMap<GMLClass, Long> tmp = new HashMap<GMLClass, Long>(geometryCounterMap);
		geometryCounterMap.clear();
		return tmp;
	}
	
	public List<ImportLogEntry> getAndResetImportedFeatures() {
		List<ImportLogEntry> tmp = new ArrayList<ImportLogEntry>(importedFeatures);
		importedFeatures.clear();
		return tmp;
	}

	public String marshal(Object object, ModuleType... moduleTypes) {
		String result = null;

		try {
			CityGMLNamespaceContext ctx = new CityGMLNamespaceContext();
			for (ModuleType moduleType : moduleTypes)
				ctx.setPrefix(cityGMLVersion.getModule(moduleType));

			ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
			saxWriter.setOutput(out);
			saxWriter.setNamespaceContext(ctx);

			Marshaller marshaller = jaxbBuilder.getJAXBContext().createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
			JAXBElement<?> jaxbElement = jaxbMarshaller.marshalJAXBElement(object);
			if (jaxbElement != null)
				marshaller.marshal(jaxbElement, saxWriter);

			saxWriter.flush();
			result = out.toString();
			out.reset();
		} catch (JAXBException e) {
			//
		} catch (IOException e) {
			// 
		} catch (SAXException e) {
			//
		} finally {
			saxWriter.reset();
		}

		return result;
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

	public AbstractDatabaseAdapter getDatabaseAdapter() {
		return databaseAdapter;
	}

	public void close() throws SQLException {
		dbSequencer.close();

		for (DBImporter importer : dbImporterMap.values())
			importer.close();
	}
}

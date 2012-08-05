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
package de.tub.citydb.modules.citygml.exporter.database.content;

import java.io.Reader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.HashMap;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.citygml4j.builder.jaxb.JAXBBuilder;
import org.citygml4j.builder.jaxb.marshal.JAXBMarshaller;
import org.citygml4j.builder.jaxb.unmarshal.JAXBUnmarshaller;
import org.citygml4j.impl.citygml.appearance.AppearanceMemberImpl;
import org.citygml4j.impl.citygml.core.CityObjectMemberImpl;
import org.citygml4j.impl.gml.feature.FeatureMemberImpl;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.appearance.Appearance;
import org.citygml4j.model.citygml.appearance.AppearanceMember;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.citygml.core.CityObjectMember;
import org.citygml4j.model.gml.GMLClass;
import org.citygml4j.model.gml.feature.AbstractFeature;
import org.citygml4j.model.gml.feature.FeatureMember;
import org.citygml4j.model.gml.feature.FeatureProperty;
import org.citygml4j.model.module.citygml.CityGMLVersion;
import org.citygml4j.util.xml.SAXEventBuffer;
import org.citygml4j.xml.io.reader.MissingADESchemaException;
import org.citygml4j.xml.io.writer.CityGMLWriteException;
import org.xml.sax.SAXException;

import de.tub.citydb.api.concurrent.WorkerPool;
import de.tub.citydb.api.event.Event;
import de.tub.citydb.api.event.EventDispatcher;
import de.tub.citydb.config.Config;
import de.tub.citydb.modules.citygml.common.database.cache.CacheManager;
import de.tub.citydb.modules.citygml.common.database.cache.TemporaryCacheTable;
import de.tub.citydb.modules.citygml.common.database.cache.model.CacheTableModelEnum;
import de.tub.citydb.modules.citygml.common.database.gmlid.DBGmlIdLookupServerManager;
import de.tub.citydb.modules.citygml.common.database.gmlid.GmlIdLookupServer;
import de.tub.citydb.modules.citygml.common.database.xlink.DBXlink;
import de.tub.citydb.modules.citygml.common.xal.AddressExportFactory;
import de.tub.citydb.modules.common.filter.ExportFilter;

public class DBExporterManager {
	private final Connection connection;
	private final JAXBBuilder jaxbBuilder;
	private final WorkerPool<SAXEventBuffer> ioWriterPool;
	private final WorkerPool<DBXlink> xlinkExporterPool;
	private final DBGmlIdLookupServerManager lookupServerManager;
	private final CacheManager cacheManager;
	private final ExportFilter exportFilter;
	private final Config config;
	private final EventDispatcher eventDispatcher;

	private final JAXBMarshaller jaxbMarshaller;
	private final JAXBUnmarshaller jaxbUnmarshaller;
	private AddressExportFactory addressExportFactory;
	private EnumMap<DBExporterEnum, DBExporter> dbExporterMap;
	private HashMap<CityGMLClass, Long> featureCounterMap;
	private HashMap<GMLClass, Long> geometryCounterMap;

	public DBExporterManager(Connection connection,
			JAXBBuilder jaxbBuilder,
			WorkerPool<SAXEventBuffer> ioWriterPool,
			WorkerPool<DBXlink> xlinkExporterPool,
			DBGmlIdLookupServerManager lookupServerManager,
			CacheManager cacheManager,
			ExportFilter exportFilter,
			Config config,
			EventDispatcher eventDispatcher) throws SAXException {
		this.connection = connection;
		this.jaxbBuilder = jaxbBuilder;
		this.ioWriterPool = ioWriterPool;
		this.xlinkExporterPool = xlinkExporterPool;
		this.lookupServerManager = lookupServerManager;
		this.cacheManager = cacheManager;
		this.exportFilter = exportFilter;
		this.config = config;
		this.eventDispatcher = eventDispatcher;

		dbExporterMap = new EnumMap<DBExporterEnum, DBExporter>(DBExporterEnum.class);
		featureCounterMap = new HashMap<CityGMLClass, Long>();
		geometryCounterMap = new HashMap<GMLClass, Long>();

		CityGMLVersion version = config.getProject().getExporter().getCityGMLVersion().toCityGMLVersion();
		jaxbMarshaller = jaxbBuilder.createJAXBMarshaller(version);
		jaxbUnmarshaller = jaxbBuilder.createJAXBUnmarshaller();
		jaxbUnmarshaller.setThrowMissingADESchema(false);
		jaxbUnmarshaller.setParseSchema(false);
	}

	public DBExporter getDBExporter(DBExporterEnum dbExporterType) throws SQLException {
		DBExporter dbExporter = dbExporterMap.get(dbExporterType);
		TemporaryCacheTable globalAppTempTable = null;

		if (dbExporter == null) {
			switch (dbExporterType) {
			case SURFACE_GEOMETRY:
				if (config.getInternal().isExportGlobalAppearances())
					globalAppTempTable = (TemporaryCacheTable)cacheManager.getCacheTable(CacheTableModelEnum.GLOBAL_APPEARANCE);

				dbExporter = new DBSurfaceGeometry(connection, globalAppTempTable, config, this);
				break;
			case IMPLICIT_GEOMETRY:
				dbExporter = new DBImplicitGeometry(connection, config, this);
				break;
			case CITYOBJECT:
				dbExporter = new DBCityObject(connection, exportFilter, config, this);
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
				dbExporter = new DBBuildingInstallation(connection, this);
				break;
			case THEMATIC_SURFACE:
				dbExporter = new DBThematicSurface(connection, config, this);
				break;
			case CITY_FURNITURE:
				dbExporter = new DBCityFurniture(connection, exportFilter, config, this);
				break;
			case LAND_USE:
				dbExporter = new DBLandUse(connection, exportFilter, this);
				break;
			case WATERBODY:
				dbExporter = new DBWaterBody(connection, exportFilter, config, this);
				break;
			case PLANT_COVER:
				dbExporter = new DBPlantCover(connection, exportFilter, this);
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
			case APPEARANCE:
				dbExporter = new DBAppearance(connection, config, this);
				break;
			case GLOBAL_APPEARANCE:
				globalAppTempTable = (TemporaryCacheTable)cacheManager.getCacheTable(CacheTableModelEnum.GLOBAL_APPEARANCE);
				if (globalAppTempTable != null)
					dbExporter = new DBGlobalAppearance(globalAppTempTable, config, this);
				break;
			case TEXTUREPARAM:
				dbExporter = new DBTextureParam(connection);
				break;
			case GENERIC_CITYOBJECT:
				dbExporter = new DBGenericCityObject(connection, exportFilter, config, this);
				break;
			case CITYOBJECTGROUP:
				dbExporter = new DBCityObjectGroup(connection, config, this);
				break;
			case GENERALIZATION:
				dbExporter = new DBGeneralization(connection, exportFilter, config);
				break;
			case SDO_GEOMETRY:
				dbExporter = new DBSdoGeometry(config);
				break;
			}

			if (dbExporter != null)
				dbExporterMap.put(dbExporterType, dbExporter);
		}

		return dbExporter;
	}

	public boolean lookupAndPutGmlId(String gmlId, long id, CityGMLClass type) {
		GmlIdLookupServer lookupServer = lookupServerManager.getLookupServer(type);

		if (lookupServer != null)
			return lookupServer.lookupAndPut(gmlId, id, type);
		else
			return false;
	}

	public void putGmlId(String gmlId, long id, long rootId, boolean reverse, String mapping, CityGMLClass type) {
		GmlIdLookupServer lookupServer = lookupServerManager.getLookupServer(type);

		if (lookupServer != null)
			lookupServer.put(gmlId, id, rootId, reverse, mapping, type);
	}

	public void putGmlId(String gmlId, long id, CityGMLClass type) {
		putGmlId(gmlId, id, -1, false, null, type);
	}

	public String getGmlId(long id, CityGMLClass type) {
		GmlIdLookupServer lookupServer = lookupServerManager.getLookupServer(type);

		if (lookupServer != null)
			return lookupServer.get(id, type);
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

	public void close() throws SQLException {
		for (DBExporter exporter : dbExporterMap.values())
			exporter.close();
	}

	public void print(AbstractFeature abstractFeature) throws CityGMLWriteException {
		FeatureProperty<? extends AbstractFeature> member = null;

		// wrap feature with a feature property element
		if (abstractFeature instanceof AbstractCityObject) {
			member = new CityObjectMemberImpl();
			((CityObjectMember)member).setCityObject((AbstractCityObject)abstractFeature);
		} 

		else if (abstractFeature instanceof Appearance) {
			member = new AppearanceMemberImpl();
			((AppearanceMember)member).setAppearance((Appearance)abstractFeature);
		} 

		else {
			member = new FeatureMemberImpl();
			((FeatureMember)member).setFeature(abstractFeature);
		}

		if (member != null) {
			try {
				SAXEventBuffer buffer = new SAXEventBuffer();
				Marshaller marshaller = jaxbBuilder.getJAXBContext().createMarshaller();
				marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);

				JAXBElement<?> jaxbElement = jaxbMarshaller.marshalJAXBElement(member);
				if (jaxbElement != null)
					marshaller.marshal(jaxbElement, buffer);

				if (!buffer.isEmpty())
					ioWriterPool.addWork(buffer);
			} catch (JAXBException e) {
				throw new CityGMLWriteException("Caused by: ", e);
			}
		}		
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

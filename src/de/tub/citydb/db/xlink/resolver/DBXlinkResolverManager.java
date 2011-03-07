package de.tub.citydb.db.xlink.resolver;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;

import org.citygml4j.model.citygml.CityGMLClass;

import de.tub.citydb.concurrent.WorkerPool;
import de.tub.citydb.config.Config;
import de.tub.citydb.db.cache.CacheManager;
import de.tub.citydb.db.cache.HeapCacheTable;
import de.tub.citydb.db.cache.model.CacheTableModelEnum;
import de.tub.citydb.db.gmlId.DBGmlIdLookupServerManager;
import de.tub.citydb.db.gmlId.GmlIdEntry;
import de.tub.citydb.db.importer.DBSequencer;
import de.tub.citydb.db.importer.DBSequencerEnum;
import de.tub.citydb.db.xlink.DBXlink;
import de.tub.citydb.event.Event;
import de.tub.citydb.event.EventDispatcher;
import de.tub.citydb.filter.ImportFilter;

public class DBXlinkResolverManager {
	private final Connection batchConn;
	private final Connection commitConn;
	private final WorkerPool<DBXlink> tmpXlinkPool;
	private final CacheManager dbTempTableManager;
	private final ImportFilter importFilter;
	private final Config config;
	private final EventDispatcher eventDispatcher;

	private HashMap<DBXlinkResolverEnum, DBXlinkResolver> dbWriterMap;
	private DBGmlIdResolver dbGmlIdResolver;
	private DBSequencer dbSequencer;

	public DBXlinkResolverManager(
			Connection batchConn,
			Connection commitConn,
			WorkerPool<DBXlink> tmpXlinkPool,
			DBGmlIdLookupServerManager lookupServerManager,
			CacheManager dbTempTableManager,
			ImportFilter importFilter,
			Config config,
			EventDispatcher eventDispatcher) throws SQLException {
		this.batchConn = batchConn;
		this.commitConn = commitConn;
		this.tmpXlinkPool = tmpXlinkPool;
		this.dbTempTableManager = dbTempTableManager;
		this.importFilter = importFilter;
		this.config = config;
		this.eventDispatcher = eventDispatcher;

		dbWriterMap = new HashMap<DBXlinkResolverEnum, DBXlinkResolver>();
		dbGmlIdResolver = new DBGmlIdResolver(batchConn, lookupServerManager, config);
		dbSequencer = new DBSequencer(batchConn);
	}

	public DBXlinkResolver getDBXlinkResolver(DBXlinkResolverEnum dbResolverType) throws SQLException {
		DBXlinkResolver dbResolver = dbWriterMap.get(dbResolverType);

		if (dbResolver == null) {
			// initialise DBWriter
			switch (dbResolverType) {
			case SURFACE_GEOMETRY:
				HeapCacheTable surfaceGeomHeapView = dbTempTableManager.getDerivedHeapCacheTable(CacheTableModelEnum.SURFACE_GEOMETRY);
				if (surfaceGeomHeapView != null)
					dbResolver = new XlinkSurfaceGeometry(batchConn, surfaceGeomHeapView, config, this);

				break;
			case BASIC:
				dbResolver = new XlinkBasic(batchConn, this);
				break;
			case TEXCOORDLIST:
				HeapCacheTable textureParamHeapView = dbTempTableManager.getDerivedHeapCacheTable(CacheTableModelEnum.TEXTUREPARAM);
				HeapCacheTable linearRingHeapView = dbTempTableManager.getDerivedHeapCacheTable(CacheTableModelEnum.LINEAR_RING);				
				if (textureParamHeapView != null && linearRingHeapView != null)
					dbResolver = new XlinkTexCoordList(batchConn,
							textureParamHeapView,
							linearRingHeapView, this);
				break;
			case TEXTUREPARAM:
				dbResolver = new XlinkTextureParam(batchConn, this);
				break;
			case XLINK_TEXTUREASSOCIATION:
				HeapCacheTable texAssHeapView = dbTempTableManager.getDerivedHeapCacheTable(CacheTableModelEnum.TEXTUREASSOCIATION);
				if (texAssHeapView != null)
					dbResolver = new XlinkTextureAssociation(batchConn, texAssHeapView, this);

				break;
			case TEXTURE_IMAGE:
				dbResolver = new XlinkTextureImage(commitConn, config, this);
				break;
			case LIBRARY_OBJECT:
				dbResolver = new XlinkLibraryObject(commitConn, config);
				break;
			case WORLD_FILE:
				dbResolver = new XlinkWorldFile(batchConn, config);
				break;
			case XLINK_DEPRECATED_MATERIAL:
				dbResolver = new XlinkDeprecatedMaterial(batchConn, this);
				break;
			case GROUP_TO_CITYOBJECT:
				HeapCacheTable groupHeapView = dbTempTableManager.getDerivedHeapCacheTable(CacheTableModelEnum.GROUP_TO_CITYOBJECT);
				if (groupHeapView != null)					
					dbResolver = new XlinkGroupToCityObject(batchConn, groupHeapView, importFilter, this);

				break;
			}

			if (dbResolver != null)
				dbWriterMap.put(dbResolverType, dbResolver);
		}

		return dbResolver;
	}

	public long getDBId(DBSequencerEnum sequence) throws SQLException {
		return dbSequencer.getDBId(sequence);
	}

	public GmlIdEntry getDBId(String gmlId, CityGMLClass type) {
		return getDBId(gmlId, type, false);
	}

	public GmlIdEntry getDBId(String gmlId, CityGMLClass type, boolean forceCityObjectDatabaseLookup) {
		return dbGmlIdResolver.getDBId(gmlId, type, forceCityObjectDatabaseLookup);
	}

	public void propagateXlink(DBXlink xlink) {
		tmpXlinkPool.addWork(xlink);
	}

	public void propagateEvent(Event event) {
		eventDispatcher.triggerEvent(event);
	}

	public void executeBatch() throws SQLException {
		for (DBXlinkResolver dbResolver : dbWriterMap.values())
			dbResolver.executeBatch();
	}

	public void close() throws SQLException {
		dbGmlIdResolver.close();
		
		for (DBXlinkResolver dbResolver : dbWriterMap.values())
			dbResolver.close();
	}
}

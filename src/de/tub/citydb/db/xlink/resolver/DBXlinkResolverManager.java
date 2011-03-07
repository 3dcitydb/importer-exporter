package de.tub.citydb.db.xlink.resolver;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;

import de.tub.citydb.concurrent.WorkerPool;
import de.tub.citydb.config.Config;
import de.tub.citydb.db.cache.DBGmlIdLookupServerManager;
import de.tub.citydb.db.cache.GmlIdEntry;
import de.tub.citydb.db.importer.DBSequencer;
import de.tub.citydb.db.importer.DBSequencerEnum;
import de.tub.citydb.db.temp.DBTempHeapTable;
import de.tub.citydb.db.temp.DBTempTableManager;
import de.tub.citydb.db.temp.model.DBTempTableModelEnum;
import de.tub.citydb.db.xlink.DBXlink;
import de.tub.citydb.event.Event;
import de.tub.citydb.event.EventDispatcher;
import de.tub.citydb.filter.ImportFilter;
import de.tub.citygml4j.model.citygml.CityGMLClass;

public class DBXlinkResolverManager {
	private final Connection batchConn;
	private final Connection externalFileConn;
	private final WorkerPool<DBXlink> tmpXlinkPool;
	private final DBTempTableManager dbTempTableManager;
	private final ImportFilter importFilter;
	private final Config config;
	private final EventDispatcher eventDispatcher;

	private HashMap<DBXlinkResolverEnum, DBXlinkResolver> dbWriterMap;
	private DBGmlIdResolver dbGmlIdResolver;
	private DBSequencer dbSequencer;

	public DBXlinkResolverManager(
			Connection batchConn,
			Connection externalFileConn,
			WorkerPool<DBXlink> tmpXlinkPool,
			DBGmlIdLookupServerManager lookupServerManager,
			DBTempTableManager dbTempTableManager,
			ImportFilter importFilter,
			Config config,
			EventDispatcher eventDispatcher) throws SQLException {
		this.batchConn = batchConn;
		this.externalFileConn = externalFileConn;
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
				DBTempHeapTable surfaceGeomHeapView = dbTempTableManager.getGTTHeapView(DBTempTableModelEnum.SURFACE_GEOMETRY);
				if (surfaceGeomHeapView != null)
					dbResolver = new XlinkSurfaceGeometry(batchConn, surfaceGeomHeapView, config, this);

				break;
			case BASIC:
				dbResolver = new XlinkBasic(batchConn, this);
				break;
			case TEXCOORDLIST:
				DBTempHeapTable textureParamHeapView = dbTempTableManager.getGTTHeapView(DBTempTableModelEnum.TEXTUREPARAM);
				DBTempHeapTable linearRingHeapView = dbTempTableManager.getGTTHeapView(DBTempTableModelEnum.LINEAR_RING);				
				if (textureParamHeapView != null && linearRingHeapView != null)
					dbResolver = new XlinkTexCoordList(batchConn,
							textureParamHeapView,
							linearRingHeapView, this);
				break;
			case TEXTUREPARAM:
				dbResolver = new XlinkTextureParam(batchConn, this);
				break;
			case XLINK_TEXTUREASSOCIATION:
				DBTempHeapTable texAssHeapView = dbTempTableManager.getGTTHeapView(DBTempTableModelEnum.TEXTUREASSOCIATION);
				if (texAssHeapView != null)
					dbResolver = new XlinkTextureAssociation(batchConn, texAssHeapView, this);

				break;
			case TEXTURE_IMAGE:
				dbResolver = new XlinkTextureImage(externalFileConn, config, this);
				break;
			case LIBRARY_OBJECT:
				dbResolver = new XlinkLibraryObject(externalFileConn, config, this);
				break;
			case WORLD_FILE:
				dbResolver = new XlinkWorldFile(batchConn, config, this);
				break;
			case XLINK_DEPRECATED_MATERIAL:
				dbResolver = new XlinkDeprecatedMaterial(batchConn, this);
				break;
			case GROUP_TO_CITYOBJECT:
				DBTempHeapTable groupHeapView = dbTempTableManager.getGTTHeapView(DBTempTableModelEnum.GROUP_TO_CITYOBJECT);
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

}

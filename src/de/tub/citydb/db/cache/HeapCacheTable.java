package de.tub.citydb.db.cache;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.locks.ReentrantLock;

import oracle.jdbc.driver.OracleConnection;
import de.tub.citydb.db.DBConnectionPool;
import de.tub.citydb.db.cache.model.CacheTableBasic;
import de.tub.citydb.db.cache.model.CacheTableDeprecatedMaterial;
import de.tub.citydb.db.cache.model.CacheTableGmlId;
import de.tub.citydb.db.cache.model.CacheTableGroupToCityObject;
import de.tub.citydb.db.cache.model.CacheTableLibraryObject;
import de.tub.citydb.db.cache.model.CacheTableLinearRing;
import de.tub.citydb.db.cache.model.CacheTableModel;
import de.tub.citydb.db.cache.model.CacheTableModelEnum;
import de.tub.citydb.db.cache.model.CacheTableSurfaceGeometry;
import de.tub.citydb.db.cache.model.CacheTableTextureAssociation;
import de.tub.citydb.db.cache.model.CacheTableTextureFile;
import de.tub.citydb.db.cache.model.CacheTableTextureParam;
import de.tub.citydb.db.cache.model.CacheTableType;
import de.tub.citydb.util.UUIDManager;

public class HeapCacheTable implements CacheTable {
	private final CacheTableModel model;
	private final DBConnectionPool dbPool;
	private final ReentrantLock mainLock = new ReentrantLock();
	private final String tableName;
	private final boolean isStandAlone;

	private Connection conn;

	private volatile boolean isCreated = false;
	private volatile boolean isIndexed = false;

	protected HeapCacheTable(CacheTableModelEnum model, DBConnectionPool dbPool, boolean isStandAlone) {
		switch (model) {
		case BASIC:
			this.model = CacheTableBasic.getInstance();
			break;
		case DEPRECATED_MATERIAL:
			this.model = CacheTableDeprecatedMaterial.getInstance();
			break;
		case TEXTURE_FILE:
			this.model = CacheTableTextureFile.getInstance();
			break;
		case LIBRARY_OBJECT:
			this.model = CacheTableLibraryObject.getInstance();
			break;
		case GMLID_FEATURE:			
		case GMLID_GEOMETRY:
			this.model = CacheTableGmlId.getInstance(model);
			break;
		case GROUP_TO_CITYOBJECT:
			this.model = CacheTableGroupToCityObject.getInstance();
			break;
		case LINEAR_RING:
			this.model = CacheTableLinearRing.getInstance();
			break;
		case SURFACE_GEOMETRY:
			this.model = CacheTableSurfaceGeometry.getInstance();
			break;
		case TEXTUREASSOCIATION:
			this.model = CacheTableTextureAssociation.getInstance();
			break;
		case TEXTUREPARAM:
			this.model = CacheTableTextureParam.getInstance();
			break;
		default:
			throw new IllegalArgumentException("Unsupported cache table type " + model);
		}
		
		this.dbPool = dbPool;
		this.isStandAlone = isStandAlone;
		tableName = createTableName();
	}

	protected HeapCacheTable(CacheTableModelEnum model, DBConnectionPool dbPool) {
		this(model, dbPool, true);
	}

	protected void create() throws SQLException {
		if (isCreated)
			return;

		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			if (!isCreated) {				
				conn = dbPool.getConnection();
				conn.setAutoCommit(false);
				((OracleConnection)conn).setImplicitCachingEnabled(true);

				model.create(conn, tableName, CacheTableType.HEAP_TABLE);
				isCreated = true;
			}
		} finally {
			lock.unlock();
		}
	}

	protected void createTableAsSelect(Connection conn, String sourceTableName) throws SQLException {
		if (isCreated)
			return;

		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			if (!isCreated) {
				model.createAsSelectFrom(conn, tableName, sourceTableName);
				this.conn = conn;
				isCreated = true;
			}
		} finally {
			lock.unlock();
		}
	}

	protected void createIndexes() throws SQLException {
		if (!isCreated || isIndexed)
			return;

		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			if (!isIndexed) {
				model.createIndexes(conn, tableName, "nologging");
				isIndexed = true;
			}
		} finally {
			lock.unlock();
		}
	}
	
	protected void createWithIndexes() throws SQLException {
		final ReentrantLock lock = this.mainLock;
		lock.lock();
		
		try {
			create();			
			createIndexes();
		} finally {
			lock.unlock();
		}
	}
		
	public void truncate() throws SQLException {
		if (!isCreated)
			return;

		model.truncate(conn, tableName);
	}

	public long size() throws SQLException {
		if (!isCreated)
			return -1;

		return model.size(conn, tableName);
	}
	
	public String getTableName() {
		return tableName;
	}

	public boolean isCreated() {
		return isCreated;
	}

	public boolean isIndexed() {
		return isIndexed;
	}

	public boolean isStandAlone() {
		return isStandAlone;
	}

	public Connection getConnection() {
		return conn;
	}

	protected void drop() throws SQLException {
		if (!isStandAlone)
			throw new IllegalStateException("Drop may not be called on a child of a compound table.");
		
		dropInternal();
	}
	
	protected void dropInternal() throws SQLException {
		if (!isCreated)
			return;

		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			if (isCreated) {			
				try {
					model.drop(conn, tableName);
					isCreated = false;
				} finally {
					if (isStandAlone && conn != null) {
						conn.close();
						conn = null;
					}
				}
			}
		} finally {
			lock.unlock();
		}
	}

	@Override
	public CacheTableEnum getType() {
		return CacheTableEnum.HEAP;
	}

	private String createTableName() {
		String tableName = "TMP_H_" + model.getType().value() + Math.abs(UUIDManager.randomUUID().hashCode());
		if (tableName.length() > 28)
			tableName = tableName.substring(tableName.length() - 28, tableName.length());

		return tableName;
	}
}

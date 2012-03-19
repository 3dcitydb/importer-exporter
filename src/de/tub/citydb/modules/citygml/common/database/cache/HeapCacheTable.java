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
package de.tub.citydb.modules.citygml.common.database.cache;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.locks.ReentrantLock;

import org.citygml4j.util.gmlid.DefaultGMLIdManager;

import de.tub.citydb.database.DatabaseConnectionPool;
import de.tub.citydb.modules.citygml.common.database.cache.model.CacheTableBasic;
import de.tub.citydb.modules.citygml.common.database.cache.model.CacheTableDeprecatedMaterial;
import de.tub.citydb.modules.citygml.common.database.cache.model.CacheTableGlobalAppearance;
import de.tub.citydb.modules.citygml.common.database.cache.model.CacheTableGmlId;
import de.tub.citydb.modules.citygml.common.database.cache.model.CacheTableGroupToCityObject;
import de.tub.citydb.modules.citygml.common.database.cache.model.CacheTableLibraryObject;
import de.tub.citydb.modules.citygml.common.database.cache.model.CacheTableLinearRing;
import de.tub.citydb.modules.citygml.common.database.cache.model.CacheTableModel;
import de.tub.citydb.modules.citygml.common.database.cache.model.CacheTableModelEnum;
import de.tub.citydb.modules.citygml.common.database.cache.model.CacheTableSurfaceGeometry;
import de.tub.citydb.modules.citygml.common.database.cache.model.CacheTableTextureAssociation;
import de.tub.citydb.modules.citygml.common.database.cache.model.CacheTableTextureFile;
import de.tub.citydb.modules.citygml.common.database.cache.model.CacheTableTextureParam;
import de.tub.citydb.modules.citygml.common.database.cache.model.CacheTableType;

public class HeapCacheTable implements CacheTable {
	private final CacheTableModel model;
	private final DatabaseConnectionPool dbPool;
	private final ReentrantLock mainLock = new ReentrantLock();
	private final String tableName;
	private final boolean isStandAlone;

	private Connection conn;

	private volatile boolean isCreated = false;
	private volatile boolean isIndexed = false;

	protected HeapCacheTable(CacheTableModelEnum model, DatabaseConnectionPool dbPool, boolean isStandAlone) {
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
		case GLOBAL_APPEARANCE:
			this.model = CacheTableGlobalAppearance.getInstance();
			break;
		default:
			throw new IllegalArgumentException("Unsupported cache table type " + model);
		}
		
		this.dbPool = dbPool;
		this.isStandAlone = isStandAlone;
		tableName = createTableName();
	}

	protected HeapCacheTable(CacheTableModelEnum model, DatabaseConnectionPool dbPool) {
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
		String tableName = "TMP_H_" + model.getType().value() + Math.abs(DefaultGMLIdManager.getInstance().generateUUID().hashCode());
		if (tableName.length() > 28)
			tableName = tableName.substring(tableName.length() - 28, tableName.length());

		return tableName;
	}
}

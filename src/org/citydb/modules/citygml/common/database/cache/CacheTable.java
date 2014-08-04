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
package org.citydb.modules.citygml.common.database.cache;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.locks.ReentrantLock;

import org.citydb.database.adapter.AbstractSQLAdapter;
import org.citydb.modules.citygml.common.database.cache.model.CacheTableBasic;
import org.citydb.modules.citygml.common.database.cache.model.CacheTableDeprecatedMaterial;
import org.citydb.modules.citygml.common.database.cache.model.CacheTableFeatureGmlId;
import org.citydb.modules.citygml.common.database.cache.model.CacheTableGeometryGmlId;
import org.citydb.modules.citygml.common.database.cache.model.CacheTableGlobalAppearance;
import org.citydb.modules.citygml.common.database.cache.model.CacheTableGroupToCityObject;
import org.citydb.modules.citygml.common.database.cache.model.CacheTableLibraryObject;
import org.citydb.modules.citygml.common.database.cache.model.CacheTableLinearRing;
import org.citydb.modules.citygml.common.database.cache.model.CacheTableModel;
import org.citydb.modules.citygml.common.database.cache.model.CacheTableModelEnum;
import org.citydb.modules.citygml.common.database.cache.model.CacheTableSolidGeometry;
import org.citydb.modules.citygml.common.database.cache.model.CacheTableSurfaceDataToTexImage;
import org.citydb.modules.citygml.common.database.cache.model.CacheTableSurfaceGeometry;
import org.citydb.modules.citygml.common.database.cache.model.CacheTableTextureAssociation;
import org.citydb.modules.citygml.common.database.cache.model.CacheTableTextureAssociationTarget;
import org.citydb.modules.citygml.common.database.cache.model.CacheTableTextureCoordList;
import org.citydb.modules.citygml.common.database.cache.model.CacheTableTextureFile;
import org.citydb.modules.citygml.common.database.cache.model.CacheTableTextureFileId;
import org.citydb.modules.citygml.common.database.cache.model.CacheTableTextureParam;
import org.citygml4j.util.gmlid.DefaultGMLIdManager;

public class CacheTable extends AbstractCacheTable {	
	private final CacheTableModel model;
	private final ReentrantLock mainLock = new ReentrantLock();
	private final String tableName;
	private final boolean isStandAlone;

	private CacheTable mirrorTable;
	private volatile boolean isCreated = false;
	private volatile boolean isIndexed = false;

	protected CacheTable(CacheTableModelEnum model, Connection connection, AbstractSQLAdapter sqlAdapter, boolean isStandAlone) {
		super(connection, sqlAdapter);
		
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
		case TEXTURE_FILE_ID:
			this.model = CacheTableTextureFileId.getInstance();
			break;
		case LIBRARY_OBJECT:
			this.model = CacheTableLibraryObject.getInstance();
			break;
		case GMLID_FEATURE:
			this.model = CacheTableFeatureGmlId.getInstance();
			break;
		case GMLID_GEOMETRY:
			this.model = CacheTableGeometryGmlId.getInstance();
			break;
		case GROUP_TO_CITYOBJECT:
			this.model = CacheTableGroupToCityObject.getInstance();
			break;
		case SURFACE_GEOMETRY:
			this.model = CacheTableSurfaceGeometry.getInstance();
			break;
		case SOLID_GEOMETRY:
			this.model = CacheTableSolidGeometry.getInstance();
			break;
		case LINEAR_RING:
			this.model = CacheTableLinearRing.getInstance();
			break;
		case TEXTUREASSOCIATION:
			this.model = CacheTableTextureAssociation.getInstance();
			break;
		case TEXTUREASSOCIATION_TARGET:
			this.model = CacheTableTextureAssociationTarget.getInstance();
			break;
		case TEXTURE_COORD_LIST:
			this.model = CacheTableTextureCoordList.getInstance();
			break;
		case TEXTUREPARAM:
			this.model = CacheTableTextureParam.getInstance();
			break;
		case SURFACE_DATA_TO_TEX_IMAGE:
			this.model = CacheTableSurfaceDataToTexImage.getInstance();
			break;
		case GLOBAL_APPEARANCE:
			this.model = CacheTableGlobalAppearance.getInstance();
			break;
		default:
			throw new IllegalArgumentException("Unsupported cache table type " + model);
		}

		this.isStandAlone = isStandAlone;
		tableName = generateUniqueTableName();
	}

	protected CacheTable(CacheTableModelEnum model, Connection connection, AbstractSQLAdapter sqlAdapter) {
		this(model, connection, sqlAdapter, true);
	}
	
	@Override
	protected void create() throws SQLException {		
		if (isCreated)
			return;

		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			if (!isCreated) {
				model.create(connection, tableName, sqlAdapter);
				isCreated = true;
			}
		} finally {
			lock.unlock();
		}
	}
	
	protected void createAsSelectFrom(String sourceTableName) throws SQLException {
		if (isCreated)
			return;

		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			if (!isCreated) {
				model.createAsSelectFrom(connection, tableName, sourceTableName, sqlAdapter);
				isCreated = true;
			}
		} finally {
			lock.unlock();
		}
	}
	
	@Override
	protected void createAndIndex() throws SQLException {
		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			create();			
			createIndexes();
		} finally {
			lock.unlock();
		}
	}

	public void createIndexes() throws SQLException {
		if (!isCreated || isIndexed)
			return;

		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			if (!isIndexed) {
				model.createIndexes(connection, tableName, sqlAdapter.getUnloggedIndexProperty());
				isIndexed = true;
			}
		} finally {
			lock.unlock();
		}
	}

	public void truncate() throws SQLException {
		if (!isCreated)
			return;

		model.truncate(connection, tableName);
	}

	public long size() throws SQLException {
		if (!isCreated)
			return -1;

		return model.size(connection, tableName);
	}

	public String getTableName() {
		return tableName;
	}

	@Override
	public boolean isCreated() {
		return isCreated;
	}

	public boolean isIndexed() {
		return isIndexed;
	}

	public boolean isStandAlone() {
		return isStandAlone;
	}

	public CacheTable mirror() throws SQLException {
		if (!isCreated)
			return null;

		if (mirrorTable != null)
			return mirrorTable;

		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			if (isCreated && mirrorTable == null) {
				mirrorTable = new CacheTable(model.getType(), connection, sqlAdapter, false);
				mirrorTable.createAsSelectFrom(tableName);
			}

			return mirrorTable;
		} finally {
			lock.unlock();
		}
	}
	
	public CacheTable mirrorAndIndex() throws SQLException {
		if (!isCreated)
			return null;

		if (mirrorTable != null)
			return mirrorTable;

		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			if (isCreated && mirrorTable == null) {
				mirror();			
				mirrorTable.createIndexes();
			}

			return mirrorTable;
		} finally {
			lock.unlock();
		}
	}
	
	public void dropMirrorTable() throws SQLException {
		if (mirrorTable == null)
			return;

		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			if (mirrorTable != null) {
				mirrorTable.dropInternal();
				mirrorTable = null;
			}
		} finally {
			lock.unlock();
		}
	}

	public CacheTable getMirrorTable() {
		return mirrorTable;
	}
	
	@Override
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
				model.drop(connection, tableName);
				
				if (mirrorTable != null)
					dropMirrorTable();
				
				isCreated = false;
			}
		} finally {
			lock.unlock();
		}
	}

	@Override
	public CacheTableModelEnum getModelType() {
		return model.getType();
	}
	
	private String generateUniqueTableName() {		
		String name = "TMP_" + model.getType().value() + ID + Math.abs(DefaultGMLIdManager.getInstance().generateUUID().hashCode());
		if (name.length() > 28)
			name = name.substring(0, 28);

		return name;
	}
}

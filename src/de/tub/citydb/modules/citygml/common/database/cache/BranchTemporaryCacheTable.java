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
package de.tub.citydb.modules.citygml.common.database.cache;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import de.tub.citydb.database.DatabaseConnectionPool;
import de.tub.citydb.modules.citygml.common.database.cache.model.CacheTableModelEnum;

public class BranchTemporaryCacheTable implements CacheTable {
	private final TemporaryCacheTable main;
	private final CacheTableModelEnum model;
	private final DatabaseConnectionPool dbPool;
	private final ReentrantLock mainLock = new ReentrantLock();

	private Connection conn;
	private volatile boolean isCreated = false;
	private List<TemporaryCacheTable> branches;

	protected BranchTemporaryCacheTable(CacheTableModelEnum model, DatabaseConnectionPool dbPool) {
		this.model = model;
		this.dbPool = dbPool;

		main = new TemporaryCacheTable(model, dbPool, false);
		branches = new ArrayList<TemporaryCacheTable>();
	}

	protected void create() throws SQLException {
		if (isCreated)
			return;

		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			if (!isCreated) {
				main.create();
				conn = main.getConnection();
				isCreated = true;
			}
		} finally {
			lock.unlock();
		}
	}

	protected void createWithIndexes() throws SQLException {
		if (isCreated)
			return;

		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			if (!isCreated) {
				main.createWithIndexes();
				conn = main.getConnection();
				isCreated = true;
			}
		} finally {
			lock.unlock();
		}
	}

	public TemporaryCacheTable branch() throws SQLException {
		if (!isCreated)
			return null;

		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			if (isCreated) {
				TemporaryCacheTable branch = new TemporaryCacheTable(model, dbPool, false);	
				branch.create(conn);
				branches.add(branch);

				return branch;
			} else
				return null;
		} finally {
			lock.unlock();
		}
	}

	public TemporaryCacheTable branchWithIndexes() throws SQLException {
		if (!isCreated)
			return null;

		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			if (isCreated) {
				TemporaryCacheTable branch = new TemporaryCacheTable(model, dbPool, false);	
				branch.createWithIndexes(conn);
				branches.add(branch);

				return branch;
			} else
				return null;
		} finally {
			lock.unlock();
		}
	}

	public TemporaryCacheTable getMainTable() {
		return main;
	}

	public List<TemporaryCacheTable> getBranchTables() {
		return new ArrayList<TemporaryCacheTable>(branches);
	}

	@Override
	public CacheTableEnum getType() {
		return CacheTableEnum.BRANCH_TEMPORARY;
	}

	public boolean isCreated() {
		return isCreated;
	}

	protected void drop() throws SQLException {
		if (!isCreated)
			return;

		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			if (isCreated) {
				try {
					main.dropInternal();

					for (TemporaryCacheTable branch : branches)
						branch.dropInternal();

					isCreated = false;
				} finally {
					if (conn != null) {
						conn.close();
						conn = null;
					}
				}
			}
		} finally {
			lock.unlock();
		}
	}

}

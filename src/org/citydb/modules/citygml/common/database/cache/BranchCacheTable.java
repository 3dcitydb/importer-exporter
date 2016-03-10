/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * (C) 2013 - 2016,
 * Chair of Geoinformatics,
 * Technische Universitaet Muenchen, Germany
 * http://www.gis.bgu.tum.de/
 * 
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 * 
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Muenchen <http://www.moss.de/>
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 */
package org.citydb.modules.citygml.common.database.cache;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.citydb.database.adapter.AbstractSQLAdapter;
import org.citydb.modules.citygml.common.database.cache.model.CacheTableModelEnum;

public class BranchCacheTable extends AbstractCacheTable {
	private final CacheTable main;
	private final CacheTableModelEnum model;
	private final ReentrantLock mainLock = new ReentrantLock();

	private volatile boolean isCreated = false;
	private List<CacheTable> branches;

	protected BranchCacheTable(CacheTableModelEnum model, Connection connection, AbstractSQLAdapter sqlAdapter) {
		super(connection, sqlAdapter);
		this.model = model;

		main = new CacheTable(model, connection, sqlAdapter, false);
		branches = new ArrayList<CacheTable>();
	}

	@Override
	protected void create() throws SQLException {
		if (isCreated)
			return;

		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			if (!isCreated) {
				main.create();
				isCreated = true;
			}
		} finally {
			lock.unlock();
		}
	}

	@Override
	protected void createAndIndex() throws SQLException {
		if (isCreated)
			return;

		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			if (!isCreated) {
				main.createAndIndex();
				isCreated = true;
			}
		} finally {
			lock.unlock();
		}
	}

	public CacheTable branch() throws SQLException {
		if (!isCreated)
			return null;

		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			if (isCreated) {
				CacheTable branch = new CacheTable(model, connection, sqlAdapter, false);	
				branch.create();
				branches.add(branch);

				return branch;
			} else
				return null;
		} finally {
			lock.unlock();
		}
	}

	public CacheTable branchAndIndex() throws SQLException {
		if (!isCreated)
			return null;

		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			if (isCreated) {
				CacheTable branch = new CacheTable(model, connection, sqlAdapter, false);	
				branch.createAndIndex();
				branches.add(branch);

				return branch;
			} else
				return null;
		} finally {
			lock.unlock();
		}
	}

	public CacheTable getMainTable() {
		return main;
	}

	public List<CacheTable> getBranchTables() {
		return new ArrayList<CacheTable>(branches);
	}

	@Override
	public boolean isCreated() {
		return isCreated;
	}

	@Override
	protected void drop() throws SQLException {
		if (!isCreated)
			return;

		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			if (isCreated) {
				main.dropInternal();
				for (CacheTable branch : branches)
					branch.dropInternal();

				isCreated = false;
			}
		} finally {
			lock.unlock();
		}
	}

	@Override
	public CacheTableModelEnum getModelType() {
		return model;
	}

}

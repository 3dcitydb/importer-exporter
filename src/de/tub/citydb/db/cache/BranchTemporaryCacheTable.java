package de.tub.citydb.db.cache;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import de.tub.citydb.db.DBConnectionPool;
import de.tub.citydb.db.cache.model.CacheTableModelEnum;

public class BranchTemporaryCacheTable implements CacheTable {
	private final TemporaryCacheTable main;
	private final CacheTableModelEnum model;
	private final DBConnectionPool dbPool;
	private final ReentrantLock mainLock = new ReentrantLock();

	private Connection conn;
	private volatile boolean isCreated = false;
	private List<TemporaryCacheTable> branches;

	protected BranchTemporaryCacheTable(CacheTableModelEnum model, DBConnectionPool dbPool) {
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

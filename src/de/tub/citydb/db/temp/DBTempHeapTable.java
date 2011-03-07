package de.tub.citydb.db.temp;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.locks.ReentrantLock;

import oracle.jdbc.driver.OracleConnection;

import de.tub.citydb.db.DBConnectionPool;
import de.tub.citydb.db.temp.model.DBTempTableModel;
import de.tub.citydb.util.UUIDManager;

public class DBTempHeapTable implements DBTempTable {
	private final DBTempTableModel tempTableModel;
	private final DBConnectionPool dbPool;
	private final ReentrantLock mainLock = new ReentrantLock();

	private Connection conn;
	private String tableName;

	private volatile boolean isCreated = false;
	private volatile boolean isIndexed = false;

	public DBTempHeapTable(DBTempTableModel tempTableModel, DBConnectionPool dbPool) {
		this.tempTableModel = tempTableModel;
		this.dbPool = dbPool;
	}

	@Override
	public void create() throws SQLException {
		if (isCreated)
			return;

		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			if (!isCreated) {
				tableName = createTableName();
				conn = dbPool.getConnection();
				conn.setAutoCommit(false);
				((OracleConnection)conn).setImplicitCachingEnabled(true);

				tempTableModel.createHeap(conn, tableName);
				conn.commit();
				isCreated = true;
			}
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void createIndexed() throws SQLException {
		if (isIndexed)
			return;
		
		final ReentrantLock lock = this.mainLock;
		lock.lock();
		
		try {
			create();			
			if (!isIndexed) {
				tempTableModel.createHeapIndexes(conn, tableName);
				isIndexed = true;
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
				Statement stmt = null;
				try {
					tableName = createTableName();
					stmt = conn.createStatement();
					stmt.executeUpdate("create table " + 
							tableName +
							" nologging" +
							" as select * from " + 
							sourceTableName);

					conn.commit();
					this.conn = conn;
					isCreated = true;
				} finally {
					if (stmt != null) {
						stmt.close();
						stmt = null;
					}
				}
			}
		} finally {
			lock.unlock();
		}
	}

	public String getTableName() {
		if (isCreated)
			return tableName;

		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			return tableName;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public boolean isCreated() {
		return isCreated;
	}

	public boolean isIndexed() {
		return isIndexed;
	}

	public Connection getConnection() {
		if (isCreated)
			return conn;

		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			return conn;
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
				tempTableModel.createHeapIndexes(conn, tableName);
				isIndexed = true;
			}
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void drop() throws SQLException {
		if (!isCreated)
			return;

		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			if (isCreated) {			
				Statement stmt = null;

				try {
					stmt = conn.createStatement();
					stmt.executeUpdate("drop table " + tableName);

					conn.commit();
					isCreated = false;
				} finally {
					if (stmt != null) {
						stmt.close();
						stmt = null;
					}

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

	@Override
	public DBTempTableEnum getType() {
		return DBTempTableEnum.HEAP;
	}

	private String createTableName() {
		String tableName = "TMP_H_" + tempTableModel.getType().value() + Math.abs(UUIDManager.randomUUID().hashCode());
		if (tableName.length() > 28)
			tableName = tableName.substring(tableName.length() - 28, tableName.length());

		return tableName;
	}
}

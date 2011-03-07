package de.tub.citydb.db.temp;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;

import oracle.jdbc.driver.OracleConnection;
import de.tub.citydb.db.DBConnectionPool;
import de.tub.citydb.db.temp.model.DBTempTableModel;
import de.tub.citydb.util.UUIDManager;

public class DBTempGTT implements DBTempTable {	
	private final DBTempTableModel tempTableModel;
	private final DBConnectionPool dbPool;
	private final ReentrantLock mainLock = new ReentrantLock();

	private Connection writer;
	private Connection reader;	
	private String tableName;
	private LinkedList<DBTempGTT> branches;

	private DBTempHeapTable heapView;
	private volatile boolean isCreated = false;
	private volatile boolean isDecoupled = false;
	private volatile boolean isIndexed = false;
	private boolean isBranch = false;

	public DBTempGTT(DBTempTableModel tempTableModel, DBConnectionPool dbPool) {
		this.tempTableModel = tempTableModel;
		this.dbPool = dbPool;

		branches = new LinkedList<DBTempGTT>();
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
				writer = reader = dbPool.getConnection();
				writer.setAutoCommit(false);
				((OracleConnection)writer).setImplicitCachingEnabled(true);

				tempTableModel.createGTT(writer, tableName);
				writer.commit();
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
				tempTableModel.createGTTIndexes(writer, tableName);
				isIndexed = true;
			}
		} finally {
			lock.unlock();
		}
	}

	private void create(Connection conn) throws SQLException {
		if (isCreated)
			return;

		tableName = createTableName();
		writer = reader = conn;

		tempTableModel.createGTT(conn, tableName);
		conn.commit();
		isCreated = true;
	}

	private void createIndexed(Connection conn) throws SQLException {
		create(conn);
		if (!isIndexed) {
			tempTableModel.createGTTIndexes(conn, tableName);
			isIndexed = true;
		}
	}

	public Connection getWriter() {
		if (isCreated)
			return writer;

		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			return writer;
		} finally {
			lock.unlock();
		}
	}

	public Connection getReader() {
		if (isCreated)
			return reader;

		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			return reader;
		} finally {
			lock.unlock();
		}
	}

	public void decoupleWriter() throws SQLException {
		if (isDecoupled)
			return;

		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			if (!isDecoupled) {
				Connection decoupledWriter = dbPool.getConnection();				
				decoupledWriter.setAutoCommit(false);
				((OracleConnection)decoupledWriter).setImplicitCachingEnabled(true);

				writer = decoupledWriter;
				isDecoupled = true;
			}
		} finally {
			lock.unlock();
		}
	}

	public void swap() {
		if (!isDecoupled)
			return;

		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			Connection swap = reader;
			reader = writer;
			writer = swap;			
		} finally {
			lock.unlock();
		}
	}

	public void truncate(Connection conn) throws SQLException {
		if (!isCreated)
			return;

		Statement stmt = null;

		try {
			stmt = conn.createStatement();
			stmt.executeUpdate("truncate table " + tableName);
			conn.commit();
		} finally {
			if (stmt != null) {
				stmt.close();
				stmt = null;
			}
		}
	}

	public long getWriterCount() throws SQLException {
		if (!isCreated)
			return -1;

		Statement stmt = null;
		ResultSet rs = null;
		long count = -1;

		try {
			stmt = writer.createStatement();
			rs = stmt.executeQuery("select count(*) from " + tableName);

			if (rs.next())
				count = rs.getLong(1);

		} finally {
			if (rs != null) {
				rs.close();
				rs = null;
			}

			if (stmt != null) {
				stmt.close();
				stmt = null;
			}
		}

		return count;
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

	public boolean isDecoupled() {
		return isDecoupled;
	}

	public DBTempGTT branch() throws SQLException {
		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			DBTempGTT branch = new DBTempGTT(tempTableModel, dbPool);	
			branch.create(writer);
			branch.isBranch = true;
			branches.addLast(branch);

			return branch;
		} finally {
			lock.unlock();
		}
	}

	public DBTempGTT branchIndexed() throws SQLException {
		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			DBTempGTT branch = new DBTempGTT(tempTableModel, dbPool);	
			branch.createIndexed(writer);
			branch.isBranch = true;
			branches.addLast(branch);

			return branch;
		} finally {
			lock.unlock();
		}
	}

	public int getNumberOfBranches() {
		return branches.size();
	}

	public LinkedList<DBTempGTT> getBranches() {
		return new LinkedList<DBTempGTT>(branches);
	}

	public DBTempHeapTable createHeapView(Connection conn) throws SQLException {
		if (!isCreated || (conn != writer && conn != reader))
			return null;

		if (heapView != null)
			return heapView;

		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			if (isCreated && heapView == null) {
				heapView = new DBTempHeapTable(tempTableModel, dbPool);
				heapView.createTableAsSelect(conn, tableName);
			}

			return heapView;
		} finally {
			lock.unlock();
		}
	}

	public DBTempHeapTable createIndexedHeapView(Connection conn) throws SQLException {
		if (!isCreated || (conn != writer && conn != reader))
			return null;

		if (heapView != null)
			return heapView;

		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			if (isCreated && heapView == null) {
				createHeapView(conn);			
				heapView.createIndexes();
			}

			return heapView;
		} finally {
			lock.unlock();
		}
	}

	public DBTempHeapTable createIndexedHeapViewOfWriter() throws SQLException {
		return createIndexedHeapView(writer);
	}

	public DBTempHeapTable createIndexedHeapViewOfReader() throws SQLException {
		return createIndexedHeapView(reader);
	}

	public DBTempHeapTable getHeapView() {
		if (heapView != null)
			return heapView;

		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			return heapView;
		} finally {
			lock.unlock();
		}
	}

	public void dropHeapView() throws SQLException {
		if (heapView == null)
			return;

		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			if (heapView != null) {
				Statement stmt = null;

				try {
					stmt = heapView.getConnection().createStatement();
					stmt.executeUpdate("drop table " + heapView.getTableName());
					heapView.getConnection().commit();
					heapView = null;
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
					truncate(writer);
					if (isDecoupled)
						truncate(reader);

					stmt = writer.createStatement();
					stmt.executeUpdate("drop table " + tableName);

					if (heapView != null)
						dropHeapView();

					for (DBTempGTT branch : branches)
						branch.drop();

					writer.commit();
					isCreated = false;
				} finally {
					if (stmt != null) {
						stmt.close();
						stmt = null;
					}

					if (!isBranch) {
						if (writer != null) {
							writer.close();
							writer = null;
						}

						if (isDecoupled && reader != null) {
							reader.close();
							reader = null;
						}
					}
				}
			}
		} finally {
			lock.unlock();
		}
	}

	@Override
	public DBTempTableEnum getType() {
		return DBTempTableEnum.GLOBAL_TEMPORARY;
	}

	private String createTableName() {		
		String tableName = "TMP_" + tempTableModel.getType().value() + Math.abs(UUIDManager.randomUUID().hashCode());
		if (tableName.length() > 28)
			tableName = tableName.substring(tableName.length() - 28, tableName.length());

		return tableName;
	}
}

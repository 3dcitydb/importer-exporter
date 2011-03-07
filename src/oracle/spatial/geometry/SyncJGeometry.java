package oracle.spatial.geometry;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.locks.ReentrantLock;

import oracle.sql.STRUCT;

public class SyncJGeometry extends JGeometry {
	protected static final ReentrantLock mainLock = new ReentrantLock();

	private SyncJGeometry() {
		// just to thwart instantiation
		// nevertheless we have to call the super constructor
		super(0., 0., 0., 0., 0);
	}

	public static STRUCT syncStore(JGeometry geom, Connection connection) throws SQLException {
		final ReentrantLock lock = mainLock;
		lock.lock();

		try {
			clearDBDescriptors();
			return JGeometry.store(geom, connection);
		} finally {
			lock.unlock();
		}
	}

	public static void clearDBDescriptors() {
		geomDesc = null;
		pointDesc = null;
		elemInfoDesc = null;
		ordinatesDesc = null;
	}

}

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
package oracle.spatial.geometry;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.locks.ReentrantLock;

import oracle.sql.STRUCT;

@SuppressWarnings("serial")
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

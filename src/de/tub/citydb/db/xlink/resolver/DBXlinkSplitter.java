package de.tub.citydb.db.xlink.resolver;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import de.tub.citydb.concurrent.WorkerPool;
import de.tub.citydb.db.DBTableEnum;
import de.tub.citydb.db.temp.DBTempGTT;
import de.tub.citydb.db.temp.DBTempTableManager;
import de.tub.citydb.db.temp.model.DBTempTableModelEnum;
import de.tub.citydb.db.xlink.DBXlink;
import de.tub.citydb.db.xlink.DBXlinkBasic;
import de.tub.citydb.db.xlink.DBXlinkDeprecatedMaterial;
import de.tub.citydb.db.xlink.DBXlinkExternalFile;
import de.tub.citydb.db.xlink.DBXlinkExternalFileEnum;
import de.tub.citydb.db.xlink.DBXlinkGroupToCityObject;
import de.tub.citydb.db.xlink.DBXlinkSurfaceGeometry;
import de.tub.citydb.db.xlink.DBXlinkTextureParam;
import de.tub.citydb.db.xlink.DBXlinkTextureParamEnum;
import de.tub.citydb.event.EventDispatcher;
import de.tub.citydb.event.info.LogMessageEnum;
import de.tub.citydb.event.info.LogMessageEvent;

public class DBXlinkSplitter {
	private final DBTempTableManager tempTableMgr;
	private final WorkerPool<DBXlink> xlinkResolverPool;
	private final WorkerPool<DBXlink> tmpXlinkPool;
	private final EventDispatcher eventDispatcher;
	private volatile boolean shouldRun = true;

	public DBXlinkSplitter(DBTempTableManager tempTableMgr, WorkerPool<DBXlink> xlinkResolverPool, WorkerPool<DBXlink> tmpXlinkPool, EventDispatcher eventDispatcher) {
		this.tempTableMgr = tempTableMgr;
		this.xlinkResolverPool = xlinkResolverPool;
		this.tmpXlinkPool = tmpXlinkPool;
		this.eventDispatcher = eventDispatcher;
	}

	public void shutdown() {
		shouldRun = false;
	}

	public void startQuery() throws SQLException {	
		basicXlinks();
		groupMemberXLinks(true);
		appearanceXlinks();

		// restart xlink worker pools
		// just to make sure all appearance xlinks have been handled
		// before starting to work on geometry xlinks
		try {
			xlinkResolverPool.join();
			tmpXlinkPool.join();
		} catch (InterruptedException e) {
			//
		}

		// xlinks to deprecated appearances can only be handled if
		// appearances have been fully written - otherwise information is
		// missing in tables SURFACE_DATA and TEXTURPARAM
		deprecatedMaterialXlinks();

		// handling geometry xlinks is more tricky...
		// the reason is that we really hard copy the entries within the database.
		// now imagine the following situation: a geometry referenced by an xlink
		// itself points to another geometry. in order to really copy any information
		// we have to resolve the inner xlink firstly. afterwards we can deal with the
		// outer xlink. thus, we need a recursive handling here...
		surfaceGeometryXlinks(true);
	}

	private void basicXlinks() throws SQLException {
		if (!shouldRun)
			return;

		Statement stmt = null;
		ResultSet rs = null;

		try {
			DBTempGTT tempTable = tempTableMgr.getGTT(DBTempTableModelEnum.BASIC);	
			if (tempTable == null)
				return;

			eventDispatcher.triggerEvent(new LogMessageEvent(
					"Feature-Xlinks werden aufgelöst...",
					LogMessageEnum.INFO));

			stmt = tempTable.getReader().createStatement();
			rs = stmt.executeQuery("select * from " + tempTable.getTableName());

			while (rs.next() && shouldRun) {
				long id = rs.getLong("ID");
				int fromTable = rs.getInt("FROM_TABLE");
				String gmlId = rs.getString("GMLID");
				int toTable = rs.getInt("TO_TABLE");
				String attrName = rs.getString("ATTRNAME");

				// set initial context...
				DBXlinkBasic xlink = new DBXlinkBasic(
						id,
						DBTableEnum.fromInt(fromTable),
						gmlId,
						DBTableEnum.fromInt(toTable));

				if (attrName != null && attrName.length() != 0)
					xlink.setAttrName(attrName);

				xlinkResolverPool.addWork(xlink);
			}
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
	}

	private void groupMemberXLinks(boolean checkRecursive) throws SQLException {
		if (!shouldRun)
			return;
		
		DBTempGTT tempTable = tempTableMgr.getGTT(DBTempTableModelEnum.GROUP_TO_CITYOBJECT);		
		if (tempTable == null)
			return;

		eventDispatcher.triggerEvent(new LogMessageEvent(
				"CityObjectGroup-Xlinks werden aufgelöst...",
				LogMessageEnum.INFO));

		queryGroupMemberXLinks(tempTable, checkRecursive, -1);
	}

	private void queryGroupMemberXLinks(DBTempGTT tempTable, boolean checkRecursive, long remaining) throws SQLException {
		Statement stmt = null;
		ResultSet rs = null;

		try {					
			tempTable.createIndexedHeapViewOfWriter();
			tempTable.swap();

			stmt = tempTable.getReader().createStatement();
			rs = stmt.executeQuery("select * from " + tempTable.getTableName());

			while (rs.next() && shouldRun) {
				long groupId = rs.getLong("GROUP_ID");
				String gmlId = rs.getString("GMLID");
				int isParent = rs.getInt("IS_PARENT");
				String role = rs.getString("ROLE");

				// set initial context...
				DBXlinkGroupToCityObject xlink = new DBXlinkGroupToCityObject(
						groupId,
						gmlId,
						isParent == 1);

				xlink.setRole(role);
				xlinkResolverPool.addWork(xlink);
			}

			if (checkRecursive && shouldRun) {
				rs.close();
				stmt.close();

				try {
					xlinkResolverPool.join();
					tmpXlinkPool.join();
				} catch (InterruptedException e) {
					//
				}

				long unresolved = tempTable.getWriterCount();
				if (unresolved > 0) {
					if (unresolved != remaining) {
						// we still have unresolved xlinks... so do another recursion
						tempTable.dropHeapView();
						tempTable.truncate(tempTable.getReader());
						queryGroupMemberXLinks(tempTable, checkRecursive, unresolved);
					} else {
						// we detected a cycle and cannot resolve the remaining xlinks
						LogMessageEvent log = new LogMessageEvent(
								"Nicht auflösbarer Zyklus in CityObjectGroup gefunden.",
								LogMessageEnum.ERROR);
						eventDispatcher.triggerEvent(log);
					}
				}
			}
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
	}

	private void appearanceXlinks() throws SQLException {
		if (!shouldRun)
			return;

		Statement stmt = null;
		ResultSet rs = null;

		try {
			if (!tempTableMgr.existsGTT(DBTempTableModelEnum.TEXTUREPARAM) && 
					!tempTableMgr.existsGTT(DBTempTableModelEnum.EXTERNAL_FILE))
				return;			

			eventDispatcher.triggerEvent(new LogMessageEvent(
					"Appearance-Xlinks werden aufgelöst...",
					LogMessageEnum.INFO));	

			// first run
			for (DBTempTableModelEnum type : DBTempTableModelEnum.values()) {
				DBTempGTT tempTable = tempTableMgr.getGTT(type);
				if (tempTable == null)
					continue;

				String query = null;
				switch (type) {
				case TEXTUREPARAM:
					tempTable.createIndexedHeapViewOfWriter();
					if (tempTableMgr.existsGTT(DBTempTableModelEnum.LINEAR_RING))
						tempTableMgr.getGTT(DBTempTableModelEnum.LINEAR_RING).createIndexedHeapViewOfWriter();

					query = "select * from " + tempTable.getTableName() + " where not TYPE=" + DBXlinkTextureParamEnum.XLINK_TEXTUREASSOCIATION.ordinal();
					break;
				case EXTERNAL_FILE:
					query = "select * from " + tempTable.getTableName();
					break;
				default:
					continue;
				}

				stmt = tempTable.getReader().createStatement();
				rs = stmt.executeQuery(query);

				while (rs.next() && shouldRun) {
					DBXlink xlink = null;

					if (type == DBTempTableModelEnum.TEXTUREPARAM) {
						long id = rs.getLong("ID");
						String gmlId = rs.getString("GMLID");
						int appType = rs.getInt("TYPE");

						// set initial context...
						xlink = new DBXlinkTextureParam(
								id,
								gmlId,
								DBXlinkTextureParamEnum.fromInt(appType));

						int isTexPara = rs.getInt("IS_TEXTURE_PARAMETERIZATION");
						if (!rs.wasNull())
							((DBXlinkTextureParam)xlink).setTextureParameterization(isTexPara != 0);

						String texParamGmlId = rs.getString("TEXPARAM_GMLID");
						if (!rs.wasNull())
							((DBXlinkTextureParam)xlink).setTexParamGmlId(texParamGmlId);

						String worldToTexture = rs.getString("WORLD_TO_TEXTURE");
						if (!rs.wasNull())
							((DBXlinkTextureParam)xlink).setWorldToTexture(worldToTexture);

						String textureCoord = rs.getString("TEXTURE_COORDINATES");
						if (!rs.wasNull())
							((DBXlinkTextureParam)xlink).setTextureCoord(textureCoord);

						String targetURI = rs.getString("TARGET_URI");
						if (!rs.wasNull())
							((DBXlinkTextureParam)xlink).setTargetURI(targetURI);

						String texCoordListId = rs.getString("TEXCOORDLIST_ID");
						if (!rs.wasNull())
							((DBXlinkTextureParam)xlink).setTexCoordListId(texCoordListId);

					}

					else if (type == DBTempTableModelEnum.EXTERNAL_FILE) {
						long id = rs.getLong("ID");
						String imageURI = rs.getString("FILE_URI");
						int dataType = rs.getInt("TYPE");

						// set initial context
						xlink = new DBXlinkExternalFile(
								id,
								imageURI,
								DBXlinkExternalFileEnum.fromInt(dataType));
					}

					if (xlink != null)
						xlinkResolverPool.addWork(xlink);
				}

				rs.close();
				stmt.close();
			}

			// restart xlink worker pools
			try {
				xlinkResolverPool.join();
				tmpXlinkPool.join();
			} catch (InterruptedException e) {
				//
			}

			if (!shouldRun)
				return;

			// second run: identifying xlinks to texture association elements...
			if (tempTableMgr.existsGTT(DBTempTableModelEnum.TEXTUREPARAM) && 
					tempTableMgr.existsGTT(DBTempTableModelEnum.TEXTUREASSOCIATION)) {
				DBTempGTT tempTable = tempTableMgr.getGTT(DBTempTableModelEnum.TEXTUREPARAM);
				tempTableMgr.getGTT(DBTempTableModelEnum.TEXTUREASSOCIATION).createIndexedHeapViewOfWriter();

				stmt = tempTable.getReader().createStatement();
				rs = stmt.executeQuery("select * from " + tempTable.getTableName() + " where TYPE=" + DBXlinkTextureParamEnum.XLINK_TEXTUREASSOCIATION.ordinal());

				while (rs.next() && shouldRun) {
					long id = rs.getLong("ID");
					String gmlId = rs.getString("GMLID");
					String targetURI = rs.getString("TARGET_URI");

					DBXlinkTextureParam xlink = new DBXlinkTextureParam(
							id,
							gmlId,
							DBXlinkTextureParamEnum.XLINK_TEXTUREASSOCIATION);

					xlink.setTargetURI(targetURI);
					xlinkResolverPool.addWork(xlink);
				}
			}

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
	}

	private void deprecatedMaterialXlinks() throws SQLException {
		if (!shouldRun)
			return;

		Statement stmt = null;
		ResultSet rs = null;

		try {
			DBTempGTT tempTable = tempTableMgr.getGTT(DBTempTableModelEnum.DEPRECATED_MATERIAL);
			if (tempTable == null)
				return;

			eventDispatcher.triggerEvent(new LogMessageEvent(
					"TexturedSurface-Xlinks werden aufgelöst...",
					LogMessageEnum.INFO));
			
			stmt = tempTable.getReader().createStatement();
			rs = stmt.executeQuery("select * from " + tempTable.getTableName());

			while (rs.next() && shouldRun) {
				long appearanceId = rs.getLong("ID");
				String gmlId = rs.getString("GMLID");
				long surfaceGeometryId = rs.getLong("SURFACE_GEOMETRY_ID");

				// set initial context
				DBXlinkDeprecatedMaterial xlink = new DBXlinkDeprecatedMaterial(
						appearanceId,
						gmlId,
						surfaceGeometryId);

				xlinkResolverPool.addWork(xlink);
			}
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
	}

	public void surfaceGeometryXlinks(boolean checkRecursive) throws SQLException {
		if (!shouldRun)
			return;
		
		DBTempGTT tempTable = tempTableMgr.getGTT(DBTempTableModelEnum.SURFACE_GEOMETRY);
		if (tempTable == null)
			return;
		
		eventDispatcher.triggerEvent(new LogMessageEvent(
				"Geometrie-Xlinks werden aufgelöst...",
				LogMessageEnum.INFO));
		
		querySurfaceGeometryXlinks(tempTable, checkRecursive, -1);
	}

	private void querySurfaceGeometryXlinks(DBTempGTT tempTable, boolean checkRecursive, long remaining) throws SQLException {
		Statement stmt = null;
		ResultSet rs = null;

		try {
			tempTable.createIndexedHeapViewOfWriter();
			tempTable.swap();

			stmt = tempTable.getReader().createStatement();
			rs = stmt.executeQuery("select * from " + tempTable.getTableName());

			while (rs.next() && shouldRun) {
				long id = rs.getLong("ID");
				long parentId = rs.getLong("PARENT_ID");
				long rootId = rs.getLong("ROOT_ID");
				boolean reverse = rs.getInt("REVERSE") == 1;
				String gmlId = rs.getString("GMLID");

				// set initial context...
				DBXlinkSurfaceGeometry xlink = new DBXlinkSurfaceGeometry(
						id,
						parentId,
						rootId,
						reverse,
						gmlId);

				xlinkResolverPool.addWork(xlink);
			}

			if (checkRecursive && shouldRun) {
				rs.close();
				stmt.close();

				try {
					xlinkResolverPool.join();
					tmpXlinkPool.join();
				} catch (InterruptedException e) {
					//
				}

				long unresolved = tempTable.getWriterCount();
				if (unresolved > 0) {
					if (unresolved != remaining) {
						// we still have unresolved xlinks... so do another recursion
						tempTable.dropHeapView();
						tempTable.truncate(tempTable.getReader());
						querySurfaceGeometryXlinks(tempTable, checkRecursive, unresolved);
					} else {
						// we detected a cycle and cannot resolve the remaining xlinks
						LogMessageEvent log = new LogMessageEvent(
								"Nicht auflösbarer Zyklus in Geometrie-Xlinks gefunden.",
								LogMessageEnum.ERROR);
						eventDispatcher.triggerEvent(log);
					}
				}
			}
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
	}
}

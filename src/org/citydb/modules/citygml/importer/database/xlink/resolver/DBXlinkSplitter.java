/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * (C) 2013 - 2015,
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
package org.citydb.modules.citygml.importer.database.xlink.resolver;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;

import org.citydb.api.concurrent.WorkerPool;
import org.citydb.api.event.Event;
import org.citydb.api.event.EventDispatcher;
import org.citydb.api.event.EventHandler;
import org.citydb.api.log.LogLevel;
import org.citydb.config.language.Language;
import org.citydb.database.TableEnum;
import org.citydb.log.Logger;
import org.citydb.modules.citygml.common.database.cache.CacheTable;
import org.citydb.modules.citygml.common.database.cache.CacheTableManager;
import org.citydb.modules.citygml.common.database.cache.model.CacheTableModelEnum;
import org.citydb.modules.citygml.common.database.xlink.DBXlink;
import org.citydb.modules.citygml.common.database.xlink.DBXlinkBasic;
import org.citydb.modules.citygml.common.database.xlink.DBXlinkDeprecatedMaterial;
import org.citydb.modules.citygml.common.database.xlink.DBXlinkGroupToCityObject;
import org.citydb.modules.citygml.common.database.xlink.DBXlinkLibraryObject;
import org.citydb.modules.citygml.common.database.xlink.DBXlinkSolidGeometry;
import org.citydb.modules.citygml.common.database.xlink.DBXlinkSurfaceDataToTexImage;
import org.citydb.modules.citygml.common.database.xlink.DBXlinkSurfaceGeometry;
import org.citydb.modules.citygml.common.database.xlink.DBXlinkTextureAssociation;
import org.citydb.modules.citygml.common.database.xlink.DBXlinkTextureCoordList;
import org.citydb.modules.citygml.common.database.xlink.DBXlinkTextureFile;
import org.citydb.modules.citygml.common.database.xlink.DBXlinkTextureParam;
import org.citydb.modules.citygml.common.database.xlink.DBXlinkTextureParamEnum;
import org.citydb.modules.common.event.EventType;
import org.citydb.modules.common.event.InterruptEvent;
import org.citydb.modules.common.event.InterruptReason;
import org.citydb.modules.common.event.StatusDialogMessage;
import org.citydb.modules.common.event.StatusDialogProgressBar;

public class DBXlinkSplitter implements EventHandler {
	private final Logger LOG = Logger.getInstance();

	private final CacheTableManager cacheTableManager;
	private final WorkerPool<DBXlink> xlinkResolverPool;
	private final WorkerPool<DBXlink> tmpXlinkPool;
	private final Object eventChannel;
	private final EventDispatcher eventDispatcher;
	private volatile boolean shouldRun = true;

	public DBXlinkSplitter(CacheTableManager cacheTableManager, 
			WorkerPool<DBXlink> xlinkResolverPool, 
			WorkerPool<DBXlink> tmpXlinkPool,
			Object eventChannel,
			EventDispatcher eventDispatcher) {
		this.cacheTableManager = cacheTableManager;
		this.xlinkResolverPool = xlinkResolverPool;
		this.tmpXlinkPool = tmpXlinkPool;
		this.eventChannel = eventChannel;
		this.eventDispatcher = eventDispatcher;

		eventDispatcher.addEventHandler(EventType.INTERRUPT, this);
	}

	public void startQuery() {
		try {
			basicXlinks();
			groupMemberXLinks(true);
			appearanceXlinks();
			libraryObjectXLinks();

			if (!shouldRun)
				return;

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
			// outer xlink. thus, we need a recursive strategy here...
			surfaceGeometryXlinks(true);

			// rebuild solid geometry objects referencing surfaces from other features
			// this requires that we have resolved surface geometry xlinks first
			solidGeometryXlinks();
		} catch (SQLException e) {
			// fire interrupt event to stop other import workers
			eventDispatcher.triggerEvent(new InterruptEvent(InterruptReason.SQL_ERROR, "Aborting import due to SQL errors.", LogLevel.WARN, e, eventChannel, this));
		} finally {
			eventDispatcher.removeEventHandler(this);
		}
	}

	private void basicXlinks() throws SQLException {
		if (!shouldRun)
			return;

		Statement stmt = null;
		ResultSet rs = null;

		try {
			CacheTable cacheTable = cacheTableManager.getCacheTable(CacheTableModelEnum.BASIC);	
			if (cacheTable == null)
				return;

			LOG.info("Resolving feature XLinks...");
			eventDispatcher.triggerEvent(new StatusDialogProgressBar(0, 0, this));
			eventDispatcher.triggerEvent(new StatusDialogMessage(Language.I18N.getString("import.dialog.basicXLink.msg"), this));

			int max = (int)cacheTable.size();
			int current = 0;

			stmt = cacheTable.getConnection().createStatement();
			rs = stmt.executeQuery("select * from " + cacheTable.getTableName());

			while (rs.next() && shouldRun) {
				eventDispatcher.triggerEvent(new StatusDialogProgressBar(++current, max, this));

				long id = rs.getLong("ID");
				int fromTable = rs.getInt("FROM_TABLE");
				String gmlId = rs.getString("GMLID");
				int toTable = rs.getInt("TO_TABLE");
				String attrName = rs.getString("ATTRNAME");

				// set initial context...
				DBXlinkBasic xlink = new DBXlinkBasic(
						id,
						TableEnum.fromInt(fromTable),
						gmlId,
						TableEnum.fromInt(toTable));

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

		CacheTable cacheTable = cacheTableManager.getCacheTable(CacheTableModelEnum.GROUP_TO_CITYOBJECT);		
		if (cacheTable == null)
			return;

		LOG.info("Resolving CityObjectGroup XLinks...");

		queryGroupMemberXLinks(cacheTable, checkRecursive, -1, 1);
	}

	private void queryGroupMemberXLinks(CacheTable cacheTable, 
			boolean checkRecursive, 
			long remaining, 
			int pass) throws SQLException {
		Statement stmt = null;
		ResultSet rs = null;

		try {					
			eventDispatcher.triggerEvent(new StatusDialogProgressBar(0, 0, this));
			String text = Language.I18N.getString("import.dialog.groupXLink.msg");
			Object[] args = new Object[]{ pass };
			eventDispatcher.triggerEvent(new StatusDialogMessage(MessageFormat.format(text, args), this));

			int max = (remaining == -1) ? (int)cacheTable.size() : (int)remaining;
			int current = 0;

			CacheTable mirrorTable = cacheTable.mirrorAndIndex();
			cacheTable.truncate();

			stmt = mirrorTable.getConnection().createStatement();
			rs = stmt.executeQuery("select * from " + mirrorTable.getTableName());

			while (rs.next() && shouldRun) {
				eventDispatcher.triggerEvent(new StatusDialogProgressBar(++current, max, this));

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

				long unresolved = cacheTable.size();
				if (unresolved > 0) {
					if (unresolved != remaining) {
						// we still have unresolved xlinks... so do another recursion
						cacheTable.dropMirrorTable();
						queryGroupMemberXLinks(cacheTable, checkRecursive, unresolved, ++pass);
					} else {
						// we detected a cycle and cannot resolve the remaining xlinks
						LOG.error("Illegal graph cycle in grouping detected. XLink references cannot be resolved.");
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
			if (!cacheTableManager.existsCacheTable(CacheTableModelEnum.TEXTURE_COORD_LIST) && 
					!cacheTableManager.existsCacheTable(CacheTableModelEnum.TEXTUREPARAM) &&
					!cacheTableManager.existsCacheTable(CacheTableModelEnum.SURFACE_DATA_TO_TEX_IMAGE))
				return;			

			LOG.info("Resolving appearance XLinks...");
			eventDispatcher.triggerEvent(new StatusDialogProgressBar(0, 0, this));
			eventDispatcher.triggerEvent(new StatusDialogMessage(Language.I18N.getString("import.dialog.appXlink.msg"), this));

			CacheTable texCoordTable = cacheTableManager.getCacheTable(CacheTableModelEnum.TEXTURE_COORD_LIST);
			CacheTable texParamTableTable = cacheTableManager.getCacheTable(CacheTableModelEnum.TEXTUREPARAM);
			boolean existsLinearRingTable = cacheTableManager.existsCacheTable(CacheTableModelEnum.LINEAR_RING);

			int max = 0, current = 0;
			if (texCoordTable != null && existsLinearRingTable) max += (int)texCoordTable.size();
			if (texParamTableTable != null) max += (int)texParamTableTable.size();

			// first step: resolve texture coordinates
			if (texCoordTable != null && existsLinearRingTable) {
				CacheTable linearRingTable = cacheTableManager.getCacheTable(CacheTableModelEnum.LINEAR_RING);
				texCoordTable.createIndexes();
				linearRingTable.createIndexes();

				stmt = texCoordTable.getConnection().createStatement();
				rs = stmt.executeQuery(new StringBuilder("select tc.ID, tc.GMLID, tc.TEXPARAM_GMLID, tc.TARGET_ID, lr.PARENT_ID, lr.REVERSE from ").append(texCoordTable.getTableName()).append(" tc ")
						.append(" join ").append(linearRingTable.getTableName()).append(" lr on tc.GMLID=lr.GMLID where lr.RING_NO = 0").toString());

				while (rs.next() && shouldRun) {
					eventDispatcher.triggerEvent(new StatusDialogProgressBar(++current, max, this));

					long id = rs.getLong("ID");
					String gmlId = rs.getString("GMLID");
					String texParamGmlId = rs.getString("TEXPARAM_GMLID");
					long targetId = rs.getLong("TARGET_ID");
					long surfaceGeometryId = rs.getLong("PARENT_ID");
					boolean reverse = rs.getBoolean("REVERSE");

					DBXlinkTextureCoordList xlink = new DBXlinkTextureCoordList(
							id,
							gmlId,
							texParamGmlId,
							targetId);

					xlink.setSurfaceGeometryId(surfaceGeometryId);
					xlink.setReverse(reverse);

					xlinkResolverPool.addWork(xlink);
				}

				rs.close();
				stmt.close();
			}

			// second step: resolve texture param other than texture coordinates
			if (texParamTableTable != null) {			
				stmt = texParamTableTable.getConnection().createStatement();
				rs = stmt.executeQuery("select * from " + texParamTableTable.getTableName());

				while (rs.next() && shouldRun) {
					eventDispatcher.triggerEvent(new StatusDialogProgressBar(++current, max, this));

					long id = rs.getLong("ID");
					String gmlId = rs.getString("GMLID");
					DBXlinkTextureParamEnum type = DBXlinkTextureParamEnum.fromInt(rs.getInt("TYPE"));
					int isTexPara = rs.getInt("IS_TEXTURE_PARAMETERIZATION");
					String texParamGmlId = rs.getString("TEXPARAM_GMLID");
					String worldToTexture = rs.getString("WORLD_TO_TEXTURE");

					// set initial context...
					DBXlinkTextureParam xlink = new DBXlinkTextureParam(
							id,
							gmlId,
							type);

					xlink.setTextureParameterization(isTexPara != 0);
					xlink.setTexParamGmlId(texParamGmlId);
					xlink.setWorldToTexture(worldToTexture);

					xlinkResolverPool.addWork(xlink);
				}

				rs.close();
				stmt.close();
			}

			if (!shouldRun)
				return;

			// third step: import texture images and world files
			if (cacheTableManager.existsCacheTable(CacheTableModelEnum.TEXTURE_FILE)) {		
				LOG.info("Importing texture images...");
				eventDispatcher.triggerEvent(new StatusDialogProgressBar(0, 0, this));
				eventDispatcher.triggerEvent(new StatusDialogMessage(Language.I18N.getString("import.dialog.texImg.msg"), this));

				CacheTable temporaryTable = cacheTableManager.getCacheTable(CacheTableModelEnum.TEXTURE_FILE);

				max = (int)temporaryTable.size();
				current = 0;

				stmt = temporaryTable.getConnection().createStatement();
				rs = stmt.executeQuery("select * from " + temporaryTable.getTableName());

				while (rs.next() && shouldRun) {
					eventDispatcher.triggerEvent(new StatusDialogProgressBar(++current, max, this));

					long id = rs.getLong("ID");
					String imageURI = rs.getString("FILE_URI");
					boolean isWorldFile = rs.getBoolean("IS_WORLD_FILE");

					xlinkResolverPool.addWork(new DBXlinkTextureFile(id, imageURI, isWorldFile));
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

			// fourth step: linking surface data to texture images
			if (cacheTableManager.existsCacheTable(CacheTableModelEnum.SURFACE_DATA_TO_TEX_IMAGE)) {

				LOG.info("Linking texture images to surface data...");
				eventDispatcher.triggerEvent(new StatusDialogProgressBar(0, 0, this));
				eventDispatcher.triggerEvent(new StatusDialogMessage(Language.I18N.getString("import.dialog.linkTexImg.msg"), this));

				CacheTable temporaryTable = cacheTableManager.getCacheTable(CacheTableModelEnum.SURFACE_DATA_TO_TEX_IMAGE);

				max = (int)temporaryTable.size();
				current = 0;

				stmt = temporaryTable.getConnection().createStatement();
				rs = stmt.executeQuery("select * from " + temporaryTable.getTableName());

				while (rs.next() && shouldRun) {
					eventDispatcher.triggerEvent(new StatusDialogProgressBar(++current, max, this));

					long fromId = rs.getLong("FROM_ID");
					long toId = rs.getLong("TO_ID");

					xlinkResolverPool.addWork(new DBXlinkSurfaceDataToTexImage(fromId, toId));
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

			// fifth step: identifying xlinks to texture association elements...
			if (cacheTableManager.existsCacheTable(CacheTableModelEnum.TEXTUREASSOCIATION) && 
					cacheTableManager.existsCacheTable(CacheTableModelEnum.TEXTUREASSOCIATION_TARGET)) {
				eventDispatcher.triggerEvent(new StatusDialogProgressBar(0, 0, this));
				eventDispatcher.triggerEvent(new StatusDialogMessage(Language.I18N.getString("import.dialog.appXlink.msg"), this));

				CacheTable cacheTable = cacheTableManager.getCacheTable(CacheTableModelEnum.TEXTUREASSOCIATION);
				cacheTableManager.getCacheTable(CacheTableModelEnum.TEXTUREASSOCIATION_TARGET).createIndexes();

				max = (int)cacheTable.size();
				current = 0;

				stmt = cacheTable.getConnection().createStatement();
				rs = stmt.executeQuery("select * from " + cacheTable.getTableName());

				while (rs.next() && shouldRun) {
					eventDispatcher.triggerEvent(new StatusDialogProgressBar(++current, max, this));

					long id = rs.getLong("ID");
					String gmlId = rs.getString("GMLID");
					String targetURI = rs.getString("TARGET_URI");

					xlinkResolverPool.addWork(new DBXlinkTextureAssociation(
							id,
							gmlId,
							targetURI));
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

	private void libraryObjectXLinks() throws SQLException {
		if (!shouldRun)
			return;

		Statement stmt = null;
		ResultSet rs = null;

		try {
			CacheTable cacheTable = cacheTableManager.getCacheTable(CacheTableModelEnum.LIBRARY_OBJECT);	
			if (cacheTable == null)
				return;

			LOG.info("Importing library objects...");
			eventDispatcher.triggerEvent(new StatusDialogProgressBar(0, 0, this));
			eventDispatcher.triggerEvent(new StatusDialogMessage(Language.I18N.getString("import.dialog.libObj.msg"), this));

			int max = (int)cacheTable.size();
			int current = 0;

			stmt = cacheTable.getConnection().createStatement();
			rs = stmt.executeQuery("select * from " + cacheTable.getTableName());

			while (rs.next() && shouldRun) {
				eventDispatcher.triggerEvent(new StatusDialogProgressBar(++current, max, this));

				long id = rs.getLong("ID");
				String imageURI = rs.getString("FILE_URI");

				// set initial context
				DBXlinkLibraryObject xlink = new DBXlinkLibraryObject(
						id,
						imageURI);

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

	private void deprecatedMaterialXlinks() throws SQLException {
		if (!shouldRun)
			return;

		Statement stmt = null;
		ResultSet rs = null;

		try {
			CacheTable cacheTable = cacheTableManager.getCacheTable(CacheTableModelEnum.DEPRECATED_MATERIAL);
			if (cacheTable == null)
				return;

			LOG.info("Resolving TexturedSurface XLinks...");
			eventDispatcher.triggerEvent(new StatusDialogProgressBar(0, 0, this));
			eventDispatcher.triggerEvent(new StatusDialogMessage(Language.I18N.getString("import.dialog.depMat.msg"), this));

			int max = (int)cacheTable.size();
			int current = 0;

			stmt = cacheTable.getConnection().createStatement();
			rs = stmt.executeQuery("select * from " + cacheTable.getTableName());

			while (rs.next() && shouldRun) {
				eventDispatcher.triggerEvent(new StatusDialogProgressBar(++current, max, this));

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

	private void surfaceGeometryXlinks(boolean checkRecursive) throws SQLException {
		if (!shouldRun)
			return;

		CacheTable cacheTable = cacheTableManager.getCacheTable(CacheTableModelEnum.SURFACE_GEOMETRY);
		if (cacheTable == null)
			return;

		LOG.info("Resolving geometry XLinks...");

		querySurfaceGeometryXlinks(cacheTable, checkRecursive, -1, 1);
	}

	private void querySurfaceGeometryXlinks(CacheTable cacheTable, 
			boolean checkRecursive, 
			long remaining, 
			int pass) throws SQLException {
		Statement stmt = null;
		ResultSet rs = null;

		try {
			eventDispatcher.triggerEvent(new StatusDialogProgressBar(0, 0, this));
			String text = Language.I18N.getString("import.dialog.geomXLink.msg");
			Object[] args = new Object[]{ pass };
			eventDispatcher.triggerEvent(new StatusDialogMessage(MessageFormat.format(text, args), this));

			int max = (remaining == -1) ? (int)cacheTable.size() : (int)remaining;
			int current = 0;

			CacheTable mirrorTable = cacheTable.mirrorAndIndex();
			cacheTable.truncate();

			stmt = mirrorTable.getConnection().createStatement();
			rs = stmt.executeQuery("select * from " + mirrorTable.getTableName());

			while (rs.next() && shouldRun) {
				eventDispatcher.triggerEvent(new StatusDialogProgressBar(++current, max, this));

				long id = rs.getLong("ID");
				long parentId = rs.getLong("PARENT_ID");
				long rootId = rs.getLong("ROOT_ID");
				boolean reverse = rs.getInt("REVERSE") == 1;
				String gmlId = rs.getString("GMLID");
				long cityObjectId = rs.getLong("CITYOBJECT_ID");
				int fromTable = rs.getInt("FROM_TABLE");
				String attrName = rs.getString("ATTRNAME");

				// set initial context...
				DBXlinkSurfaceGeometry xlink = new DBXlinkSurfaceGeometry(
						id,
						parentId,
						rootId,
						reverse,
						gmlId,
						cityObjectId,
						TableEnum.fromInt(fromTable),
						attrName);

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

				long unresolved = cacheTable.size();
				if (unresolved > 0) {
					if (unresolved != remaining) {
						// we still have unresolved xlinks... so do another recursion
						cacheTable.dropMirrorTable();
						querySurfaceGeometryXlinks(cacheTable, checkRecursive, unresolved, ++pass);
					} else {
						// we detected a cycle and cannot resolve the remaining xlinks
						LOG.error("Illegal graph cycle in geometry detected. XLink references cannot be resolved.");
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

	private void solidGeometryXlinks() throws SQLException {
		if (!shouldRun)
			return;

		Statement stmt = null;
		ResultSet rs = null;

		try {
			CacheTable cacheTable = cacheTableManager.getCacheTable(CacheTableModelEnum.SOLID_GEOMETRY);
			if (cacheTable == null)
				return;

			eventDispatcher.triggerEvent(new StatusDialogProgressBar(0, 0, this));
			eventDispatcher.triggerEvent(new StatusDialogMessage(Language.I18N.getString("import.dialog.solidXLink.msg"), this));

			int max = (int)cacheTable.size();
			int current = 0;

			stmt = cacheTable.getConnection().createStatement();
			rs = stmt.executeQuery("select * from " + cacheTable.getTableName());

			while (rs.next() && shouldRun) {
				eventDispatcher.triggerEvent(new StatusDialogProgressBar(++current, max, this));

				long id = rs.getLong("ID");

				// set initial context
				DBXlinkSolidGeometry xlink = new DBXlinkSolidGeometry(id);
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

	@Override
	public void handleEvent(Event event) throws Exception {
		if (event.getChannel() == eventChannel)
			shouldRun = false;
	}

}

/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2021
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.lrg.tum.de/gis/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * Virtual City Systems, Berlin <https://vc.systems/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Taufkirchen <http://www.moss.de/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.citydb.core.operation.importer.database.xlink.resolver;

import org.citydb.config.i18n.Language;
import org.citydb.config.project.global.LogLevel;
import org.citydb.core.operation.common.cache.CacheTable;
import org.citydb.core.operation.common.cache.CacheTableManager;
import org.citydb.core.operation.common.cache.model.CacheTableModel;
import org.citydb.core.operation.common.xlink.*;
import org.citydb.util.concurrent.WorkerPool;
import org.citydb.util.event.Event;
import org.citydb.util.event.EventDispatcher;
import org.citydb.util.event.EventHandler;
import org.citydb.util.event.global.*;
import org.citydb.util.log.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;

public class DBXlinkSplitter implements EventHandler {
	private final Logger log = Logger.getInstance();

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
			groupMemberXLinks();
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
			surfaceGeometryXlinks();

			// rebuild solid geometry objects referencing surfaces from other features
			// this requires that we have resolved surface geometry xlinks first
			solidGeometryXlinks();
		} catch (Throwable e) {
			eventDispatcher.triggerSyncEvent(new InterruptEvent("A fatal error occurred during XLink resolving.", LogLevel.ERROR, e, eventChannel));
		} finally {
			eventDispatcher.removeEventHandler(this);
		}
	}

	private void basicXlinks() throws SQLException {
		if (!shouldRun)
			return;

		CacheTable cacheTable = cacheTableManager.getCacheTable(CacheTableModel.BASIC);
		if (cacheTable == null)
			return;

		log.info("Resolving feature XLinks...");
		eventDispatcher.triggerEvent(new StatusDialogProgressBar(ProgressBarEventType.INIT, (int)cacheTable.size()));
		eventDispatcher.triggerEvent(new StatusDialogMessage(Language.I18N.getString("import.dialog.basicXLink.msg")));

		try (Statement stmt = cacheTable.getConnection().createStatement();
			 ResultSet rs = stmt.executeQuery("select * from " + cacheTable.getTableName())) {
			while (rs.next() && shouldRun) {
				eventDispatcher.triggerEvent(new StatusDialogProgressBar(ProgressBarEventType.UPDATE, 1));

				long id = rs.getLong("ID");
				String table = rs.getString("TABLE_NAME");
				String fromColumn = rs.getString("FROM_COLUMN");
				String toColumn = rs.getString("TO_COLUMN");				
				String gmlId = rs.getString("GMLID");

				// set initial context...
				xlinkResolverPool.addWork(new DBXlinkBasic(id,
						table,
						fromColumn,
						toColumn,
						gmlId));
			}
		}
	}

	private void groupMemberXLinks() throws SQLException {
		if (!shouldRun)
			return;

		CacheTable cacheTable = cacheTableManager.getCacheTable(CacheTableModel.GROUP_TO_CITYOBJECT);
		if (cacheTable == null)
			return;

		log.info("Resolving CityObjectGroup XLinks...");
		queryGroupMemberXLinks(cacheTable, true, -1, 1);
	}

	private void queryGroupMemberXLinks(CacheTable cacheTable, 
			boolean checkRecursive, 
			long remaining, 
			int pass) throws SQLException {
		eventDispatcher.triggerEvent(new StatusDialogProgressBar(ProgressBarEventType.INIT, (remaining == -1) ? (int)cacheTable.size() : (int)remaining));
		String text = Language.I18N.getString("import.dialog.groupXLink.msg");
		Object[] args = new Object[]{ pass };
		eventDispatcher.triggerEvent(new StatusDialogMessage(MessageFormat.format(text, args)));

		CacheTable mirrorTable = cacheTable.mirrorAndIndex();
		cacheTable.truncate();

		try (Statement stmt = mirrorTable.getConnection().createStatement();
			 ResultSet rs = stmt.executeQuery("select * from " + mirrorTable.getTableName())) {
			while (rs.next() && shouldRun) {
				eventDispatcher.triggerEvent(new StatusDialogProgressBar(ProgressBarEventType.UPDATE, 1));

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
		}

		if (checkRecursive && shouldRun) {
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
					log.error("Illegal graph cycle in grouping detected. XLink references cannot be resolved.");
				}
			}
		}
	}

	private void appearanceXlinks() throws SQLException {
		if (!shouldRun)
			return;

		if (!cacheTableManager.existsCacheTable(CacheTableModel.TEXTURE_COORD_LIST) &&
				!cacheTableManager.existsCacheTable(CacheTableModel.TEXTUREPARAM) &&
				!cacheTableManager.existsCacheTable(CacheTableModel.SURFACE_DATA_TO_TEX_IMAGE))
			return;

		CacheTable texCoordTable = cacheTableManager.getCacheTable(CacheTableModel.TEXTURE_COORD_LIST);
		CacheTable texParamTableTable = cacheTableManager.getCacheTable(CacheTableModel.TEXTUREPARAM);
		boolean existsLinearRingTable = cacheTableManager.existsCacheTable(CacheTableModel.LINEAR_RING);

		int max = 0;
		if (texCoordTable != null && existsLinearRingTable) max += (int) texCoordTable.size();
		if (texParamTableTable != null) max += (int) texParamTableTable.size();

		log.info("Resolving appearance XLinks...");
		eventDispatcher.triggerEvent(new StatusDialogProgressBar(ProgressBarEventType.INIT, max));
		eventDispatcher.triggerEvent(new StatusDialogMessage(Language.I18N.getString("import.dialog.appXlink.msg")));

		// first step: resolve texture coordinates
		if (texCoordTable != null && existsLinearRingTable) {
			CacheTable linearRingTable = cacheTableManager.getCacheTable(CacheTableModel.LINEAR_RING);
			texCoordTable.createIndexes();
			linearRingTable.createIndexes();

			try (Statement stmt = texCoordTable.getConnection().createStatement();
				 ResultSet rs = stmt.executeQuery("select tc.ID, tc.GMLID, tc.TEXPARAM_GMLID, tc.TARGET_ID, " +
						 "lr.PARENT_ID, lr.REVERSE from " + texCoordTable.getTableName() + " tc " +
						 " join " + linearRingTable.getTableName() + " lr on tc.GMLID=lr.GMLID where lr.RING_NO = 0")) {
				while (rs.next() && shouldRun) {
					eventDispatcher.triggerEvent(new StatusDialogProgressBar(ProgressBarEventType.UPDATE, 1));

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
			}
		}

		// second step: resolve texture param other than texture coordinates
		if (texParamTableTable != null) {
			try (Statement stmt = texParamTableTable.getConnection().createStatement();
				 ResultSet rs = stmt.executeQuery("select * from " + texParamTableTable.getTableName())) {
				while (rs.next() && shouldRun) {
					eventDispatcher.triggerEvent(new StatusDialogProgressBar(ProgressBarEventType.UPDATE, 1));

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
			}
		}

		if (!shouldRun)
			return;

		// third step: import texture images and world files
		if (cacheTableManager.existsCacheTable(CacheTableModel.TEXTURE_FILE)) {
			CacheTable temporaryTable = cacheTableManager.getCacheTable(CacheTableModel.TEXTURE_FILE);

			log.info("Importing texture images...");
			eventDispatcher.triggerEvent(new StatusDialogProgressBar(ProgressBarEventType.INIT, (int) temporaryTable.size()));
			eventDispatcher.triggerEvent(new StatusDialogMessage(Language.I18N.getString("import.dialog.texImg.msg")));

			try (Statement stmt = temporaryTable.getConnection().createStatement();
				 ResultSet rs = stmt.executeQuery("select * from " + temporaryTable.getTableName())) {
				while (rs.next() && shouldRun) {
					eventDispatcher.triggerEvent(new StatusDialogProgressBar(ProgressBarEventType.UPDATE, 1));

					long id = rs.getLong("ID");
					String imageURI = rs.getString("FILE_URI");

					xlinkResolverPool.addWork(new DBXlinkTextureFile(id, imageURI));
				}
			}
		}

		// restart xlink worker pools
		try {
			xlinkResolverPool.join();
			tmpXlinkPool.join();
		} catch (InterruptedException e) {
			//
		}

		// fourth step: linking surface data to texture images
		if (cacheTableManager.existsCacheTable(CacheTableModel.SURFACE_DATA_TO_TEX_IMAGE)) {
			CacheTable temporaryTable = cacheTableManager.getCacheTable(CacheTableModel.SURFACE_DATA_TO_TEX_IMAGE);

			log.info("Linking texture images to surface data...");
			eventDispatcher.triggerEvent(new StatusDialogProgressBar(ProgressBarEventType.INIT, (int) temporaryTable.size()));
			eventDispatcher.triggerEvent(new StatusDialogMessage(Language.I18N.getString("import.dialog.linkTexImg.msg")));

			try (Statement stmt = temporaryTable.getConnection().createStatement();
				 ResultSet rs = stmt.executeQuery("select * from " + temporaryTable.getTableName())) {
				while (rs.next() && shouldRun) {
					eventDispatcher.triggerEvent(new StatusDialogProgressBar(ProgressBarEventType.UPDATE, 1));

					long fromId = rs.getLong("FROM_ID");
					long toId = rs.getLong("TO_ID");

					xlinkResolverPool.addWork(new DBXlinkSurfaceDataToTexImage(fromId, toId));
				}
			}
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
		if (cacheTableManager.existsCacheTable(CacheTableModel.TEXTUREASSOCIATION) &&
				cacheTableManager.existsCacheTable(CacheTableModel.TEXTUREASSOCIATION_TARGET)) {
			CacheTable cacheTable = cacheTableManager.getCacheTable(CacheTableModel.TEXTUREASSOCIATION);
			cacheTableManager.getCacheTable(CacheTableModel.TEXTUREASSOCIATION_TARGET).createIndexes();

			eventDispatcher.triggerEvent(new StatusDialogProgressBar(ProgressBarEventType.INIT, (int) cacheTable.size()));
			eventDispatcher.triggerEvent(new StatusDialogMessage(Language.I18N.getString("import.dialog.appXlink.msg")));

			try (Statement stmt = cacheTable.getConnection().createStatement();
				 ResultSet rs = stmt.executeQuery("select * from " + cacheTable.getTableName())) {
				while (rs.next() && shouldRun) {
					eventDispatcher.triggerEvent(new StatusDialogProgressBar(ProgressBarEventType.UPDATE, 1));

					long id = rs.getLong("ID");
					String gmlId = rs.getString("GMLID");
					String targetURI = rs.getString("TARGET_URI");

					xlinkResolverPool.addWork(new DBXlinkTextureAssociation(
							id,
							gmlId,
							targetURI));
				}
			}
		}
	}

	private void libraryObjectXLinks() throws SQLException {
		if (!shouldRun)
			return;

		CacheTable cacheTable = cacheTableManager.getCacheTable(CacheTableModel.LIBRARY_OBJECT);
		if (cacheTable == null)
			return;

		log.info("Importing library objects...");
		eventDispatcher.triggerEvent(new StatusDialogProgressBar(ProgressBarEventType.INIT, (int)cacheTable.size()));
		eventDispatcher.triggerEvent(new StatusDialogMessage(Language.I18N.getString("import.dialog.libObj.msg")));

		try (Statement stmt = cacheTable.getConnection().createStatement();
			 ResultSet rs = stmt.executeQuery("select * from " + cacheTable.getTableName())) {
			while (rs.next() && shouldRun) {
				eventDispatcher.triggerEvent(new StatusDialogProgressBar(ProgressBarEventType.UPDATE, 1));

				long id = rs.getLong("ID");
				String imageURI = rs.getString("FILE_URI");

				// set initial context
				DBXlinkLibraryObject xlink = new DBXlinkLibraryObject(
						id,
						imageURI);

				xlinkResolverPool.addWork(xlink);
			}
		}
	}

	private void deprecatedMaterialXlinks() throws SQLException {
		if (!shouldRun)
			return;

		CacheTable cacheTable = cacheTableManager.getCacheTable(CacheTableModel.DEPRECATED_MATERIAL);
		if (cacheTable == null)
			return;

		log.info("Resolving TexturedSurface XLinks...");
		eventDispatcher.triggerEvent(new StatusDialogProgressBar(ProgressBarEventType.INIT, (int)cacheTable.size()));
		eventDispatcher.triggerEvent(new StatusDialogMessage(Language.I18N.getString("import.dialog.depMat.msg")));

		try (Statement stmt = cacheTable.getConnection().createStatement();
			 ResultSet rs = stmt.executeQuery("select * from " + cacheTable.getTableName())) {
			while (rs.next() && shouldRun) {
				eventDispatcher.triggerEvent(new StatusDialogProgressBar(ProgressBarEventType.UPDATE, 1));

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
		}
	}

	private void surfaceGeometryXlinks() throws SQLException {
		if (!shouldRun)
			return;

		CacheTable cacheTable = cacheTableManager.getCacheTable(CacheTableModel.SURFACE_GEOMETRY);
		if (cacheTable == null)
			return;

		log.info("Resolving geometry XLinks...");
		querySurfaceGeometryXlinks(cacheTable, true, -1, 1);
	}

	private void querySurfaceGeometryXlinks(CacheTable cacheTable, 
			boolean checkRecursive, 
			long remaining, 
			int pass) throws SQLException {
		eventDispatcher.triggerEvent(new StatusDialogProgressBar(ProgressBarEventType.INIT, (remaining == -1) ? (int)cacheTable.size() : (int)remaining));
		String text = Language.I18N.getString("import.dialog.geomXLink.msg");
		Object[] args = new Object[]{ pass };
		eventDispatcher.triggerEvent(new StatusDialogMessage(MessageFormat.format(text, args)));

		CacheTable mirrorTable = cacheTable.mirrorAndIndex();
		cacheTable.truncate();

		try (Statement stmt = mirrorTable.getConnection().createStatement();
			 ResultSet rs = stmt.executeQuery("select * from " + mirrorTable.getTableName())) {
			while (rs.next() && shouldRun) {
				eventDispatcher.triggerEvent(new StatusDialogProgressBar(ProgressBarEventType.UPDATE, 1));

				long id = rs.getLong("ID");
				long parentId = rs.getLong("PARENT_ID");
				long rootId = rs.getLong("ROOT_ID");
				boolean reverse = rs.getInt("REVERSE") == 1;
				String gmlId = rs.getString("GMLID");
				long cityObjectId = rs.getLong("CITYOBJECT_ID");
				String table = rs.getString("TABLE_NAME");
				String fromColumn = rs.getString("FROM_COLUMN");

				// set initial context...
				DBXlinkSurfaceGeometry xlink = new DBXlinkSurfaceGeometry(
						id,
						parentId,
						rootId,
						reverse,
						gmlId,
						cityObjectId,
						table,
						fromColumn);

				xlinkResolverPool.addWork(xlink);
			}
		}

		if (checkRecursive && shouldRun) {
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
					log.error("Illegal graph cycle in geometry detected. XLink references cannot be resolved.");
				}
			}
		}
	}

	private void solidGeometryXlinks() throws SQLException {
		if (!shouldRun)
			return;

		CacheTable cacheTable = cacheTableManager.getCacheTable(CacheTableModel.SOLID_GEOMETRY);
		if (cacheTable == null)
			return;

		eventDispatcher.triggerEvent(new StatusDialogProgressBar(ProgressBarEventType.INIT, (int)cacheTable.size()));
		eventDispatcher.triggerEvent(new StatusDialogMessage(Language.I18N.getString("import.dialog.solidXLink.msg")));

		try (Statement stmt = cacheTable.getConnection().createStatement();
			 ResultSet rs = stmt.executeQuery("select * from " + cacheTable.getTableName())) {
			while (rs.next() && shouldRun) {
				eventDispatcher.triggerEvent(new StatusDialogProgressBar(ProgressBarEventType.UPDATE, 1));

				long id = rs.getLong("ID");

				// set initial context
				DBXlinkSolidGeometry xlink = new DBXlinkSolidGeometry(id);
				xlinkResolverPool.addWork(xlink);
			}
		}
	}

	@Override
	public void handleEvent(Event event) throws Exception {
		if (event.getChannel() == eventChannel)
			shouldRun = false;
	}

}

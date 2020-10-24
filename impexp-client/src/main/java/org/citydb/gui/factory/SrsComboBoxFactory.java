/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2019
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.gis.bgu.tum.de/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
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
package org.citydb.gui.factory;

import org.citydb.config.Config;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.database.DatabaseSrs;
import org.citydb.database.connection.DatabaseConnectionPool;
import org.citydb.event.Event;
import org.citydb.event.EventHandler;
import org.citydb.event.global.EventType;
import org.citydb.plugin.extension.view.components.DatabaseSrsComboBox;
import org.citydb.registry.ObjectRegistry;

import javax.swing.*;
import java.awt.*;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class SrsComboBoxFactory {
	private static SrsComboBoxFactory instance = null;
	private final DatabaseSrs dbRefSys;
	private final List<WeakReference<SrsComboBox>> srsBoxes = new ArrayList<>();
	private final DatabaseConnectionPool dbPool;
	private final Config config;

	private SrsComboBoxFactory() {
		// just to thwart instantiation
		dbPool = DatabaseConnectionPool.getInstance();
		config = ObjectRegistry.getInstance().getConfig();
		dbRefSys = DatabaseSrs.createDefaultSrs();
		dbRefSys.setSupported(true);
	}

	public static synchronized SrsComboBoxFactory getInstance() {
		if (instance == null) {
			instance = new SrsComboBoxFactory();
			instance.resetAll(true);
		}

		return instance;
	}

	public SrsComboBox createSrsComboBox(boolean onlyShowSupported) {
		SrsComboBox srsBox = new SrsComboBox(onlyShowSupported);
		srsBox.init();

		WeakReference<SrsComboBox> ref = new WeakReference<>(srsBox);
		srsBoxes.add(ref);

		return srsBox;
	}

	public void updateAll(boolean sort) {
		processSrsComboBoxes(sort, true);
	}

	public void resetAll(boolean sort) {
		// by default, any reference system is not supported. In GUI mode we can
		// override this because the SRS combo boxes will take care.
		for (DatabaseSrs refSys : config.getProject().getDatabaseConfig().getReferenceSystems())
			refSys.setSupported(true);
		
		processSrsComboBoxes(sort, false);
	}

	private void processSrsComboBoxes(boolean sort, boolean update) {
		if (sort)
			Collections.sort(config.getProject().getDatabaseConfig().getReferenceSystems());

		Iterator<WeakReference<SrsComboBox>> iter = srsBoxes.iterator();
		while (iter.hasNext()) {
			WeakReference<SrsComboBox> ref = iter.next();
			SrsComboBox srsBox = ref.get();

			if (srsBox == null) {
				iter.remove();
				continue;
			}

			if (update)
				srsBox.updateContent();
			else
				srsBox.reset();

			srsBox.repaint();
		}
	}

	@SuppressWarnings("serial")
	public class SrsComboBox extends DatabaseSrsComboBox implements EventHandler {
		private boolean showOnlySupported;
		private boolean showOnlySameDimension;

		@SuppressWarnings("unchecked")
		private SrsComboBox(boolean onlyShowSupported) {
			this.showOnlySupported = onlyShowSupported;
			setRenderer(new SrsComboBoxRenderer(this, (ListCellRenderer<DatabaseSrs>)getRenderer()));

			ObjectRegistry.getInstance().getEventDispatcher().addEventHandler(EventType.SWITCH_LOCALE, this);
		}

		@Override
		public void setSelectedItem(Object anObject) {
			if (anObject instanceof DatabaseSrs) {
				DatabaseSrs refSys = (DatabaseSrs)anObject;

				if (refSys == dbRefSys || config.getProject().getDatabaseConfig().getReferenceSystems().contains(refSys))
					super.setSelectedItem(refSys);
				else {
					DatabaseSrs cand = null;

					for (int i = 0; i < getItemCount(); i++) {
						DatabaseSrs item = getItemAt(i);
						if (item != null) {
							if (item.getId().equals(refSys.getId())) {
								super.setSelectedItem(item);
								cand = null;
								break;
							} else if (cand == null && item.getSrid() == refSys.getSrid())
								cand = refSys;
						}
					}

					if (cand != null)
						super.setSelectedItem(cand);
				}
			}
		}

		@Override
		public void setShowOnlySupported(boolean show) {
			showOnlySupported = show;
		}

		@Override
		public void setShowOnlySameDimension(boolean show) {
			showOnlySameDimension = show;
		}

		public void setDBReferenceSystem() {
			setSelectedItem(dbRefSys);
		}

		public boolean isDBReferenceSystemSelected() {
			return getSelectedItem() == dbRefSys;
		}

		private void init() {
			addItem(dbRefSys);

			// user-defined reference systems
			for (DatabaseSrs refSys : config.getProject().getDatabaseConfig().getReferenceSystems()) {
				if (showOnlySupported && !refSys.isSupported())
					continue;

				if (showOnlySameDimension && refSys.is3D() != dbRefSys.is3D())
					continue;

				addItem(refSys);
			}
		}

		private void reset() {
			DatabaseSrs tmp = dbPool.isConnected() ? dbPool.getActiveDatabaseAdapter().getConnectionMetaData().getReferenceSystem() : DatabaseSrs.createDefaultSrs();

			dbRefSys.setSrid(tmp.getSrid());
			dbRefSys.setGMLSrsName(tmp.getGMLSrsName());
			dbRefSys.setDatabaseSrsName(tmp.getDatabaseSrsName());
			dbRefSys.setType(tmp.getType());

			removeAllItems();
			init();
		}

		private void updateContent() {
			DatabaseSrs selectedItem = getSelectedItem();
			if (selectedItem == null)
				selectedItem = dbRefSys;

			reset();
			setSelectedItem(selectedItem);
		}

		private void doTranslation() {
			dbRefSys.setDescription(Language.I18N.getString("common.label.boundingBox.crs.sameAsInDB"));
			DatabaseSrs selectedItem = getSelectedItem();
			if (selectedItem == null)
				selectedItem = dbRefSys;

			removeItemAt(0);
			insertItemAt(dbRefSys, 0);

			if (selectedItem == dbRefSys)
				setSelectedItem(selectedItem);

			repaint();
			fireActionEvent();
		}

		@Override
		public void handleEvent(Event event) throws Exception {
			doTranslation();
		}
	}

	private class SrsComboBoxRenderer implements ListCellRenderer<DatabaseSrs> {
		final DatabaseSrsComboBox box;
		final ListCellRenderer<DatabaseSrs> renderer;

		public SrsComboBoxRenderer(DatabaseSrsComboBox box, ListCellRenderer<DatabaseSrs> renderer) {
			this.box = box;
			this.renderer = renderer;
		}

		@Override
		public Component getListCellRendererComponent(JList<? extends DatabaseSrs> list, 
				DatabaseSrs value, 
				int index, 
				boolean isSelected, 
				boolean cellHasFocus) {
			Component c = renderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

			if (value != null)
				box.setToolTipText(value.toString());

			return c;
		}
	}
}

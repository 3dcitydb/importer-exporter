/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2011
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
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
package de.tub.citydb.gui.factory;

import java.awt.Component;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import de.tub.citydb.api.config.DatabaseSrs;
import de.tub.citydb.api.event.Event;
import de.tub.citydb.api.event.EventHandler;
import de.tub.citydb.api.event.global.GlobalEvents;
import de.tub.citydb.api.gui.DatabaseSrsComboBox;
import de.tub.citydb.api.registry.ObjectRegistry;
import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.database.Database;
import de.tub.citydb.database.DBConnectionPool;

public class SrsComboBoxFactory {
	private static SrsComboBoxFactory instance = null;
	private final DatabaseSrs dbRefSys = new DatabaseSrs(Database.DEFAULT_SRS);
	private final List<WeakReference<SrsComboBox>> srsBoxes = new ArrayList<WeakReference<SrsComboBox>>();
	private final Config config;

	private SrsComboBoxFactory(Config config) {
		// just to thwart instantiation
		this.config = config;
	}

	public static synchronized SrsComboBoxFactory getInstance(Config config) {
		if (instance == null) {
			instance = new SrsComboBoxFactory(config);
			Collections.sort(config.getProject().getDatabase().getReferenceSystems());
		}

		return instance;
	}

	public SrsComboBox createSrsComboBox(boolean onlyShowSupported) {
		SrsComboBox srsBox = new SrsComboBox(onlyShowSupported);
		srsBox.init();

		WeakReference<SrsComboBox> ref = new WeakReference<SrsComboBox>(srsBox);
		srsBoxes.add(ref);

		return srsBox;
	}

	public void updateAll(boolean sort) {
		if (sort)
			Collections.sort(config.getProject().getDatabase().getReferenceSystems());

		Iterator<WeakReference<SrsComboBox>> iter = srsBoxes.iterator();
		while (iter.hasNext()) {
			WeakReference<SrsComboBox> ref = iter.next();
			SrsComboBox srsBox = ref.get();

			if (srsBox == null) {
				iter.remove();
				continue;
			}

			srsBox.updateContent();
			srsBox.repaint();
		}
	}

	@SuppressWarnings("serial")
	public class SrsComboBox extends DatabaseSrsComboBox implements EventHandler {
		private final boolean onlyShowSupported;

		private SrsComboBox(boolean onlyShowSupported) {
			this.onlyShowSupported = onlyShowSupported;
			setRenderer(new SrsComboBoxRenderer(this));

			ObjectRegistry.getInstance().getEventDispatcher().addEventHandler(GlobalEvents.SWITCH_LOCALE, this);
		}

		@Override
		public void setSelectedItem(Object anObject) {
			if (anObject instanceof DatabaseSrs) {
				DatabaseSrs refSys = (DatabaseSrs)anObject;

				if (refSys == dbRefSys || config.getProject().getDatabase().getReferenceSystems().contains(refSys))
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

		public boolean isDBReferenceSystemSelected() {
			return getSelectedItem() == dbRefSys;
		}

		private void init() {
			addItem(dbRefSys);

			// user-defined reference systems
			for (DatabaseSrs refSys : config.getProject().getDatabase().getReferenceSystems())
				if (!onlyShowSupported || refSys.isSupported())
					addItem(refSys);
		}

		private void updateContent() {
			DBConnectionPool dbPool = DBConnectionPool.getInstance();

			if (dbPool.isConnected()) {
				dbRefSys.setSrid(dbPool.getActiveConnection().getMetaData().getSrid());
				dbRefSys.setSrsName(dbPool.getActiveConnection().getMetaData().getSrsName());
			} else {
				dbRefSys.setSrid(Database.DEFAULT_SRS.getSrid());
				dbRefSys.setSrsName(Database.DEFAULT_SRS.getSrsName());				
			}		

			DatabaseSrs selectedItem = getSelectedItem();
			if (selectedItem == null)
				selectedItem = dbRefSys;

			removeAllItems();
			addItem(dbRefSys);

			// user-defined reference systems
			for (DatabaseSrs refSys : config.getProject().getDatabase().getReferenceSystems())
				if (!onlyShowSupported || refSys.isSupported())
					addItem(refSys);

			setSelectedItem(selectedItem);
		}

		private void doTranslation() {
			dbRefSys.setDescription(Internal.I18N.getString("common.label.boundingBox.crs.sameAsInDB"));
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

	@SuppressWarnings("serial")
	private class SrsComboBoxRenderer extends DefaultListCellRenderer {
		final SrsComboBox box;

		public SrsComboBoxRenderer(SrsComboBox box) {
			this.box = box;
		}

		@Override
		public Component getListCellRendererComponent(JList list, 
				Object value, 
				int index, 
				boolean isSelected, 
				boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

			if (value != null)
				box.setToolTipText(value.toString());

			return this;
		}
	}
}

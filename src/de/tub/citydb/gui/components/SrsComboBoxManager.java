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
package de.tub.citydb.gui.components;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import de.tub.citydb.api.gui.DatabaseSrsComboBox;
import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.database.ReferenceSystem;
import de.tub.citydb.database.DBConnectionPool;

public class SrsComboBoxManager {
	private static SrsComboBoxManager instance = null;
	private final ReferenceSystem dbRefSys = new ReferenceSystem(ReferenceSystem.DEFAULT);
	private final List<WeakReference<SrsComboBox>> srsBoxes = new ArrayList<WeakReference<SrsComboBox>>();
	private final Config config;

	private SrsComboBoxManager(Config config) {
		// just to thwart instantiation
		this.config = config;
	}

	public static synchronized SrsComboBoxManager getInstance(Config config) {
		if (instance == null) {
			instance = new SrsComboBoxManager(config);
			Collections.sort(config.getProject().getDatabase().getReferenceSystems());
		}

		return instance;
	}

	public SrsComboBox getSrsComboBox(boolean onlyShowSupported) {
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
	
	public void translateAll() {
		Iterator<WeakReference<SrsComboBox>> iter = srsBoxes.iterator();
		while (iter.hasNext()) {
			WeakReference<SrsComboBox> ref = iter.next();
			SrsComboBox srsBox = ref.get();
			
			if (srsBox == null) {
				iter.remove();
				continue;
			}
			
			srsBox.doTranslation();
		}
	}

	@SuppressWarnings("serial")
	public class SrsComboBox extends DatabaseSrsComboBox {
		private final boolean onlyShowSupported;

		private SrsComboBox(boolean onlyShowSupported) {
			this.onlyShowSupported = onlyShowSupported;
		}

		@Override
		public ReferenceSystem getSelectedItem() {
			Object object = super.getSelectedItem();
			return (object instanceof ReferenceSystem) ? (ReferenceSystem)object : null;
		}

		@Override
		public void addItem(Object anObject) {
			if (anObject instanceof ReferenceSystem)
				super.addItem(anObject);
		}
		
		public boolean isDBReferenceSystemSelected() {
			return getSelectedItem() == dbRefSys;
		}
		
		private void init() {
			addItem(dbRefSys);

			// user-defined reference systems
			for (ReferenceSystem refSys : config.getProject().getDatabase().getReferenceSystems())
				if (!onlyShowSupported || refSys.isSupported())
					addItem(refSys);
		}

		private void updateContent() {
			DBConnectionPool dbPool = DBConnectionPool.getInstance();
			
			if (dbPool.isConnected()) {
				dbRefSys.setSrid(dbPool.getActiveConnection().getMetaData().getSrid());
				dbRefSys.setSrsName(dbPool.getActiveConnection().getMetaData().getSrsName());
			} else {
				dbRefSys.setSrid(ReferenceSystem.DEFAULT.getSrid());
				dbRefSys.setSrsName(ReferenceSystem.DEFAULT.getSrsName());				
			}		
			
			ReferenceSystem selectedItem = getSelectedItem();
			if (selectedItem == null)
				selectedItem = dbRefSys;

			removeAllItems();
			addItem(dbRefSys);

			// user-defined reference systems
			for (ReferenceSystem refSys : config.getProject().getDatabase().getReferenceSystems())
				if (!onlyShowSupported || refSys.isSupported())
					addItem(refSys);

			setSelectedItem(selectedItem);
		}

		private void doTranslation() {
			dbRefSys.setDescription(Internal.I18N.getString("common.label.boundingBox.crs.sameAsInDB"));
			init();
			repaint();
			fireActionEvent();
		}
	}
}

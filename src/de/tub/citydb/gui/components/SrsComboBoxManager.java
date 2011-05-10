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

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.database.ReferenceSystem;

public class SrsComboBoxManager {
	private static SrsComboBoxManager instance = null;
	private final ReferenceSystem dbRefSys = new ReferenceSystem(Internal.DEFAULT_DB_REF_SYS);
	private final List<SrsComboBox> srsBoxes = new ArrayList<SrsComboBox>();
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
		srsBoxes.add(srsBox);
		return srsBox;
	}
	
	public SrsComboBox getSrsComboBox(boolean onlyShowSupported, int maxLimit) {
		SrsComboBox srsBox = new SrsComboBox(onlyShowSupported, maxLimit);
		srsBoxes.add(srsBox);
		return srsBox;
	}

	public void updateAll(boolean sort) {
		if (sort)
			Collections.sort(config.getProject().getDatabase().getReferenceSystems());

		for (SrsComboBox srsBox : srsBoxes) {
			srsBox.updateContent();
			srsBox.repaint();
		}
	}
	
	public void translateAll() {
		for (SrsComboBox srsBox : srsBoxes)
			srsBox.doTranslation();
	}

	@SuppressWarnings("serial")
	public class SrsComboBox extends JComboBox {
		private final boolean onlyShowSupported;
		private int maxLimit = Integer.MAX_VALUE;

		private SrsComboBox(boolean onlyShowSupported) {
			this.onlyShowSupported = onlyShowSupported;
			setRenderer(new SrsComboBoxRenderer(this));
		}
		
		private SrsComboBox(boolean onlyShowSupported, int maxLimit) {
			this(onlyShowSupported);
			this.maxLimit = maxLimit;
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

		public void updateContent() {
			if (config.getInternal().isConnected()) {
				dbRefSys.setSrid(config.getInternal().getOpenConnection().getMetaData().getSrid());
				dbRefSys.setSrsName(config.getInternal().getOpenConnection().getMetaData().getSrsName());
			} else {
				dbRefSys.setSrid(Internal.DEFAULT_DB_REF_SYS.getSrid());
				dbRefSys.setSrsName(Internal.DEFAULT_DB_REF_SYS.getSrsName());				
			}		
			
			ReferenceSystem selectedItem = getSelectedItem();
			if (selectedItem == null)
				selectedItem = dbRefSys;

			removeAllItems();

			// default reference systems
			addItem(dbRefSys);

			// user-defined reference systems
			for (ReferenceSystem refSys : config.getProject().getDatabase().getReferenceSystems())
				if (!onlyShowSupported || refSys.isSupported())
					addItem(refSys);

			setSelectedItem(selectedItem);
		}

		private void doTranslation() {
			dbRefSys.setDescription(Internal.I18N.getString("common.label.boundingBox.crs.sameAsInDB"));
			updateContent();
			repaint();
			fireActionEvent();
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
			Component component =  super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if (value != null && value.toString().length() > box.maxLimit && component instanceof JLabel)
				((JLabel)component).setText(value.toString().substring(0, box.maxLimit) + "...");
			
			box.setToolTipText(value.toString());
			return component;
		}
	}
}

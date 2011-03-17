package de.tub.citydb.gui.components;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JComboBox;

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

	public void updateAll(boolean sort) {
		if (sort)
			Collections.sort(config.getProject().getDatabase().getReferenceSystems());

		for (SrsComboBox srsBox : srsBoxes) {
			srsBox.updateContent();
			srsBox.repaint();
		}
	}

	@SuppressWarnings("serial")
	public class SrsComboBox extends JComboBox {
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

		public void doTranslation() {
			dbRefSys.setDescription(Internal.I18N.getString("common.label.boundingBox.crs.sameAsInDB"));
			updateContent();
			repaint();
			fireActionEvent();
		}
	}
}

package de.tub.citydb.gui.components;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JComboBox;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.project.database.ReferenceSystem;

public class SrsComboBoxManager {
	private static SrsComboBoxManager instance = null;
	private final Config config;
	private final List<SrsComboBox> srsBoxes = new ArrayList<SrsComboBox>();

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

		public void updateContent() {
			ReferenceSystem selectedItem = getSelectedItem();
			if (selectedItem == null)
				selectedItem = ReferenceSystem.SAME_AS_IN_DB;

			removeAllItems();

			// default reference systems
			addItem(ReferenceSystem.SAME_AS_IN_DB);

			// user-defined reference systems
			for (ReferenceSystem refSys : config.getProject().getDatabase().getReferenceSystems())
				if (!onlyShowSupported || refSys.isSupported())
					addItem(refSys);

			setSelectedItem(selectedItem);
		}

		public void doTranslation() {
			repaint();
			fireActionEvent();
		}
	}
}

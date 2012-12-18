package de.tub.citydb.modules.citygml.exporter.gui.preferences;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.exporter.AddressMode;
import de.tub.citydb.config.project.exporter.ExportAddress;
import de.tub.citydb.gui.preferences.AbstractPreferencesComponent;
import de.tub.citydb.util.gui.GuiUtil;

@SuppressWarnings("serial")
public class AddressPanel extends AbstractPreferencesComponent {
	private JPanel exportXALPanel;
	private JRadioButton exportXAL;
	private JRadioButton exportDB;
	private JCheckBox exportFallback;

	public AddressPanel(Config config) {
		super(config);
		initGui();
	}

	@Override
	public boolean isModified() {
		ExportAddress address = config.getProject().getExporter().getAddress();
		
		if (exportDB.isSelected() && address.getMode() != AddressMode.DB) return true;
		if (exportXAL.isSelected() && address.getMode() != AddressMode.XAL) return true;
		if (exportFallback.isSelected() != address.isSetUseFallback()) return true;
		
		return false;
	}

	private void initGui() {
		exportXAL = new JRadioButton();
		exportXAL.setIconTextGap(10);
		exportDB = new JRadioButton();
		exportDB.setIconTextGap(10);
		ButtonGroup exportGroup = new ButtonGroup();
		exportGroup.add(exportXAL);
		exportGroup.add(exportDB);
		exportFallback = new JCheckBox();
		exportFallback.setIconTextGap(10);

		setLayout(new GridBagLayout());
		{
			exportXALPanel = new JPanel();
			add(exportXALPanel, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));
			exportXALPanel.setBorder(BorderFactory.createTitledBorder(""));
			exportXALPanel.setLayout(new GridBagLayout());
			{
				exportXALPanel.add(exportDB, GuiUtil.setConstraints(0,0,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
				exportXALPanel.add(exportXAL, GuiUtil.setConstraints(0,1,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
				exportXALPanel.add(exportFallback, GuiUtil.setConstraints(0,2,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
			}
		}
	}

	@Override
	public void setSettings() {
		ExportAddress address = config.getProject().getExporter().getAddress();
		
		address.setMode(exportDB.isSelected() ? AddressMode.DB : AddressMode.XAL);
		address.setUseFallback(exportFallback.isSelected());
	}

	@Override
	public void loadSettings() {
		ExportAddress address = config.getProject().getExporter().getAddress();
		
		switch (address.getMode()) {
		case XAL:
			exportXAL.setSelected(true);
			break;
		default:
			exportDB.setSelected(true);
		}
		
		exportFallback.setSelected(address.isSetUseFallback());
	}

	@Override
	public void doTranslation() {
		((TitledBorder)exportXALPanel.getBorder()).setTitle(Internal.I18N.getString("pref.export.address.border.export"));	
		exportXAL.setText(Internal.I18N.getString("pref.export.address.label.exportXAL"));
		exportDB.setText(Internal.I18N.getString("pref.export.address.label.exportDB"));
		exportFallback.setText(Internal.I18N.getString("pref.export.address.label.exportFallback"));
	}

	@Override
	public String getTitle() {
		return Internal.I18N.getString("pref.tree.export.address");
	}

}

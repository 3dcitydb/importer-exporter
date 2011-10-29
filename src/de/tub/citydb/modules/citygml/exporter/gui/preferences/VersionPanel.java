package de.tub.citydb.modules.citygml.exporter.gui.preferences;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;

import org.citygml4j.model.module.citygml.CityGMLVersion;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.exporter.CityGMLVersionType;
import de.tub.citydb.gui.preferences.AbstractPreferencesComponent;
import de.tub.citydb.util.gui.GuiUtil;

@SuppressWarnings("serial")
public class VersionPanel extends AbstractPreferencesComponent {
	private JPanel block1;
	private JRadioButton[] cityGMLVersionBox;

	public VersionPanel(Config config) {
		super(config);
		initGui();
	}

	@Override
	public boolean isModified() {
		CityGMLVersionType version = config.getProject().getExporter().getCityGMLVersion();
		
		for (int i = 0; i < CityGMLVersionType.values().length; i++)
			if (cityGMLVersionBox[i].isSelected())
				return version != CityGMLVersionType.fromValue(cityGMLVersionBox[i].getText());

		return false;
	}

	private void initGui() {
		ButtonGroup group = new ButtonGroup();
		cityGMLVersionBox = new JRadioButton[CityGMLVersionType.values().length];

		for (int i = 0; i < CityGMLVersionType.values().length; i++) {			
			cityGMLVersionBox[i] = new JRadioButton();
			cityGMLVersionBox[i].setText(CityGMLVersionType.values()[i].toString());
			cityGMLVersionBox[i].setIconTextGap(10);
			group.add(cityGMLVersionBox[i]);
			
			if (CityGMLVersionType.values()[i].toCityGMLVersion() == CityGMLVersion.DEFAULT)
				cityGMLVersionBox[i].setSelected(true);
		}

		setLayout(new GridBagLayout());
		block1 = new JPanel();
		add(block1, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));
		block1.setBorder(BorderFactory.createTitledBorder(""));
		block1.setLayout(new GridBagLayout());
		{
			for (int i = 0; i < cityGMLVersionBox.length; i++)
				block1.add(cityGMLVersionBox[i], GuiUtil.setConstraints(0,i,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));										
		}
	}

	@Override
	public void doTranslation() {
		((TitledBorder)block1.getBorder()).setTitle(Internal.I18N.getString("pref.export.version.border.versions"));	
	}

	@Override
	public void loadSettings() {
		CityGMLVersionType version = config.getProject().getExporter().getCityGMLVersion();
		for (int i = 0; i < CityGMLVersionType.values().length; i++) {
			if (CityGMLVersionType.values()[i].toCityGMLVersion() == version.toCityGMLVersion()) {
				cityGMLVersionBox[i].setSelected(true);
				break;
			}
		}
	}

	@Override
	public void setSettings() {
		for (int i = 0; i < CityGMLVersionType.values().length; i++) {
			if (cityGMLVersionBox[i].isSelected()) {
				config.getProject().getExporter().setCityGMLVersion(CityGMLVersionType.fromValue(cityGMLVersionBox[i].getText()));
				break;
			}
		}
	}
	
	@Override
	public String getTitle() {
		return Internal.I18N.getString("pref.tree.export.version");
	}

}

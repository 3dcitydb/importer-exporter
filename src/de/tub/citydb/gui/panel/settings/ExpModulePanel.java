package de.tub.citydb.gui.panel.settings;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.citygml4j.model.citygml.CityGMLModule;
import org.citygml4j.model.citygml.CityGMLModuleType;
import org.citygml4j.model.citygml.CityGMLModuleVersion;
import org.citygml4j.util.CityGMLModules;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.exporter.ModuleVersion;
import de.tub.citydb.gui.util.GuiUtil;

@SuppressWarnings("serial")
public class ExpModulePanel extends PrefPanelBase {
	private JPanel block1;
	private JPanel block2;
	private JButton cityGML040Button;
	private JButton cityGML100Button;
	private HashMap<CityGMLModuleType, JComboBox> comboMap;

	public ExpModulePanel(Config config) {
		super(config);

		comboMap = new HashMap<CityGMLModuleType, JComboBox>();
		initGui();
	}

	private void initGui() {		
		block1 = new JPanel();
		block2 = new JPanel();
		cityGML040Button = new JButton("v" + CityGMLModuleVersion.v0_4_0);
		cityGML100Button = new JButton("v" + CityGMLModuleVersion.v1_0_0);
		
		cityGML040Button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVersion(CityGMLModuleVersion.v0_4_0);
			}
		});
		
		cityGML100Button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVersion(CityGMLModuleVersion.v1_0_0);
			}
		});
		
		//Layout
		setLayout(new GridBagLayout());

		add(block1, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));
		block1.setBorder(BorderFactory.createTitledBorder(""));
		block1.setLayout(new GridBagLayout());
		{
			int i = 0;
			for (CityGMLModuleType moduleName : CityGMLModuleType.values()) {
				JLabel label = new JLabel(moduleName.value());
				JComboBox comboBox = new JComboBox();
				comboMap.put(moduleName, comboBox);

				block1.add(label, GuiUtil.setConstraints(0,i,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
				block1.add(comboBox, GuiUtil.setConstraints(1,i++,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));										
			}
		}

		add(block2, GuiUtil.setConstraints(0,1,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));
		block2.setBorder(BorderFactory.createTitledBorder(""));
		block2.setLayout(new GridBagLayout());
		{
			block2.add(cityGML040Button, GuiUtil.setConstraints(0,0,0.0,1.0,GridBagConstraints.BOTH,5,5,5,5));
			block2.add(cityGML100Button, GuiUtil.setConstraints(1,0,0.0,1.0,GridBagConstraints.BOTH,5,5,5,5));
		}

	}

	@Override
	public boolean isModified() {
		for (CityGMLModule module : config.getProject().getExporter().getModuleVersion().getModules())
			if (module.getModuleVersion() != (CityGMLModuleVersion)comboMap.get(module.getModuleType()).getSelectedItem())
				return true;

		return false;
	}

	@Override
	public void doTranslation() {
		block1.setBorder(BorderFactory.createTitledBorder(Internal.I18N.getString("pref.export.module.border.versions")));		
		block2.setBorder(BorderFactory.createTitledBorder(Internal.I18N.getString("pref.export.module.border.quickSelect")));		
	}

	@Override
	public void loadSettings() {
		List<CityGMLModule> moduleConfig = config.getProject().getExporter().getModuleVersion().getModules();

		Iterator<Entry<CityGMLModuleType, JComboBox>> iter = comboMap.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<CityGMLModuleType, JComboBox> mapEntry = iter.next();			
			JComboBox comboBox = mapEntry.getValue();
			CityGMLModuleType type = mapEntry.getKey();

			comboBox.removeAllItems();
			for (CityGMLModuleVersion version : CityGMLModules.getModuleVersion(type))
				comboBox.addItem(version);

			for (CityGMLModule module : moduleConfig) {
				if (module.getModuleType().equals(type)) {
					comboBox.setSelectedItem(module.getModuleVersion());
					break;
				}
			}
		}
	}

	@Override
	public void setSettings() {
		ModuleVersion moduleVersion = config.getProject().getExporter().getModuleVersion();

		Iterator<Entry<CityGMLModuleType, JComboBox>> iter = comboMap.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<CityGMLModuleType, JComboBox> mapEntry = iter.next();			
			JComboBox comboBox = mapEntry.getValue();
			CityGMLModuleType moduleName = mapEntry.getKey();

			moduleVersion.setModuleVersion(moduleName, (CityGMLModuleVersion)comboBox.getSelectedItem());
		}
	}
	
	private void setVersion(CityGMLModuleVersion version) {
		for (JComboBox comboBox : comboMap.values())
			comboBox.setSelectedItem(version);
	}
}

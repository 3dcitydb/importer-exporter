package de.tub.citydb.gui.panel.matching;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.gui.util.GuiUtil;

public class MatchingPanel extends JPanel implements PropertyChangeListener {	
	private final Config config;

	private JComboBox lodRef1Combo;
	private JComboBox lodMer1Combo;
	private JTextField lineage1Text;
	private JCheckBox tableCheck;
	private JButton matchButton;

	private JFormattedTextField percentageAText;
	private JFormattedTextField percentageBText;
	private JComboBox lodRef2Combo;
	private JComboBox lodMer2Combo;
	private JTextField lineage2Text;
	private JButton mergeButton;

	private JTextField lineage3Text;
	private JButton deleteButton;

	private JPanel row1;
	private JLabel row1_1;
	private JLabel row1_2;
	private JLabel row1_3;
	private JPanel row2;
	private JLabel row2_1;
	private JLabel row2_2;
	private JLabel row2_3;
	private JLabel row2_4;
	private JLabel row2_5;
	private JLabel row2_6;
	private JPanel row3;
	private JLabel row3_1;
	private JLabel settingsAText;
	private JLabel settingsBText;

	public MatchingPanel(Config config) {
		this.config = config;
		
		config.getProject().getMatching().getMatchingSettings().addPropertyChangeListener(this);
		initGui();
	}

	private void initGui() {
		//gui-elemente anlegen
		lodRef1Combo = new JComboBox();
		lodMer1Combo = new JComboBox();
		lineage1Text = new JTextField("");
		tableCheck = new JCheckBox("");
		matchButton = new JButton("");

		percentageAText = new JFormattedTextField();
		percentageBText = new JFormattedTextField();
		percentageAText.setValue(new Float(0.8));
		percentageBText.setValue(new Float(0.8));
		percentageAText.setColumns(5);
		percentageBText.setColumns(5);
		lodRef2Combo = new JComboBox();
		lodMer2Combo = new JComboBox();
		lineage2Text = new JTextField("");
		mergeButton = new JButton("");

		lineage3Text = new JTextField("");
		deleteButton = new JButton("");

		lodRef1Combo.addItem("LOD 1");
		lodRef1Combo.addItem("LOD 2");
		lodRef1Combo.addItem("LOD 3");
		lodRef1Combo.addItem("LOD 4");
		lodMer1Combo.addItem("LOD 1");
		lodMer1Combo.addItem("LOD 2");
		lodMer1Combo.addItem("LOD 3");
		lodMer1Combo.addItem("LOD 4");
		lodRef2Combo.addItem("LOD 1");
		lodRef2Combo.addItem("LOD 2");
		lodRef2Combo.addItem("LOD 3");
		lodRef2Combo.addItem("LOD 4");
		lodRef2Combo.setSelectedIndex(2);
		lodMer2Combo.addItem("LOD 1");
		lodMer2Combo.addItem("LOD 2");
		lodMer2Combo.addItem("LOD 3");
		lodMer2Combo.addItem("LOD 4");

		lodMer1Combo.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent ie) {
				lodMer2Combo.setSelectedIndex(lodMer1Combo.getSelectedIndex());
			}
		});

		percentageAText.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				checkValue(percentageAText);
			}
		});

		percentageBText.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				checkValue(percentageBText);
			}
		});

		//layout
		setLayout(new GridBagLayout());
		{
			row1 = new JPanel();
			add(row1, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.HORIZONTAL,10,5,5,5));
			row1.setBorder(BorderFactory.createTitledBorder(""));
			row1.setLayout(new GridBagLayout());
			row1_1 = new JLabel();
			row1_2 = new JLabel();
			row1_3 = new JLabel();

			{
				row1.add(row1_1, GuiUtil.setConstraints(0,0,0.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
				GridBagConstraints c = GuiUtil.setConstraints(1,0,0.0,0.0,GridBagConstraints.NONE,0,5,5,5);
				c.anchor = GridBagConstraints.WEST;
				row1.add(lodRef1Combo, c);
				
				row1.add(row1_2, GuiUtil.setConstraints(0,1,0.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
				c = GuiUtil.setConstraints(1,1,0.0,0.0,GridBagConstraints.NONE,0,5,5,5);
				c.anchor = GridBagConstraints.WEST;
				row1.add(lodMer1Combo, c);
				
				row1.add(row1_3, GuiUtil.setConstraints(0,2,0.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
				row1.add(lineage1Text, GuiUtil.setConstraints(1,2,1.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
				
				tableCheck.setIconTextGap(10);
				c = GuiUtil.setConstraints(0,3,0.0,0.0,GridBagConstraints.BOTH,0,5,5,5);
				c.gridwidth = 2;
				c.anchor = GridBagConstraints.WEST;
				row1.add(tableCheck, c);

				c = GuiUtil.setConstraints(0,4,1.0,0.0,GridBagConstraints.NONE,10,5,5,5);
				c.gridwidth = 2;
				row1.add(matchButton,c);
			}
		}
		{
			row2 = new JPanel();
			add(row2, GuiUtil.setConstraints(0,1,1.0,0.0,GridBagConstraints.HORIZONTAL,5,5,5,5));
			row2.setBorder(BorderFactory.createTitledBorder(""));
			row2.setLayout(new GridBagLayout());
			row2_1 = new JLabel();
			row2_2 = new JLabel();
			row2_3 = new JLabel();
			row2_4 = new JLabel();
			row2_5 = new JLabel();
			row2_6 = new JLabel();
			{
				row2.add(row2_1, GuiUtil.setConstraints(0,0,0.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
				GridBagConstraints c = GuiUtil.setConstraints(1,0,0.0,0.0,GridBagConstraints.NONE,0,5,5,5);
				c.anchor = GridBagConstraints.WEST;
				row2.add(percentageAText, c);
				
				row2.add(row2_2, GuiUtil.setConstraints(0,1,0.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
				c = GuiUtil.setConstraints(1,1,0.0,0.0,GridBagConstraints.NONE,0,5,5,5);
				c.anchor = GridBagConstraints.WEST;
				row2.add(percentageBText, c);
				
				row2.add(row2_3, GuiUtil.setConstraints(0,2,0.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
				c = GuiUtil.setConstraints(1,2,0.0,0.0,GridBagConstraints.NONE,0,5,5,5);
				c.anchor = GridBagConstraints.WEST;
				row2.add(lodRef2Combo, c);
				
				row2.add(row2_4, GuiUtil.setConstraints(0,3,0.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
				c = GuiUtil.setConstraints(1,3,1.0,0.0,GridBagConstraints.NONE,0,5,5,5);
				c.anchor = GridBagConstraints.WEST;
				row2.add(lodMer2Combo, c);
				
				row2.add(row2_5, GuiUtil.setConstraints(0,4,0.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
				row2.add(lineage2Text, GuiUtil.setConstraints(1,4,1.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
				
				row2.add(row2_6, GuiUtil.setConstraints(0,5,0.0,0.0,GridBagConstraints.BOTH,10,5,0,5));
				
				JPanel settings = new JPanel();
				settings.setBorder(BorderFactory.createEtchedBorder());
				settings.setBackground(new Color(255, 255, 255));
				settings.setLayout(new GridBagLayout());
				
				c = GuiUtil.setConstraints(0,6,1.0,0.0,GridBagConstraints.BOTH,0,5,5,5);
				c.anchor = GridBagConstraints.WEST;
				c.gridwidth = 2;
				row2.add(settings, c);
				
				settingsAText = new JLabel();
				settingsBText = new JLabel();
				{
					settings.add(settingsAText, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,5,5,5,5));
					settings.add(settingsBText, GuiUtil.setConstraints(0,1,1.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
				}
				
				c = new GridBagConstraints();
				c = GuiUtil.setConstraints(0,7,0.0,0.0,GridBagConstraints.NONE,10,5,5,5);
				c.gridwidth = 2;
				row2.add(mergeButton,c);
			}
		}
		{
			row3 = new JPanel();
			add(row3, GuiUtil.setConstraints(0,2,1.0,0.0,GridBagConstraints.HORIZONTAL,5,5,5,5));
			row3.setBorder(BorderFactory.createTitledBorder(""));
			row3.setLayout(new GridBagLayout());
			row3_1 = new JLabel();
			{
				row3.add(row3_1, GuiUtil.setConstraints(0,0,0.0,0.0,GridBagConstraints.NONE,0,5,5,5));
				row3.add(lineage3Text, GuiUtil.setConstraints(1,0,1.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
				row3.add(deleteButton, GuiUtil.setConstraints(2,0,0.0,0.0,GridBagConstraints.NONE,0,5,5,5));
			}
		}
		
		JPanel panel = new JPanel();
		add(panel, GuiUtil.setConstraints(0,3,1.0,1.0,GridBagConstraints.BOTH,5,5,5,5));
		
		lineageVisibility();
	}

	public void doTranslation() {
		//internationalisierte Labels und Strings
		row1.setBorder(BorderFactory.createTitledBorder(Internal.I18N.getString("match.step1.border")));
		row1_1.setText(Internal.I18N.getString("match.step1.lodRef1"));
		row1_2.setText(Internal.I18N.getString("match.step1.lodMer1"));
		row1_3.setText(Internal.I18N.getString("match.step1.lineage"));
		tableCheck.setText(Internal.I18N.getString("match.step1.tableCheck"));
		matchButton.setText(Internal.I18N.getString("match.step1.button"));

		row2.setBorder(BorderFactory.createTitledBorder(Internal.I18N.getString("match.step2.border")));
		row2_1.setText(Internal.I18N.getString("match.step2.overlapOfMer"));
		row2_2.setText(Internal.I18N.getString("match.step2.overlapOfRef"));
		row2_3.setText(Internal.I18N.getString("match.step2.lodRef2"));
		row2_4.setText(Internal.I18N.getString("match.step2.lodMer2"));
		row2_5.setText(Internal.I18N.getString("match.step2.lineage"));
		lineage2Text.setToolTipText(Internal.I18N.getString("match.step2.tooltip"));
		
		row2_6.setText(Internal.I18N.getString("match.step2.setting"));
		createSettingText();
		
		mergeButton.setText(Internal.I18N.getString("match.step2.button"));

		row3.setBorder(BorderFactory.createTitledBorder(Internal.I18N.getString("match.tools.border")));
		row3_1.setText(Internal.I18N.getString("match.tools.lineage"));
		deleteButton.setText(Internal.I18N.getString("match.tools.delete"));
	}

	public void lineageVisibility() {
		lineage2Text.setEnabled(config.getProject().getMatching().getMatchingSettings().isDeleteModeRename());	
	}

	public void createSettingText() {
		String prefixA = Internal.I18N.getString("pref.matching.gmlName.short");
		String prefixB = Internal.I18N.getString("pref.matching.delete.short");
		
		if (config.getProject().getMatching().getMatchingSettings().isGmlNameModeIgnore())
			settingsAText.setText("<html>" + prefixA + ": " + "<b>" + Internal.I18N.getString("pref.matching.gmlName.ignore") + "</b></html>");
		else if (config.getProject().getMatching().getMatchingSettings().isGmlNameModeReplace())
			settingsAText.setText("<html>" + prefixA + ": " + "<b>" + Internal.I18N.getString("pref.matching.gmlName.replace") + "</b></html>");
		else
			settingsAText.setText("<html>" + prefixA + ": " + "<b>" + Internal.I18N.getString("pref.matching.gmlName.append") + "</b></html>");

		if (config.getProject().getMatching().getMatchingSettings().isDeleteModeDelAll())
			settingsBText.setText("<html>" + prefixB + ": " + "<b>" + Internal.I18N.getString("pref.matching.delete.all") + "</b></html>");
		else if (config.getProject().getMatching().getMatchingSettings().isDeleteModeRename())
			settingsBText.setText("<html>" + prefixB + ": " + "<b>" + Internal.I18N.getString("pref.matching.delete.rename") + "</b></html>");
		else 
			settingsBText.setText("<html>" + prefixB + ": " + "<b>" + Internal.I18N.getString("pref.matching.delete.merge") + "</b></html>");
	}

	public void checkValue(JFormattedTextField field) {
		if (((Number)field.getValue()).floatValue()>1) field.setValue(new Float(1.0f));
		if (((Number)field.getValue()).floatValue()<0) field.setValue(new Float(0.0f));
	}

	public JButton getMatchButton() {
		return matchButton;
	}
	
	public JButton getMergeButton() {
		return mergeButton;
	}

	public JButton getDeleteButton() {
		return deleteButton;
	}

	public void loadSettings() {
		lodRef1Combo.setSelectedIndex(config.getProject().getMatching().getMatch().getLodReference()-1);
		lodMer1Combo.setSelectedIndex(config.getProject().getMatching().getMatch().getLodMerge()-1);	
		lineage1Text.setText(config.getProject().getMatching().getMatch().getLineage());
		tableCheck.setSelected(config.getProject().getMatching().getMatch().getShowTable());

		percentageAText.setValue(new Float(config.getProject().getMatching().getMerge().getOverlapOfMerge()));
		percentageBText.setValue(new Float(config.getProject().getMatching().getMerge().getOverlapOfReference()));
		lodRef2Combo.setSelectedIndex(config.getProject().getMatching().getMerge().getLodReference()-1);
		lodMer2Combo.setSelectedIndex(config.getProject().getMatching().getMerge().getLodMerge()-1);
		lineage2Text.setText(config.getProject().getMatching().getMerge().getLineage());

		lineage3Text.setText(config.getProject().getMatching().getMatchingDelete().getLineage());
	}

	public void setSettings() {
		config.getProject().getMatching().getMatch().setLodReference(lodRef1Combo.getSelectedIndex()+1);
		config.getProject().getMatching().getMatch().setLodMerge(lodMer1Combo.getSelectedIndex()+1);	
		config.getProject().getMatching().getMatch().setLineage(lineage1Text.getText());
		config.getProject().getMatching().getMatch().setShowTable(tableCheck.isSelected());

		config.getProject().getMatching().getMerge().setOverlapOfMerge((Float)percentageAText.getValue());
		config.getProject().getMatching().getMerge().setOverlapOfReference((Float)percentageBText.getValue());
		config.getProject().getMatching().getMerge().setLodReference(lodRef2Combo.getSelectedIndex()+1);
		config.getProject().getMatching().getMerge().setLodMerge(lodMer2Combo.getSelectedIndex()+1);
		config.getProject().getMatching().getMerge().setLineage(lineage2Text.getText());

		config.getProject().getMatching().getMatchingDelete().setLineage(lineage3Text.getText());
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals("matchPref.deleteMode")) {
			createSettingText();
			lineageVisibility();
		}
		
		else if (evt.getPropertyName().equals("matchPref.gmlNameMode")) {
			createSettingText();
		}
	}

}

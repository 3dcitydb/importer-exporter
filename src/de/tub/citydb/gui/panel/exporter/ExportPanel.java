package de.tub.citydb.gui.panel.exporter;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import de.tub.citydb.config.Config;
import de.tub.citydb.gui.ImpExpGui;
import de.tub.citydb.gui.panel.filter.FilterPanel;
import de.tub.citydb.gui.util.GuiUtil;

public class ExportPanel extends JPanel {

	private JTextField browseText;
	private JButton browseButton;
	private JTextField workspaceText;
	private JTextField timestampText;
    private FilterPanel filterPanel;
	private JButton exportButton;

	private JPanel row2;
	private JLabel row2_1;
	private JLabel row2_2;

	private Config config;

	public ExportPanel(Config config) {
		this.config = config;
		initGui();
	}

	private void initGui() {

		//gui-elemente anlegen
		browseText = new JTextField("");
		browseButton = new JButton("");
		workspaceText = new JTextField("LIVE");
		timestampText = new JTextField("", 10);
		filterPanel = new FilterPanel(config.getProject().getExporter().getFilter());
		exportButton = new JButton("");

		//gui-elemente mit funktionalität ausstatten
		workspaceText.setEnabled(true);
		timestampText.setEnabled(true);
		browseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveFile();
			}
		});

		//layout
		setLayout(new GridBagLayout());
		{
			JPanel row1 = new JPanel();
			add(row1, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,10,5,5,5));
			row1.setLayout(new GridBagLayout());
			{
				row1.add(browseText, GuiUtil.setConstraints(0,0,1.0,1.0,GridBagConstraints.BOTH,5,5,5,5));
				row1.add(browseButton, GuiUtil.setConstraints(1,0,0.0,0.0,GridBagConstraints.NONE,5,5,5,5));
			}
		}
		{
			row2 = new JPanel();
			add(row2, GuiUtil.setConstraints(0,1,0.0,0.0,GridBagConstraints.HORIZONTAL,0,5,5,5));
			row2.setBorder(BorderFactory.createTitledBorder(""));
			row2.setLayout(new GridBagLayout());
			row2_1 = new JLabel();
			row2_2 = new JLabel();
			workspaceText.setPreferredSize(workspaceText.getPreferredSize());
			timestampText.setPreferredSize(timestampText.getPreferredSize());
			{
				row2.add(row2_1, GuiUtil.setConstraints(0,0,0.0,0.0,GridBagConstraints.NONE,0,5,5,5));
				row2.add(workspaceText, GuiUtil.setConstraints(1,0,1.0,0.0,GridBagConstraints.HORIZONTAL,0,5,5,5));
				row2.add(row2_2, GuiUtil.setConstraints(2,0,0.0,0.0,GridBagConstraints.NONE,0,10,5,5));
				row2.add(timestampText, GuiUtil.setConstraints(3,0,0.0,0.0,GridBagConstraints.HORIZONTAL,0,5,5,5));
			}
		}
		{
			add(filterPanel, GuiUtil.setConstraints(0,2,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
		}
		{
			JPanel row3 = new JPanel();
			add(row3, GuiUtil.setConstraints(0,3,1.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
			row3.add(exportButton);
		}
		this.setEnabled(false);
	}

	public void doTranslation() {
		//internationalisierte Labels und Strings

		row2.setBorder(BorderFactory.createTitledBorder(ImpExpGui.labels.getString("common.border.versioning")));
		row2_1.setText(ImpExpGui.labels.getString("common.label.workspace"));
		row2_2.setText(ImpExpGui.labels.getString("export.label.timestamp"));
		browseButton.setText(ImpExpGui.labels.getString("common.button.browse"));
		exportButton.setText(ImpExpGui.labels.getString("export.button.export"));

		filterPanel.doTranslation();
	}

	//public-methoden
	public void loadSettings() {
		browseText.setText(config.getInternal().getExportFileName());
		workspaceText.setText(config.getProject().getDatabase().getWorkspace().getExportWorkspace());
		timestampText.setText(config.getProject().getDatabase().getWorkspace().getExportDate());
		filterPanel.loadSettings();
	}

	public void setSettings() {
		config.getInternal().setExportFileName(browseText.getText());
		config.getProject().getDatabase().getWorkspace().setExportWorkspace(workspaceText.getText());
		config.getProject().getDatabase().getWorkspace().setExportDate(timestampText.getText());
		filterPanel.setSettings();
	}

	public JButton getExportButton() {
		return exportButton;
	}


	// private-methoden
	private void saveFile() {
		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("CityGML Files (*.gml, *.xml)", "gml", "xml");
		chooser.addChoosableFileFilter(filter);
		chooser.addChoosableFileFilter(chooser.getAcceptAllFileFilter());
		chooser.setFileFilter(filter);
		if (config.getProject().getExporter().getPath().isSetLastUsedMode()) {
			chooser.setCurrentDirectory(new File(config.getProject().getExporter().getPath().getLastUsedPath()));
		} else {
			chooser.setCurrentDirectory(new File(config.getProject().getExporter().getPath().getStandardPath()));
		}
		int result = chooser.showSaveDialog(getTopLevelAncestor());
		if (result == JFileChooser.CANCEL_OPTION) return;
		try {
			String exportString = chooser.getSelectedFile().toString();
			if ((!chooser.getSelectedFile().getName().contains("."))&&(!exportString.equals(""))) exportString += ".gml";
			browseText.setText(exportString);
			config.getProject().getExporter().getPath().setLastUsedPath(chooser.getCurrentDirectory().getAbsolutePath());
		}
		catch (Exception e) {
			//
		}
	}

}



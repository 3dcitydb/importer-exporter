/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2013
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
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
package de.tub.citydb.modules.database.gui.preferences;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import de.tub.citydb.api.database.DatabaseSrs;
import de.tub.citydb.api.event.Event;
import de.tub.citydb.api.event.EventHandler;
import de.tub.citydb.api.event.global.DatabaseConnectionStateEvent;
import de.tub.citydb.api.event.global.GlobalEvents;
import de.tub.citydb.api.log.LogLevel;
import de.tub.citydb.api.registry.ObjectRegistry;
import de.tub.citydb.config.Config;
import de.tub.citydb.config.ConfigUtil;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.database.DatabaseSrsList;
import de.tub.citydb.database.DatabaseConnectionPool;
import de.tub.citydb.gui.ImpExpGui;
import de.tub.citydb.gui.factory.PopupMenuDecorator;
import de.tub.citydb.gui.factory.SrsComboBoxFactory;
import de.tub.citydb.gui.factory.SrsComboBoxFactory.SrsComboBox;
import de.tub.citydb.gui.preferences.AbstractPreferencesComponent;
import de.tub.citydb.log.Logger;
import de.tub.citydb.util.database.DBUtil;
import de.tub.citydb.util.gui.GuiUtil;

@SuppressWarnings("serial")
public class SrsPanel extends AbstractPreferencesComponent implements EventHandler, DropTargetListener {
	private static final int BORDER_THICKNESS = 5;
	private final Logger LOG = Logger.getInstance();
	private final DatabaseConnectionPool dbPool;
	private final ImpExpGui topFrame;

	private JLabel srsComboBoxLabel;
	private JLabel sridLabel;
	private JFormattedTextField sridText;
	private JLabel srsNameLabel;
	private JTextField gmlSrsNameText;
	private JLabel descriptionLabel;
	private JTextField descriptionText;
	private JLabel dbSrsTypeLabel;
	private JTextPane dbSrsTypeText;
	private JLabel dbSrsNameLabel;
	private JTextPane dbSrsNameText;
	private JButton newButton;
	private JButton applyButton;
	private JButton deleteButton;
	private JButton checkButton;
	private JButton copyButton;

	private SrsComboBoxFactory srsComboBoxFactory;
	private SrsComboBox srsComboBox;
	private ActionListener srsComboBoxListener;
	private JPanel contentsPanel;
	private JPanel impExpPanel;
	private JLabel fileLabel;
	private JTextField fileText;
	private JButton browseFileButton;
	private JButton addFileButton;
	private JButton replaceWithFileButton;
	private JButton saveFileButton;

	private JAXBContext projectContext;

	public SrsPanel(Config config, ImpExpGui topFrame) {
		super(config);
		this.topFrame = topFrame;
		dbPool = DatabaseConnectionPool.getInstance();

		initGui();
		ObjectRegistry.getInstance().getEventDispatcher().addEventHandler(GlobalEvents.DATABASE_CONNECTION_STATE, this);
	}

	@Override
	public boolean isModified() {
		DatabaseSrs refSys = srsComboBox.getSelectedItem();

		try { sridText.commitEdit(); } catch (ParseException e) { }
		if (((Number)sridText.getValue()).intValue() != refSys.getSrid()) return true;

		if (!gmlSrsNameText.getText().equals(refSys.getGMLSrsName())) return true;
		if (!descriptionText.getText().equals(refSys.getDescription())) return true;

		return false;
	}

	private void initGui() {
		srsComboBoxLabel = new JLabel();
		sridLabel = new JLabel();

		DecimalFormat tileFormat = new DecimalFormat("##########");	
		tileFormat.setMaximumIntegerDigits(10);
		tileFormat.setMinimumIntegerDigits(1);
		sridText = new JFormattedTextField(tileFormat);

		srsNameLabel = new JLabel();
		gmlSrsNameText = new JTextField();
		descriptionLabel = new JLabel();
		descriptionText = new JTextField();
		newButton = new JButton();
		applyButton = new JButton();
		deleteButton = new JButton();
		checkButton = new JButton();
		checkButton.setEnabled(false);
		copyButton = new JButton();

		fileLabel = new JLabel();
		fileText = new JTextField();
		browseFileButton = new JButton();
		addFileButton = new JButton();
		replaceWithFileButton = new JButton();
		saveFileButton = new JButton();

		dbSrsTypeLabel = new JLabel();
		dbSrsTypeText = new JTextPane();
		dbSrsNameLabel = new JLabel();
		dbSrsNameText = new JTextPane();

		srsComboBoxFactory = SrsComboBoxFactory.getInstance(config);
		srsComboBox = srsComboBoxFactory.createSrsComboBox(false);

		PopupMenuDecorator.getInstance().decorate(sridText, gmlSrsNameText, descriptionText, fileText, dbSrsTypeText, dbSrsNameText);

		sridText.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				if (sridText.getValue() != null) {
					if (((Number)sridText.getValue()).intValue() < 0)
						sridText.setValue(0);
					else if (((Number)sridText.getValue()).intValue() > Integer.MAX_VALUE)
						sridText.setValue(Integer.MAX_VALUE);
				}
			}
		});

		setLayout(new GridBagLayout());

		contentsPanel = new JPanel();
		contentsPanel.setBorder(BorderFactory.createTitledBorder(""));
		contentsPanel.setLayout(new GridBagLayout());
		add(contentsPanel, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));
		{
			JPanel srsPanel = new JPanel();
			srsPanel.setLayout(new GridBagLayout());

			gmlSrsNameText.setPreferredSize(gmlSrsNameText.getPreferredSize());
			descriptionText.setPreferredSize(gmlSrsNameText.getPreferredSize());
			srsComboBox.setPreferredSize(gmlSrsNameText.getPreferredSize());

			srsPanel.add(sridLabel, GuiUtil.setConstraints(0,0,0,0,GridBagConstraints.BOTH,0,BORDER_THICKNESS,0,BORDER_THICKNESS));
			srsPanel.add(sridText, GuiUtil.setConstraints(1,0,1,0,GridBagConstraints.HORIZONTAL,0,BORDER_THICKNESS,0,BORDER_THICKNESS));
			srsPanel.add(checkButton, GuiUtil.setConstraints(2,0,0,0,GridBagConstraints.HORIZONTAL,0,BORDER_THICKNESS,0,BORDER_THICKNESS));

			srsPanel.add(srsNameLabel, GuiUtil.setConstraints(0,1,0,0,GridBagConstraints.BOTH,BORDER_THICKNESS,BORDER_THICKNESS,0,BORDER_THICKNESS));
			srsPanel.add(gmlSrsNameText, GuiUtil.setConstraints(1,1,2,1,1,0,GridBagConstraints.HORIZONTAL,BORDER_THICKNESS,BORDER_THICKNESS,0,BORDER_THICKNESS));

			srsPanel.add(descriptionLabel, GuiUtil.setConstraints(0,2,0,0,GridBagConstraints.BOTH,BORDER_THICKNESS,BORDER_THICKNESS,0,BORDER_THICKNESS));
			srsPanel.add(descriptionText, GuiUtil.setConstraints(1,2,2,1,1,0,GridBagConstraints.HORIZONTAL,BORDER_THICKNESS,BORDER_THICKNESS,0,BORDER_THICKNESS));

			dbSrsTypeText.setEditable(false);
			dbSrsTypeText.setBorder(sridText.getBorder()); 
			dbSrsTypeText.setBackground(srsPanel.getBackground());
			dbSrsTypeText.setMargin(sridText.getMargin());

			dbSrsNameText.setEditable(false);
			dbSrsNameText.setBorder(sridText.getBorder()); 
			dbSrsNameText.setBackground(srsPanel.getBackground());
			dbSrsNameText.setMargin(sridText.getMargin());

			srsPanel.add(dbSrsNameLabel, GuiUtil.setConstraints(0,3,0,0,GridBagConstraints.HORIZONTAL,BORDER_THICKNESS,BORDER_THICKNESS,0,BORDER_THICKNESS));
			srsPanel.add(dbSrsNameText, GuiUtil.setConstraints(1,3,2,1,1,0,GridBagConstraints.BOTH,BORDER_THICKNESS,BORDER_THICKNESS,0,BORDER_THICKNESS));
			srsPanel.add(dbSrsTypeLabel, GuiUtil.setConstraints(0,4,0,0,GridBagConstraints.HORIZONTAL,BORDER_THICKNESS,BORDER_THICKNESS,BORDER_THICKNESS,BORDER_THICKNESS));
			srsPanel.add(dbSrsTypeText, GuiUtil.setConstraints(1,4,2,1,1,0,GridBagConstraints.BOTH,BORDER_THICKNESS,BORDER_THICKNESS,BORDER_THICKNESS,BORDER_THICKNESS));

			Box buttonsPanel = Box.createHorizontalBox();
			buttonsPanel.add(applyButton);
			buttonsPanel.add(Box.createRigidArea(new Dimension(2*BORDER_THICKNESS, 0)));
			buttonsPanel.add(newButton);
			buttonsPanel.add(Box.createRigidArea(new Dimension(2*BORDER_THICKNESS, 0)));
			buttonsPanel.add(copyButton);
			buttonsPanel.add(Box.createRigidArea(new Dimension(2*BORDER_THICKNESS, 0)));
			buttonsPanel.add(deleteButton);
			buttonsPanel.add(Box.createRigidArea(new Dimension(2*BORDER_THICKNESS, 0)));

			GridBagConstraints c = GuiUtil.setConstraints(0,5,3,1,1,0,GridBagConstraints.NONE,BORDER_THICKNESS*2,BORDER_THICKNESS,BORDER_THICKNESS,BORDER_THICKNESS);
			c.anchor = GridBagConstraints.CENTER;
			srsPanel.add(buttonsPanel, c);

			JPanel currentlySupportedContent = new JPanel();
			currentlySupportedContent.setBorder(BorderFactory.createEmptyBorder());
			currentlySupportedContent.setLayout(new GridBagLayout());		
			currentlySupportedContent.add(srsComboBoxLabel, GuiUtil.setConstraints(0,0,0.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
			currentlySupportedContent.add(srsComboBox, GuiUtil.setConstraints(1,0,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));

			contentsPanel.add(currentlySupportedContent, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,0,0,5,0));
			contentsPanel.add(srsPanel, GuiUtil.setConstraints(0,1,1.0,0.0,GridBagConstraints.BOTH,20,0,5,0));
		}

		impExpPanel = new JPanel();
		impExpPanel.setBorder(BorderFactory.createTitledBorder(""));
		impExpPanel.setLayout(new GridBagLayout());
		add(impExpPanel, GuiUtil.setConstraints(0,1,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));
		{
			JPanel browse = new JPanel();
			JPanel button = new JPanel();

			impExpPanel.add(browse, GuiUtil.setConstraints(0,0,1.0,1.0,GridBagConstraints.BOTH,0,0,0,0));
			impExpPanel.add(button, GuiUtil.setConstraints(0,1,0.0,1.0,GridBagConstraints.BOTH,5,0,5,0));
			fileText.setPreferredSize(fileText.getPreferredSize());

			browse.setLayout(new GridBagLayout());
			{
				browse.add(fileLabel, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.HORIZONTAL,5,5,0,5));
				browse.add(fileText, GuiUtil.setConstraints(0,1,0.0,0.0,GridBagConstraints.HORIZONTAL,0,5,5,5));
				browse.add(browseFileButton, GuiUtil.setConstraints(1,1,0.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
			}

			button.setLayout(new GridBagLayout());
			{
				button.add(addFileButton,GuiUtil.setConstraints(0,0,0,0,GridBagConstraints.NONE,5,5,5,5));
				button.add(replaceWithFileButton, GuiUtil.setConstraints(1,0,0,0,GridBagConstraints.NONE,5,5,5,5));
				button.add(saveFileButton, GuiUtil.setConstraints(2,0,0,0,GridBagConstraints.NONE,5,20,5,5));
			}
		}

		// influence focus policy
		checkButton.setFocusCycleRoot(false);

		srsComboBoxListener = new ActionListener() {			
			public void actionPerformed(ActionEvent e) {
				displaySelectedValues();
			}
		};

		DropTarget dropTarget = new DropTarget(fileText, this);
		fileText.setDropTarget(dropTarget);
		impExpPanel.setDropTarget(dropTarget);

		srsComboBox.addActionListener(srsComboBoxListener);

		newButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (requestChange()) {
					DatabaseSrs refSys = DatabaseSrs.createDefaultSrs();
					refSys.setDescription(getNewRefSysDescription());
					refSys.setSupported(!dbPool.isConnected());

					config.getProject().getDatabase().addReferenceSystem(refSys);
					updateSrsComboBoxes(false);
					srsComboBox.setSelectedItem(refSys);

					displaySelectedValues();
				}
			}
		});

		copyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (requestChange()) {
					DatabaseSrs orig = srsComboBox.getSelectedItem();
					DatabaseSrs copy = new DatabaseSrs(orig);
					copy.setDescription(getCopyOfDescription(orig));

					config.getProject().getDatabase().addReferenceSystem(copy);
					updateSrsComboBoxes(false);
					srsComboBox.setSelectedItem(copy);

					displaySelectedValues();
				}
			}
		});

		applyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setSettings();
				LOG.info("Settings successfully applied.");
			}
		});

		deleteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DatabaseSrs refSys = srsComboBox.getSelectedItem();
				int index = srsComboBox.getSelectedIndex();

				String text = Internal.I18N.getString("pref.db.srs.dialog.delete.msg");
				Object[] args = new Object[]{refSys.getDescription()};
				String formattedMsg = MessageFormat.format(text, args);

				if (JOptionPane.showConfirmDialog(getTopLevelAncestor(), formattedMsg, Internal.I18N.getString("pref.db.srs.dialog.delete.title"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
					config.getProject().getDatabase().getReferenceSystems().remove(refSys);
					updateSrsComboBoxes(false);
					srsComboBox.setSelectedIndex(index < srsComboBox.getItemCount() ? index : index - 1);
					displaySelectedValues();
				}
			}
		});

		checkButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int srid = 0;

				try {
					srid = Integer.parseInt(sridText.getText().trim());
				} catch (NumberFormatException nfe) {
					//
				}			

				try {
					DatabaseSrs tmp = DatabaseSrs.createDefaultSrs();
					tmp.setSrid(srid);
					DBUtil.getSrsInfo(tmp);
					if (tmp.isSupported()) {
						LOG.all(LogLevel.INFO, "SRID " + srid + " is supported.");
						LOG.all(LogLevel.INFO, "Database name: " + tmp.getDatabaseSrsName());
						LOG.all(LogLevel.INFO, "SRS type: " + tmp.getType());
					} else
						LOG.all(LogLevel.WARN, "SRID " + srid + " is NOT supported.");
				} catch (SQLException sqlEx) {
					LOG.error("Error while checking user-defined SRSs: " + sqlEx.getMessage().trim());
				}
			}
		});

		browseFileButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				browseReferenceSystemFile(Internal.I18N.getString("pref.db.srs.label.file"));
			}
		});

		addFileButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				importReferenceSystems(false);
			}
		});

		replaceWithFileButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				importReferenceSystems(true);
			}
		});

		saveFileButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exportReferenceSystems();
			}
		});
	}

	@Override
	public void doTranslation() {
		((TitledBorder)contentsPanel.getBorder()).setTitle(Internal.I18N.getString("pref.db.srs.border.currentlySupported"));	
		((TitledBorder)impExpPanel.getBorder()).setTitle(Internal.I18N.getString("pref.db.srs.border.impexp"));	

		srsComboBoxLabel.setText(Internal.I18N.getString("common.label.boundingBox.crs"));		
		sridLabel.setText(Internal.I18N.getString("pref.db.srs.label.srid"));
		srsNameLabel.setText(Internal.I18N.getString("pref.db.srs.label.srsName"));
		descriptionLabel.setText(Internal.I18N.getString("pref.db.srs.label.description"));
		dbSrsTypeLabel.setText(Internal.I18N.getString("pref.db.srs.label.dbSrsType"));		
		dbSrsNameLabel.setText(Internal.I18N.getString("pref.db.srs.label.dbSrsName"));		
		newButton.setText(Internal.I18N.getString("pref.db.srs.button.new"));
		applyButton.setText(Internal.I18N.getString("common.button.apply"));
		deleteButton.setText(Internal.I18N.getString("pref.db.srs.button.delete"));
		copyButton.setText(Internal.I18N.getString("pref.db.srs.button.copy"));		
		checkButton.setText(Internal.I18N.getString("pref.db.srs.button.check"));

		fileLabel.setText(Internal.I18N.getString("pref.db.srs.label.file"));
		browseFileButton.setText(Internal.I18N.getString("common.button.browse"));
		addFileButton.setText(Internal.I18N.getString("pref.db.srs.button.addFile"));
		replaceWithFileButton.setText(Internal.I18N.getString("pref.db.srs.button.replaceWithFile"));
		saveFileButton.setText(Internal.I18N.getString("pref.db.srs.button.saveFile"));
	}

	@Override
	public void resetSettings() {
		config.getProject().getDatabase().addDefaultReferenceSystems();
		srsComboBoxFactory.updateAll(true);
	}

	@Override
	public void loadSettings() {
		displaySelectedValues();
	}

	@Override
	public void setSettings() {
		DatabaseSrs refSys = srsComboBox.getSelectedItem();
		int prev = refSys.getSrid();

		refSys.setSrid(((Number)sridText.getValue()).intValue());
		if (dbPool.isConnected() && prev != refSys.getSrid()) {
			try {
				DBUtil.getSrsInfo(refSys);

				if (refSys.isSupported())
					LOG.debug("SRID " + refSys.getSrid() + " is supported.");
				else
					LOG.warn("SRID " + refSys.getSrid() + " is NOT supported.");
			} catch (SQLException sqlEx) {
				LOG.error("Error while checking user-defined SRSs: " + sqlEx.getMessage().trim());
			}
		}

		refSys.setGMLSrsName(gmlSrsNameText.getText().trim());
		refSys.setDescription(descriptionText.getText().trim());
		if (refSys.getDescription().length() == 0)
			refSys.setDescription(getNewRefSysDescription());

		updateSrsComboBoxes(true);				
		displaySelectedValues();
	}

	@Override
	public String getTitle() {
		return Internal.I18N.getString("pref.tree.db.srs");
	}

	private void displaySelectedValues() {
		DatabaseSrs refSys = srsComboBox.getSelectedItem();
		if (refSys == null) 
			return;

		sridText.setValue(refSys.getSrid());
		gmlSrsNameText.setText(refSys.getGMLSrsName());
		descriptionText.setText(refSys.toString());
		srsComboBox.setToolTipText(refSys.getDescription());
		dbSrsNameText.setText(wrap(refSys.getDatabaseSrsName(), 40));
		dbSrsTypeText.setText(refSys.getType().toString());
		
		boolean isDBSrs = !srsComboBox.isDBReferenceSystemSelected();
		sridText.setEditable(isDBSrs);
		gmlSrsNameText.setEditable(isDBSrs);
		descriptionText.setEditable(isDBSrs);
		applyButton.setEnabled(isDBSrs);		
		deleteButton.setEnabled(isDBSrs);
	}

	private String wrap(String in,int len) {
		if (in.length() <= len)
			return in;

		int wrapAt = Math.max(in.substring(0, len).lastIndexOf(' '), in.substring(0, len).lastIndexOf('-'));
		
		return wrapAt == -1 ?
				in.substring(0, len) + '-' + System.getProperty("line.separator") + wrap(in.substring(len, in.length()).trim(), len) :
				in.substring(0, wrapAt) + System.getProperty("line.separator") + wrap(in.substring(wrapAt, in.length()).trim(), len);
	}

	private String getCopyOfDescription(DatabaseSrs refSys) {
		// pattern: "referenceSystemName - copy 1"
		// so to retrieve referenceSystem, " - copy*" has to be deleted...

		int nr = 0;
		String name = refSys.getDescription().replaceAll("\\s*-\\s*" + Internal.I18N.getString("pref.db.srs.label.copyReferenceSystem") + ".*$", "");
		String copy = name + " - " + Internal.I18N.getString("pref.db.srs.label.copyReferenceSystem");

		if (Internal.I18N.getString("common.label.boundingBox.crs.sameAsInDB").replaceAll("\\s*-\\s*" + Internal.I18N.getString("pref.db.srs.label.copyReferenceSystem") + ".*$", "").toLowerCase().equals(name.toLowerCase()))
			nr++;

		for (DatabaseSrs tmp : config.getProject().getDatabase().getReferenceSystems()) 
			if (tmp.getDescription().replaceAll("\\s*-\\s*" + Internal.I18N.getString("pref.db.srs.label.copyReferenceSystem") + ".*$", "").toLowerCase().equals(name.toLowerCase()))
				nr++;

		if (nr > 1)
			return copy + " " + nr;
		else
			return copy;
	}

	private String getNewRefSysDescription() {
		int nr = 1;
		String name = Internal.I18N.getString("pref.db.srs.label.newReferenceSystem");
		for (DatabaseSrs refSys : config.getProject().getDatabase().getReferenceSystems()) 
			if (refSys.getDescription().toLowerCase().startsWith(name.toLowerCase()))
				nr++;

		if (nr > 1)
			return name + " " + nr;
		else
			return name;
	}

	private void updateSrsComboBoxes(boolean sort) {
		srsComboBox.removeActionListener(srsComboBoxListener);
		srsComboBoxFactory.updateAll(sort);
		srsComboBox.addActionListener(srsComboBoxListener);
	}

	private void importReferenceSystems(boolean replace) {
		try {
			topFrame.clearConsole();
			topFrame.setStatusText(Internal.I18N.getString("main.status.database.srs.import.label"));

			File file = new File(fileText.getText().trim());
			String msg = "";

			if (replace)
				msg += "Replacing reference systems with those from file '";
			else
				msg += "Adding reference systems from file '";

			LOG.info(msg + file.getAbsolutePath() + "'.");

			if (!file.exists() || !file.isFile() || !file.canRead()) {
				LOG.error("Failed to open reference system file.");
				topFrame.errorMessage(Internal.I18N.getString("common.dialog.error.io.title"), 
						MessageFormat.format(Internal.I18N.getString("common.dialog.file.read.error"), Internal.I18N.getString("pref.db.srs.error.read.msg")));
				return;
			}

			Object object = ConfigUtil.unmarshal(file, getJAXBContext());
			if (object instanceof DatabaseSrsList) {
				DatabaseSrsList refSyss = (DatabaseSrsList)object;				
				if (replace)
					config.getProject().getDatabase().getReferenceSystems().clear();

				if (dbPool.isConnected())
					LOG.info("Checking whether reference systems are supported by database profile.");

				for (DatabaseSrs refSys : refSyss.getItems()) {
					msg = "Adding reference system '" + refSys.getDescription() + "' (SRID: " + refSys.getSrid() + ").";

					if (dbPool.isConnected()) {
						try {
							DBUtil.getSrsInfo(refSys);
							if (!refSys.isSupported())
								msg += " (NOT supported)";
							else
								msg += " (supported)";

						} catch (SQLException sqlEx) {
							LOG.error("Error while checking user-defined SRSs: " + sqlEx.getMessage().trim());
						}
					}

					config.getProject().getDatabase().getReferenceSystems().add(refSys);
					LOG.info(msg);
				}

				updateSrsComboBoxes(true);
				displaySelectedValues();

				LOG.info("Reference systems successfully imported from file '" + file.getAbsolutePath() + "'.");
			} else
				LOG.error("Could not find reference system definitions in file '" + file.getAbsolutePath() + "'.");

		} catch (JAXBException jaxb) {
			String msg = jaxb.getMessage();
			if (msg == null && jaxb.getLinkedException() != null)
				msg = jaxb.getLinkedException().getMessage();

			LOG.error("Failed to parse file: " + msg);
			topFrame.errorMessage(Internal.I18N.getString("common.dialog.error.io.title"), 
					MessageFormat.format(Internal.I18N.getString("common.dialog.file.read.error"), msg));
		} catch (IOException e) {
			String msg = e.getMessage();
			LOG.error("Failed to access file: " + msg);
			topFrame.errorMessage(Internal.I18N.getString("common.dialog.error.io.title"), 
					MessageFormat.format(Internal.I18N.getString("common.dialog.file.read.error"), msg));			
		} finally {
			topFrame.setStatusText(Internal.I18N.getString("main.status.ready.label"));
		}
	}

	private void exportReferenceSystems() {		
		try {
			setSettings();

			if (config.getProject().getDatabase().getReferenceSystems().isEmpty()) {
				LOG.error("There are no user-defined reference systems to be exported.");
				return;
			}

			topFrame.clearConsole();
			topFrame.setStatusText(Internal.I18N.getString("main.status.database.srs.export.label"));

			String fileName = fileText.getText().trim();
			if (fileName.length() == 0) {
				LOG.error("Please specify the export file for the reference systems.");
				topFrame.errorMessage(Internal.I18N.getString("common.dialog.error.io.title"), Internal.I18N.getString("pref.db.srs.error.write.msg"));
				return;
			}

			if ((!fileName.contains("."))) {
				fileName += ".xml";
				fileText.setText(fileName);
			}

			File file = new File(fileName);
			LOG.info("Writing reference systems to file '" + file.getAbsolutePath() + "'.");

			DatabaseSrsList refSys = new DatabaseSrsList();
			for (DatabaseSrs tmp : config.getProject().getDatabase().getReferenceSystems()) {
				DatabaseSrs copy = new DatabaseSrs(tmp);
				copy.setId(null);				
				refSys.addItem(copy);

				LOG.info("Writing reference system '" + tmp.getDescription() + "' (SRID: " + tmp.getSrid() + ").");
			}

			ConfigUtil.marshal(refSys, file, getJAXBContext());			
			LOG.info("Reference systems successfully written to file '" + file.getAbsolutePath() + "'.");
		} catch (JAXBException jaxb) {
			String msg = jaxb.getMessage();
			if (msg == null && jaxb.getLinkedException() != null)
				msg = jaxb.getLinkedException().getMessage();

			LOG.error("Failed to write file: " + msg);
			topFrame.errorMessage(Internal.I18N.getString("common.dialog.error.io.title"), 
					MessageFormat.format(Internal.I18N.getString("common.dialog.file.write.error"), msg));
		} finally {
			topFrame.setStatusText(Internal.I18N.getString("main.status.ready.label"));
		}
	}

	private boolean requestChange() {
		if (isModified()) {
			String text = Internal.I18N.getString("pref.db.srs.apply.msg");
			Object[] args = new Object[]{srsComboBox.getSelectedItem().getDescription()};
			String formattedMsg = MessageFormat.format(text, args);

			int res = JOptionPane.showConfirmDialog(getTopLevelAncestor(), formattedMsg, 
					Internal.I18N.getString("pref.db.srs.apply.title"), JOptionPane.YES_NO_CANCEL_OPTION);
			if (res == JOptionPane.CANCEL_OPTION) 
				return false;
			else if (res == JOptionPane.YES_OPTION) {
				setSettings();
			} else
				loadSettings();
		}

		return true;
	}

	private JAXBContext getJAXBContext() throws JAXBException {
		if (projectContext == null)
			projectContext = JAXBContext.newInstance(DatabaseSrsList.class);

		return projectContext;
	}

	private void browseReferenceSystemFile(String title) {
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle(title);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

		FileNameExtensionFilter filter = new FileNameExtensionFilter("XML Files (*.xml)", "xml");
		chooser.addChoosableFileFilter(filter);
		chooser.addChoosableFileFilter(chooser.getAcceptAllFileFilter());
		chooser.setFileFilter(filter);

		if (!fileText.getText().trim().isEmpty())
			chooser.setCurrentDirectory(new File(fileText.getText()));
		else
			chooser.setCurrentDirectory(new File(Internal.SRS_TEMPLATES_PATH));

		int result = chooser.showOpenDialog(getTopLevelAncestor());
		if (result == JFileChooser.CANCEL_OPTION) 
			return;

		fileText.setText(chooser.getSelectedFile().toString());
	}

	@Override
	public void handleEvent(Event event) throws Exception {
		boolean isConnected = ((DatabaseConnectionStateEvent)event).isConnected();

		if (!isConnected) {
			DatabaseSrs tmp = DatabaseSrs.createDefaultSrs();
			for (DatabaseSrs refSys : config.getProject().getDatabase().getReferenceSystems()) {
				refSys.setSupported(true);
				refSys.setDatabaseSrsName(tmp.getDatabaseSrsName());
				refSys.setType(tmp.getType());
			}
		}

		checkButton.setEnabled(isConnected);
		updateSrsComboBoxes(false);			
		displaySelectedValues();
	}

	@Override
	public void dragEnter(DropTargetDragEvent dtde) {
		dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
	}

	@Override
	public void dragExit(DropTargetEvent dte) {
		// nothing to do here
	}

	@Override
	public void dragOver(DropTargetDragEvent dtde) {
		// nothing to do here
	}

	@SuppressWarnings("unchecked")
	@Override
	public void drop(DropTargetDropEvent dtde) {
		for (DataFlavor dataFlover : dtde.getCurrentDataFlavors()) {
			if (dataFlover.isFlavorJavaFileListType()) {
				try {
					dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);

					for (File file : (List<File>)dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor)) {
						if (file.isFile() && file.canRead()) {
							fileText.setText(file.getCanonicalPath());
							break;
						}
					}

					dtde.getDropTargetContext().dropComplete(true);	
				} catch (UnsupportedFlavorException e1) {
					//
				} catch (IOException e2) {
					//
				}
			}
		}
	}

	@Override
	public void dropActionChanged(DropTargetDragEvent dtde) {
		// nothing to do here
	}

}

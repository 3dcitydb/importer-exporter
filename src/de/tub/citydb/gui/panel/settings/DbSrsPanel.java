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
package de.tub.citydb.gui.panel.settings;

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
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.database.ReferenceSystem;
import de.tub.citydb.config.project.database.ReferenceSystems;
import de.tub.citydb.gui.ImpExpGui;
import de.tub.citydb.gui.components.SrsComboBoxManager;
import de.tub.citydb.gui.components.SrsComboBoxManager.SrsComboBox;
import de.tub.citydb.gui.util.GuiUtil;
import de.tub.citydb.jaxb.JAXBContextRegistry;
import de.tub.citydb.log.Logger;
import de.tub.citydb.util.DBUtil;

@SuppressWarnings("serial")
public class DbSrsPanel extends PrefPanelBase implements PropertyChangeListener, DropTargetListener {
	private static final int BORDER_THICKNESS = 5;
	private final Logger LOG = Logger.getInstance();

	private JLabel srsComboBoxLabel;
	private JLabel sridLabel;
	private JFormattedTextField sridText;
	private JLabel srsNameLabel;
	private JTextField srsNameText;
	private JLabel descriptionLabel;
	private JTextField descriptionText;
	private JButton newButton;
	private JButton applyButton;
	private JButton deleteButton;
	private JButton checkButton;
	private JButton copyButton;

	private SrsComboBoxManager srsComboBoxManager;
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

	private final ImpExpGui topFrame;

	public DbSrsPanel(Config config, ImpExpGui topFrame) {
		super(config);
		this.topFrame = topFrame;

		initGui();
		config.getInternal().addPropertyChangeListener(this);
	}

	@Override
	public boolean isModified() {
		ReferenceSystem refSys = srsComboBox.getSelectedItem();

		try { sridText.commitEdit(); } catch (ParseException e) { }
		if (((Number)sridText.getValue()).intValue() != refSys.getSrid()) return true;

		if (!srsNameText.getText().equals(refSys.getSrsName())) return true;
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
		srsNameText = new JTextField();
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

		srsComboBoxManager = SrsComboBoxManager.getInstance(config);
		srsComboBox = srsComboBoxManager.getSrsComboBox(false);

		GuiUtil.addStandardEditingPopupMenu(sridText, srsNameText, descriptionText, fileText);

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

			srsNameText.setPreferredSize(srsNameText.getPreferredSize());
			descriptionText.setPreferredSize(srsNameText.getPreferredSize());
			srsComboBox.setPreferredSize(srsNameText.getPreferredSize());

			srsPanel.add(sridLabel, GuiUtil.setConstraints(0,0,0,0,GridBagConstraints.BOTH,0,BORDER_THICKNESS,0,BORDER_THICKNESS));
			srsPanel.add(sridText, GuiUtil.setConstraints(1,0,1,0,GridBagConstraints.HORIZONTAL,0,BORDER_THICKNESS,0,BORDER_THICKNESS));
			srsPanel.add(checkButton, GuiUtil.setConstraints(2,0,0,0,GridBagConstraints.HORIZONTAL,0,BORDER_THICKNESS,0,BORDER_THICKNESS));

			srsPanel.add(srsNameLabel, GuiUtil.setConstraints(0,1,0,0,GridBagConstraints.BOTH,BORDER_THICKNESS,BORDER_THICKNESS,0,BORDER_THICKNESS));
			GridBagConstraints c =  GuiUtil.setConstraints(1,1,1,0,GridBagConstraints.HORIZONTAL,BORDER_THICKNESS,BORDER_THICKNESS,0,BORDER_THICKNESS);
			c.gridwidth = 2;
			srsPanel.add(srsNameText, c);

			srsPanel.add(descriptionLabel, GuiUtil.setConstraints(0,2,0,0,GridBagConstraints.BOTH,BORDER_THICKNESS,BORDER_THICKNESS,BORDER_THICKNESS,BORDER_THICKNESS));
			c = GuiUtil.setConstraints(1,2,1,0,GridBagConstraints.HORIZONTAL,BORDER_THICKNESS,BORDER_THICKNESS,BORDER_THICKNESS,BORDER_THICKNESS);
			c.gridwidth = 2;
			srsPanel.add(descriptionText, c);

			Box buttonsPanel = Box.createHorizontalBox();
			buttonsPanel.add(applyButton);
			buttonsPanel.add(Box.createRigidArea(new Dimension(2*BORDER_THICKNESS, 0)));
			buttonsPanel.add(newButton);
			buttonsPanel.add(Box.createRigidArea(new Dimension(2*BORDER_THICKNESS, 0)));
			buttonsPanel.add(copyButton);
			buttonsPanel.add(Box.createRigidArea(new Dimension(2*BORDER_THICKNESS, 0)));
			buttonsPanel.add(deleteButton);
			buttonsPanel.add(Box.createRigidArea(new Dimension(2*BORDER_THICKNESS, 0)));

			c = GuiUtil.setConstraints(0,3,1,0,GridBagConstraints.NONE,BORDER_THICKNESS*2,BORDER_THICKNESS,BORDER_THICKNESS,BORDER_THICKNESS);
			c.gridwidth = 3;
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
					ReferenceSystem refSys = new ReferenceSystem(0, "", getNewRefSysDescription(), false);

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
					ReferenceSystem orig = srsComboBox.getSelectedItem();
					ReferenceSystem copy = new ReferenceSystem(orig);
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
				ReferenceSystem refSys = srsComboBox.getSelectedItem();
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
					if (DBUtil.getInstance(topFrame.getDBPool()).isSrsSupported(srid))
						LOG.info("SRID " + srid + " is supported.");
					else
						LOG.warn("SRID " + srid + " is NOT supported.");
				} catch (SQLException sqlEx) {
					LOG.error("Error while checking user-defined SRSs: " + sqlEx.getMessage().trim());
				}
			}
		});

		browseFileButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				browseImpExpFile(Internal.I18N.getString("pref.db.srs.label.file"));
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
		contentsPanel.setBorder(BorderFactory.createTitledBorder(Internal.I18N.getString("pref.db.srs.border.currentlySupported")));

		srsComboBoxLabel.setText(Internal.I18N.getString("common.label.boundingBox.crs"));		
		sridLabel.setText(Internal.I18N.getString("pref.db.srs.label.srid"));
		srsNameLabel.setText(Internal.I18N.getString("pref.db.srs.label.srsName"));
		descriptionLabel.setText(Internal.I18N.getString("pref.db.srs.label.description"));
		newButton.setText(Internal.I18N.getString("pref.db.srs.button.new"));
		applyButton.setText(Internal.I18N.getString("pref.db.srs.button.apply"));
		deleteButton.setText(Internal.I18N.getString("pref.db.srs.button.delete"));
		copyButton.setText(Internal.I18N.getString("pref.db.srs.button.copy"));		
		checkButton.setText(Internal.I18N.getString("pref.db.srs.button.check"));

		impExpPanel.setBorder(BorderFactory.createTitledBorder(Internal.I18N.getString("pref.db.srs.border.impexp")));
		fileLabel.setText(Internal.I18N.getString("pref.db.srs.label.file"));
		browseFileButton.setText(Internal.I18N.getString("common.button.browse"));
		addFileButton.setText(Internal.I18N.getString("pref.db.srs.button.addFile"));
		replaceWithFileButton.setText(Internal.I18N.getString("pref.db.srs.button.replaceWithFile"));
		saveFileButton.setText(Internal.I18N.getString("pref.db.srs.button.saveFile"));

		srsComboBox.doTranslation();
	}

	@Override
	public void loadSettings() {
		srsComboBox.updateContent();
		displaySelectedValues();
	}

	@Override
	public void setSettings() {
		ReferenceSystem refSys = srsComboBox.getSelectedItem();
		int srid = ((Number)sridText.getValue()).intValue();

		if (!config.getInternal().isShuttingDown() && 
				srid != refSys.getSrid() && 
				config.getInternal().isConnected()) {
			boolean isSupported = false;

			try {
				isSupported = DBUtil.getInstance(topFrame.getDBPool()).isSrsSupported(srid);

				if (isSupported)
					LOG.debug("SRID " + srid + " is supported.");
				else
					LOG.warn("SRID " + srid + " is NOT supported.");
			} catch (SQLException sqlEx) {
				LOG.error("Error while checking user-defined SRSs: " + sqlEx.getMessage().trim());
			}

			refSys.setSupported(isSupported);
		}

		refSys.setSrid(srid);
		refSys.setSrsName(srsNameText.getText().trim());
		refSys.setDescription(descriptionText.getText().trim());
		if (refSys.getDescription().length() == 0)
			refSys.setDescription(getNewRefSysDescription());

		if (!config.getInternal().isShuttingDown()) {
			updateSrsComboBoxes(true);				
			displaySelectedValues();
		}
	}

	private void displaySelectedValues() {
		ReferenceSystem refSys = srsComboBox.getSelectedItem();
		if (refSys == null) 
			return;

		sridText.setValue(refSys.getSrid());
		srsNameText.setText(refSys.getSrsName());
		descriptionText.setText(refSys.toString());
		srsComboBox.setToolTipText(refSys.getDescription());

		boolean enabled = !srsComboBox.isDBReferenceSystemSelected();
		sridText.setEnabled(enabled);
		srsNameText.setEnabled(enabled);
		descriptionText.setEnabled(enabled);
		applyButton.setEnabled(enabled);		
		deleteButton.setEnabled(enabled);
	}

	private String getCopyOfDescription(ReferenceSystem refSys) {
		// pattern: "referenceSystemName - copy 1"
		// so to retrieve referenceSystem, " - copy*" has to be deleted...

		int nr = 0;
		String name = refSys.getDescription().replaceAll("\\s*-\\s*" + Internal.I18N.getString("pref.db.srs.label.copyReferenceSystem") + ".*$", "");
		String copy = name + " - " + Internal.I18N.getString("pref.db.srs.label.copyReferenceSystem");

		if (Internal.I18N.getString("common.label.boundingBox.crs.sameAsInDB").replaceAll("\\s*-\\s*" + Internal.I18N.getString("pref.db.srs.label.copyReferenceSystem") + ".*$", "").toLowerCase().equals(name.toLowerCase()))
			nr++;

		for (ReferenceSystem tmp : config.getProject().getDatabase().getReferenceSystems()) 
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
		for (ReferenceSystem refSys : config.getProject().getDatabase().getReferenceSystems()) 
			if (refSys.getDescription().toLowerCase().startsWith(name.toLowerCase()))
				nr++;

		if (nr > 1)
			return name + " " + nr;
		else
			return name;
	}

	private void updateSrsComboBoxes(boolean sort) {
		srsComboBox.removeActionListener(srsComboBoxListener);
		srsComboBoxManager.updateAll(sort);
		srsComboBox.addActionListener(srsComboBoxListener);
	}

	private void importReferenceSystems(boolean replace) {
		try {
			topFrame.getConsoleText().setText("");
			topFrame.getStatusText().setText(Internal.I18N.getString("main.status.database.srs.import.label"));

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

			JAXBContext ctx = JAXBContextRegistry.getInstance("de.tub.citydb.config.project");
			Unmarshaller um = ctx.createUnmarshaller();			
			Object object = um.unmarshal(file);

			if (object instanceof ReferenceSystems) {
				ReferenceSystems refSyss = (ReferenceSystems)object;				
				if (replace)
					config.getProject().getDatabase().getReferenceSystems().clear();

				if (config.getInternal().isConnected())
					LOG.info("Checking whether reference systems are supported by database profile.");

				for (ReferenceSystem refSys : refSyss.getItems()) {
					msg = "Adding reference system '" + refSys.getDescription() + "' (SRID: " + refSys.getSrid() + ").";

					if (config.getInternal().isConnected()) {
						boolean isSupported = false;

						try {
							isSupported = DBUtil.getInstance(topFrame.getDBPool()).isSrsSupported(refSys.getSrid());
							if (!isSupported)
								msg += " (NOT supported)";
							else
								msg += " (supported)";

						} catch (SQLException sqlEx) {
							LOG.error("Error while checking user-defined SRSs: " + sqlEx.getMessage().trim());
						}

						refSys.setSupported(isSupported);
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
		} finally {
			topFrame.getStatusText().setText(Internal.I18N.getString("main.status.ready.label"));
		}
	}

	private void exportReferenceSystems() {		
		try {
			if (config.getProject().getDatabase().getReferenceSystems().isEmpty()) {
				LOG.error("There are no user-defined reference systems to be exported.");
				return;
			}

			topFrame.getConsoleText().setText("");
			topFrame.getStatusText().setText(Internal.I18N.getString("main.status.database.srs.export.label"));

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

			ReferenceSystems refSys = new ReferenceSystems();
			for (ReferenceSystem tmp : config.getProject().getDatabase().getReferenceSystems()) {
				ReferenceSystem copy = new ReferenceSystem(tmp);
				copy.setId(null);				
				refSys.addItem(copy);

				LOG.info("Writing reference system '" + tmp.getDescription() + "' (SRID: " + tmp.getSrid() + ").");
			}

			JAXBContext ctx = JAXBContextRegistry.getInstance("de.tub.citydb.config.project");
			Marshaller m = ctx.createMarshaller();			
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,Boolean.TRUE);
			m.setProperty("com.sun.xml.bind.indentString", "  ");

			m.marshal(refSys, file);			
			LOG.info("Reference systems successfully written to file '" + file.getAbsolutePath() + "'.");
		} catch (JAXBException jaxb) {
			String msg = jaxb.getMessage();
			if (msg == null && jaxb.getLinkedException() != null)
				msg = jaxb.getLinkedException().getMessage();

			LOG.error("Failed to write file: " + msg);
			topFrame.errorMessage(Internal.I18N.getString("common.dialog.error.io.title"), 
					MessageFormat.format(Internal.I18N.getString("common.dialog.file.write.error"), msg));
		} finally {
			topFrame.getStatusText().setText(Internal.I18N.getString("main.status.ready.label"));
		}
	}

	private boolean requestChange() {
		if (isModified()) {
			int res = JOptionPane.showConfirmDialog(getTopLevelAncestor(), Internal.I18N.getString("pref.db.srs.apply.msg"), 
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

	private void browseImpExpFile(String title) {
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle(title);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

		FileNameExtensionFilter filter = new FileNameExtensionFilter("XML Files (*.xml)", "xml");
		chooser.addChoosableFileFilter(filter);
		chooser.addChoosableFileFilter(chooser.getAcceptAllFileFilter());
		chooser.setFileFilter(filter);

		if (!fileText.getText().trim().isEmpty())
			chooser.setCurrentDirectory(new File(fileText.getText()));

		int result = chooser.showOpenDialog(getTopLevelAncestor());
		if (result == JFileChooser.CANCEL_OPTION) 
			return;

		fileText.setText(chooser.getSelectedFile().toString());
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals("database.isConnected")) {
			boolean isConnected = (Boolean)evt.getNewValue();

			if (!isConnected) {
				for (ReferenceSystem refSys : config.getProject().getDatabase().getReferenceSystems())
					refSys.setSupported(true);
			}

			checkButton.setEnabled(isConnected);
			updateSrsComboBoxes(false);			
			if (srsComboBox.isDBReferenceSystemSelected())
				displaySelectedValues();
		}
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

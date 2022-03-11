/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2021
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.lrg.tum.de/gis/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * Virtual City Systems, Berlin <https://vc.systems/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Taufkirchen <http://www.moss.de/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.citydb.gui.operation.database.preferences;

import org.citydb.cli.util.CliConstants;
import org.citydb.config.Config;
import org.citydb.config.ConfigUtil;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.database.DatabaseSrs;
import org.citydb.config.project.database.DatabaseSrsList;
import org.citydb.core.database.connection.DatabaseConnectionPool;
import org.citydb.core.registry.ObjectRegistry;
import org.citydb.core.util.CoreConstants;
import org.citydb.gui.components.TitledPanel;
import org.citydb.gui.components.popup.PopupMenuDecorator;
import org.citydb.gui.components.srs.SrsComboBox;
import org.citydb.gui.components.srs.SrsComboBoxFactory;
import org.citydb.gui.operation.database.operations.SrsOperation;
import org.citydb.gui.operation.database.util.SrsNameComboBox;
import org.citydb.gui.plugin.internal.InternalPreferencesComponent;
import org.citydb.gui.plugin.view.ViewController;
import org.citydb.gui.util.GuiUtil;
import org.citydb.util.event.Event;
import org.citydb.util.event.EventDispatcher;
import org.citydb.util.event.EventHandler;
import org.citydb.util.event.global.DatabaseConnectionStateEvent;
import org.citydb.util.event.global.EventType;
import org.citydb.util.event.global.PropertyChangeEvent;
import org.citydb.util.log.Logger;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.NumberFormatter;
import javax.xml.bind.JAXBException;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.List;
import java.util.Locale;

public class SrsPanel extends InternalPreferencesComponent implements EventHandler, DropTargetListener {
	private final Logger log = Logger.getInstance();
	private final DatabaseConnectionPool dbPool;
	private final ViewController viewController;

	private TitledPanel contentsPanel;
	private TitledPanel impExpPanel;

	private JLabel srsComboBoxLabel;
	private JLabel sridLabel;
	private JFormattedTextField sridText;
	private JLabel srsNameLabel;
	private SrsNameComboBox srsNameComboBox;
	private JLabel descriptionLabel;
	private JTextField descriptionText;
	private JLabel dbSrsTypeLabel;
	private JTextField dbSrsTypeText;
	private JLabel dbSrsNameLabel;
	private JTextField dbSrsNameText;
	private JButton newButton;
	private JButton applyButton;
	private JButton deleteButton;
	private JButton checkButton;
	private JButton copyButton;

	private SrsComboBoxFactory srsComboBoxFactory;
	private SrsComboBox srsComboBox;
	private ActionListener srsComboBoxListener;
	private JLabel fileLabel;
	private JTextField fileText;
	private JButton browseFileButton;
	private JButton addFileButton;
	private JButton replaceWithFileButton;
	private JButton saveFileButton;

	public SrsPanel(ViewController viewController, Config config) {
		super(config);
		this.viewController = viewController;

		dbPool = DatabaseConnectionPool.getInstance();

		EventDispatcher eventDispatcher = ObjectRegistry.getInstance().getEventDispatcher();
		eventDispatcher.addEventHandler(EventType.DATABASE_CONNECTION_STATE, this);
		eventDispatcher.addEventHandler(EventType.PROPERTY_CHANGE_EVENT, this);

		initGui();
	}

	@Override
	public boolean isModified() {
		try { sridText.commitEdit(); } catch (ParseException ignored) { }

		DatabaseSrs refSys = srsComboBox.getSelectedItem();
		if (refSys != null) {
			if (((Number) sridText.getValue()).intValue() != refSys.getSrid()) return true;
			if (!srsNameComboBox.getText().equals(refSys.getGMLSrsName())) return true;
			if (!descriptionText.getText().equals(refSys.getDescription())) return true;
		}

		return false;
	}

	private void initGui() {
		srsComboBoxLabel = new JLabel();
		sridLabel = new JLabel();

		NumberFormatter sridFormat = new NumberFormatter(new DecimalFormat("#"));
		sridFormat.setMaximum(Integer.MAX_VALUE);
		sridFormat.setMinimum(0);
		sridText = new JFormattedTextField(sridFormat);
		sridText.setValue(0);

		srsNameLabel = new JLabel();
		srsNameComboBox = new SrsNameComboBox();
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
		dbSrsTypeText = new JTextField();
		dbSrsNameLabel = new JLabel();
		dbSrsNameText = new JTextField();
		dbSrsTypeText.setEditable(false);
		dbSrsNameText.setEditable(false);

		srsComboBoxFactory = SrsComboBoxFactory.getInstance();
		srsComboBox = srsComboBoxFactory.createSrsComboBox(false);

		PopupMenuDecorator.getInstance().decorate(sridText, (JTextField) srsNameComboBox.getEditor().getEditorComponent(),
				descriptionText, fileText, dbSrsTypeText, dbSrsNameText);

		sridText.addPropertyChangeListener("value", e -> {
			int srid = sridText.getValue() != null ? ((Number) sridText.getValue()).intValue() : 0;
			srsNameComboBox.updateSrid(srid);
		});

		setLayout(new GridBagLayout());
		{
			JPanel content = new JPanel();
			content.setLayout(new GridBagLayout());
			{
				JPanel srsPanel = new JPanel();
				srsPanel.setLayout(new GridBagLayout());
				srsPanel.add(srsComboBoxLabel, GuiUtil.setConstraints(0, 0, 0, 1, GridBagConstraints.BOTH, 0, 0, 20, 5));
				srsPanel.add(srsComboBox, GuiUtil.setConstraints(1, 0, 2, 1, 1, 1, GridBagConstraints.BOTH, 0, 5, 20, 0));
				srsPanel.add(sridLabel, GuiUtil.setConstraints(0, 1, 0, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
				srsPanel.add(sridText, GuiUtil.setConstraints(1, 1, 1, 0, GridBagConstraints.HORIZONTAL, 0, 5, 0, 5));
				srsPanel.add(checkButton, GuiUtil.setConstraints(2, 1, 0, 0, GridBagConstraints.HORIZONTAL, 0, 5, 0, 0));
				srsPanel.add(srsNameLabel, GuiUtil.setConstraints(0, 2, 0, 0, GridBagConstraints.BOTH, 5, 0, 0, 5));
				srsPanel.add(srsNameComboBox, GuiUtil.setConstraints(1, 2, 2, 1, 1, 0, GridBagConstraints.HORIZONTAL, 5, 5, 0, 0));
				srsPanel.add(descriptionLabel, GuiUtil.setConstraints(0, 3, 0, 0, GridBagConstraints.BOTH, 5, 0, 0, 5));
				srsPanel.add(descriptionText, GuiUtil.setConstraints(1, 3, 2, 1, 1, 0, GridBagConstraints.HORIZONTAL, 5, 5, 0, 0));
				srsPanel.add(dbSrsNameLabel, GuiUtil.setConstraints(0, 4, 0, 0, GridBagConstraints.HORIZONTAL, 5, 0, 0, 5));
				srsPanel.add(dbSrsNameText, GuiUtil.setConstraints(1, 4, 2, 1, 1, 0, GridBagConstraints.BOTH, 5, 5, 0, 0));
				srsPanel.add(dbSrsTypeLabel, GuiUtil.setConstraints(0, 5, 0, 0, GridBagConstraints.HORIZONTAL, 5, 0, 5, 5));
				srsPanel.add(dbSrsTypeText, GuiUtil.setConstraints(1, 5, 2, 1, 1, 0, GridBagConstraints.BOTH, 5, 5, 5, 0));

				Box buttonsPanel = Box.createHorizontalBox();
				buttonsPanel.add(applyButton);
				buttonsPanel.add(Box.createHorizontalStrut(10));
				buttonsPanel.add(newButton);
				buttonsPanel.add(Box.createHorizontalStrut(10));
				buttonsPanel.add(copyButton);
				buttonsPanel.add(Box.createHorizontalStrut(10));
				buttonsPanel.add(deleteButton);

				srsPanel.add(buttonsPanel, GuiUtil.setConstraints(0, 6, 3, 1, 0, 0, GridBagConstraints.NONE, 10, 0, 0, 0));
				content.add(srsPanel, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
			}

			contentsPanel = new TitledPanel().build(content);
		}
		{
			JPanel content = new JPanel();
			content.setLayout(new GridBagLayout());
			{
				JPanel browse = new JPanel();
				browse.setLayout(new GridBagLayout());
				{
					browse.add(fileLabel, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 5));
					browse.add(fileText, GuiUtil.setConstraints(1, 0, 1, 0, GridBagConstraints.HORIZONTAL, 0, 5, 0, 5));
					browse.add(browseFileButton, GuiUtil.setConstraints(2, 0, 0, 0, GridBagConstraints.BOTH, 0, 5, 0, 0));
				}

				JPanel button = new JPanel();
				button.setLayout(new GridBagLayout());
				{
					button.add(addFileButton, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.NONE, 0, 0, 0, 5));
					button.add(replaceWithFileButton, GuiUtil.setConstraints(1, 0, 0, 0, GridBagConstraints.NONE, 0, 5, 0, 5));
					button.add(saveFileButton, GuiUtil.setConstraints(2, 0, 0, 0, GridBagConstraints.NONE, 0, 5, 0, 0));
				}

				content.add(browse, GuiUtil.setConstraints(0, 0, 1, 1, GridBagConstraints.BOTH, 0, 0, 0, 0));
				content.add(button, GuiUtil.setConstraints(0, 1, 0, 1, GridBagConstraints.BOTH, 15, 0, 0, 0));
			}

			impExpPanel = new TitledPanel().build(content);
		}

		add(contentsPanel, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
		add(impExpPanel, GuiUtil.setConstraints(0, 1, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));

		DropTarget dropTarget = new DropTarget(fileText, this);
		fileText.setDropTarget(dropTarget);
		impExpPanel.setDropTarget(dropTarget);

		srsComboBoxListener = e -> displaySelectedValues();
		srsComboBox.addActionListener(srsComboBoxListener);

		newButton.addActionListener(e -> {
			if (requestChange()) {
				DatabaseSrs refSys = DatabaseSrs.createDefaultSrs();
				refSys.setDescription(getNewRefSysDescription());
				refSys.setSupported(!dbPool.isConnected());

				config.getDatabaseConfig().addReferenceSystem(refSys);
				updateSrsComboBoxes(false);
				srsComboBox.setSelectedItem(refSys);

				displaySelectedValues();
			}
		});

		copyButton.addActionListener(e -> {
			if (requestChange()) {
				DatabaseSrs orig = srsComboBox.getSelectedItem();
				if (orig != null) {
					DatabaseSrs copy = new DatabaseSrs(orig);
					copy.setDescription(getCopyOfDescription(orig));

					config.getDatabaseConfig().addReferenceSystem(copy);
					updateSrsComboBoxes(false);
					srsComboBox.setSelectedItem(copy);

					displaySelectedValues();
				}
			}
		});

		applyButton.addActionListener(e -> {
			setSettings();
			log.info("Settings successfully applied.");
		});

		deleteButton.addActionListener(e -> {
			DatabaseSrs refSys = srsComboBox.getSelectedItem();
			if (refSys != null) {
				int option = viewController.showOptionDialog(Language.I18N.getString("pref.db.srs.dialog.delete.title"),
						MessageFormat.format(Language.I18N.getString("pref.db.srs.dialog.delete.msg"), refSys.getDescription()),
						JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

				if (option == JOptionPane.YES_OPTION) {
					int index = srsComboBox.getSelectedIndex();
					config.getDatabaseConfig().getReferenceSystems().remove(refSys);
					updateSrsComboBoxes(false);
					srsComboBox.setSelectedIndex(index < srsComboBox.getItemCount() ? index : index - 1);
					displaySelectedValues();
				}
			}
		});

		checkButton.addActionListener(e -> {
			int srid = 0;

			try {
				sridText.commitEdit();
				srid = ((Number) sridText.getValue()).intValue();
			} catch (ParseException pe) {
				//
			}

			try {
				DatabaseSrs tmp = DatabaseSrs.createDefaultSrs();
				tmp.setSrid(srid);
				dbPool.getActiveDatabaseAdapter().getUtil().getSrsInfo(tmp);
				if (tmp.isSupported()) {
					log.info("SRID " + srid + " is supported.");
					log.info("Database name: " + tmp.getDatabaseSrsName());
					log.info("SRS type: " + tmp.getType());
				} else
					log.warn("SRID " + srid + " is NOT supported.");
			} catch (SQLException sqlEx) {
				log.error("Error while checking user-defined SRSs: " + sqlEx.getMessage().trim());
			}
		});

		browseFileButton.addActionListener(e -> browseReferenceSystemFile(Language.I18N.getString("pref.db.srs.label.file")));
		addFileButton.addActionListener(e -> importReferenceSystems(false));
		replaceWithFileButton.addActionListener(e -> importReferenceSystems(true));
		saveFileButton.addActionListener(e -> exportReferenceSystems());
	}

	@Override
	public void switchLocale(Locale locale) {
		contentsPanel.setTitle(Language.I18N.getString("pref.db.srs.border.currentlySupported"));
		impExpPanel.setTitle(Language.I18N.getString("pref.db.srs.border.impexp"));

		srsComboBoxLabel.setText(Language.I18N.getString("common.label.boundingBox.crs"));		
		sridLabel.setText(Language.I18N.getString("pref.db.srs.label.srid"));
		srsNameLabel.setText(Language.I18N.getString("pref.db.srs.label.srsName"));
		descriptionLabel.setText(Language.I18N.getString("pref.db.srs.label.description"));
		dbSrsTypeLabel.setText(Language.I18N.getString("pref.db.srs.label.dbSrsType"));		
		dbSrsNameLabel.setText(Language.I18N.getString("pref.db.srs.label.dbSrsName"));		
		newButton.setText(Language.I18N.getString("pref.db.srs.button.new"));
		applyButton.setText(Language.I18N.getString("common.button.apply"));
		deleteButton.setText(Language.I18N.getString("pref.db.srs.button.delete"));
		copyButton.setText(Language.I18N.getString("pref.db.srs.button.copy"));		
		checkButton.setText(Language.I18N.getString("pref.db.srs.button.check"));

		fileLabel.setText(Language.I18N.getString("pref.db.srs.label.file"));
		browseFileButton.setText(Language.I18N.getString("common.button.browse"));
		addFileButton.setText(Language.I18N.getString("pref.db.srs.button.addFile"));
		replaceWithFileButton.setText(Language.I18N.getString("pref.db.srs.button.replaceWithFile"));
		saveFileButton.setText(Language.I18N.getString("pref.db.srs.button.saveFile"));
	}

	@Override
	public void resetSettings() {
		config.getDatabaseConfig().addDefaultReferenceSystems();
		srsComboBoxFactory.updateAll(true);
	}

	@Override
	public void loadSettings() {
		displaySelectedValues();
	}

	@Override
	public void setSettings() {
		DatabaseSrs refSys = srsComboBox.getSelectedItem();
		if (refSys != null) {
			int prev = refSys.getSrid();

			refSys.setSrid(((Number) sridText.getValue()).intValue());
			if (dbPool.isConnected() && prev != refSys.getSrid()) {
				try {
					dbPool.getActiveDatabaseAdapter().getUtil().getSrsInfo(refSys);

					if (refSys.isSupported())
						log.debug("SRID " + refSys.getSrid() + " is supported.");
					else
						log.warn("SRID " + refSys.getSrid() + " is NOT supported.");
				} catch (SQLException sqlEx) {
					log.error("Error while checking user-defined SRSs: " + sqlEx.getMessage().trim());
				}
			}

			refSys.setGMLSrsName(srsNameComboBox.getText().trim());
			refSys.setDescription(descriptionText.getText().trim());
			if (refSys.getDescription().length() == 0)
				refSys.setDescription(getNewRefSysDescription());

			updateSrsComboBoxes(true);
			displaySelectedValues();
		}
	}

	@Override
	public String getLocalizedTitle() {
		return Language.I18N.getString("pref.tree.db.srs");
	}

	private void displaySelectedValues() {
		DatabaseSrs refSys = srsComboBox.getSelectedItem();
		if (refSys == null) 
			return;

		sridText.setValue(refSys.getSrid());
		if (refSys.getSrid() == 0)
			sridText.setText("");

		srsNameComboBox.setText(refSys.getGMLSrsName());
		descriptionText.setText(refSys.toString());
		dbSrsNameText.setText(wrap(refSys.getDatabaseSrsName(), 80));
		dbSrsTypeText.setText(refSys.getType().toString());

		boolean isEditable = !srsComboBox.isDBReferenceSystemSelected();
		sridText.setEditable(isEditable);
		srsNameComboBox.setEnabled(isEditable);
		descriptionText.setEditable(isEditable);
		applyButton.setEnabled(isEditable);
		deleteButton.setEnabled(isEditable);
		copyButton.setEnabled(dbPool.isConnected() || isEditable);
	}

	private String wrap(String in, int len) {
		if (in == null || in.length() <= len)
			return in;

		int wrapAt = Math.max(in.substring(0, len).lastIndexOf(' '), in.substring(0, len).lastIndexOf('-'));
		return wrapAt == -1 ?
				in.substring(0, len) + '-' + System.getProperty("line.separator") + wrap(in.substring(len).trim(), len) :
				in.substring(0, wrapAt) + System.getProperty("line.separator") + wrap(in.substring(wrapAt).trim(), len);
	}

	private String getCopyOfDescription(DatabaseSrs refSys) {
		// pattern: "referenceSystemName - copy 1"
		// so to retrieve referenceSystem, " - copy*" has to be deleted...

		int nr = 0;
		String name = refSys.getDescription().replaceAll("\\s*-\\s*" + Language.I18N.getString("pref.db.srs.label.copyReferenceSystem") + ".*$", "");
		String copy = name + " - " + Language.I18N.getString("pref.db.srs.label.copyReferenceSystem");

		if (Language.I18N.getString("common.label.boundingBox.crs.sameAsInDB").replaceAll("\\s*-\\s*" + Language.I18N.getString("pref.db.srs.label.copyReferenceSystem") + ".*$", "").equalsIgnoreCase(name))
			nr++;

		for (DatabaseSrs tmp : config.getDatabaseConfig().getReferenceSystems())
			if (tmp.getDescription().replaceAll("\\s*-\\s*" + Language.I18N.getString("pref.db.srs.label.copyReferenceSystem") + ".*$", "").equalsIgnoreCase(name))
				nr++;

		if (nr > 1)
			return copy + " " + nr;
		else
			return copy;
	}

	private String getNewRefSysDescription() {
		int nr = 1;
		String name = Language.I18N.getString("pref.db.srs.label.newReferenceSystem");
		for (DatabaseSrs refSys : config.getDatabaseConfig().getReferenceSystems())
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
			viewController.clearConsole();
			viewController.setStatusText(Language.I18N.getString("main.status.database.srs.import.label"));

			File file = new File(fileText.getText().trim());
			String msg = "";

			if (replace)
				msg += "Replacing reference systems with those from file '";
			else
				msg += "Adding reference systems from file '";

			log.info(msg + file.getAbsolutePath() + "'.");

			if (!file.exists() || !file.isFile() || !file.canRead()) {
				log.error("Failed to open reference system file.");
				viewController.errorMessage(Language.I18N.getString("common.dialog.error.io.title"), 
						MessageFormat.format(Language.I18N.getString("common.dialog.file.read.error"), Language.I18N.getString("pref.db.srs.error.read.msg")));
				return;
			}

			Object object = ConfigUtil.getInstance().unmarshal(file);
			if (object instanceof DatabaseSrsList) {
				DatabaseSrsList refSyss = (DatabaseSrsList)object;				
				if (replace)
					config.getDatabaseConfig().getReferenceSystems().clear();

				if (dbPool.isConnected())
					log.info("Checking whether reference systems are supported by database profile.");

				for (DatabaseSrs refSys : refSyss.getItems()) {
					msg = "Adding reference system '" + refSys.getDescription() + "' (SRID: " + refSys.getSrid() + ").";

					if (dbPool.isConnected()) {
						try {
							dbPool.getActiveDatabaseAdapter().getUtil().getSrsInfo(refSys);
							if (!refSys.isSupported())
								msg += " (NOT supported)";
							else
								msg += " (supported)";

						} catch (SQLException sqlEx) {
							log.error("Error while checking user-defined SRSs: " + sqlEx.getMessage().trim());
						}
					}

					config.getDatabaseConfig().getReferenceSystems().add(refSys);
					log.info(msg);
				}

				updateSrsComboBoxes(true);
				displaySelectedValues();

				log.info("Reference systems successfully imported from file '" + file.getAbsolutePath() + "'.");
			} else
				log.error("Could not find reference system definitions in file '" + file.getAbsolutePath() + "'.");

		} catch (JAXBException jaxb) {
			String msg = jaxb.getMessage();
			if (msg == null && jaxb.getLinkedException() != null)
				msg = jaxb.getLinkedException().getMessage();

			log.error("Failed to parse file: " + msg);
			viewController.errorMessage(Language.I18N.getString("common.dialog.error.io.title"), 
					MessageFormat.format(Language.I18N.getString("common.dialog.file.read.error"), msg));
		} catch (IOException e) {
			String msg = e.getMessage();
			log.error("Failed to access file: " + msg);
			viewController.errorMessage(Language.I18N.getString("common.dialog.error.io.title"), 
					MessageFormat.format(Language.I18N.getString("common.dialog.file.read.error"), msg));			
		} finally {
			viewController.setStatusText(Language.I18N.getString("main.status.ready.label"));
		}
	}

	private void exportReferenceSystems() {		
		try {
			setSettings();

			if (config.getDatabaseConfig().getReferenceSystems().isEmpty()) {
				log.error("There are no user-defined reference systems to be exported.");
				return;
			}

			viewController.clearConsole();
			viewController.setStatusText(Language.I18N.getString("main.status.database.srs.export.label"));

			String fileName = fileText.getText().trim();
			if (fileName.length() == 0) {
				log.error("Please specify the export file for the reference systems.");
				viewController.errorMessage(Language.I18N.getString("common.dialog.error.io.title"), Language.I18N.getString("pref.db.srs.error.write.msg"));
				return;
			}

			if ((!fileName.contains("."))) {
				fileName += ".xml";
				fileText.setText(fileName);
			}

			File file = new File(fileName);
			log.info("Writing reference systems to file '" + file.getAbsolutePath() + "'.");

			DatabaseSrsList refSys = new DatabaseSrsList();
			for (DatabaseSrs tmp : config.getDatabaseConfig().getReferenceSystems()) {
				DatabaseSrs copy = new DatabaseSrs(tmp);
				copy.setId(null);				
				refSys.addItem(copy);

				log.info("Writing reference system '" + tmp.getDescription() + "' (SRID: " + tmp.getSrid() + ").");
			}

			ConfigUtil.getInstance().marshal(refSys, file);
			log.info("Reference systems successfully written to file '" + file.getAbsolutePath() + "'.");
		} catch (JAXBException jaxb) {
			String msg = jaxb.getMessage();
			if (msg == null && jaxb.getLinkedException() != null)
				msg = jaxb.getLinkedException().getMessage();

			log.error("Failed to write file: " + msg);
			viewController.errorMessage(Language.I18N.getString("common.dialog.error.io.title"), 
					MessageFormat.format(Language.I18N.getString("common.dialog.file.write.error"), msg));
		} finally {
			viewController.setStatusText(Language.I18N.getString("main.status.ready.label"));
		}
	}

	private boolean requestChange() {
		if (isModified()) {
			DatabaseSrs refSys = srsComboBox.getSelectedItem();
			String description = refSys != null ? refSys.getDescription() : "";

			int option = viewController.showOptionDialog(Language.I18N.getString("pref.db.srs.apply.title"),
					MessageFormat.format(Language.I18N.getString("pref.db.srs.apply.msg"), description),
					JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

			switch (option) {
				case JOptionPane.CANCEL_OPTION:
					return false;
				case JOptionPane.YES_OPTION:
					setSettings();
					break;
				default:
					loadSettings();
					break;
			}
		}

		return true;
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
			chooser.setCurrentDirectory(CoreConstants.IMPEXP_HOME.resolve(CliConstants.SRS_TEMPLATES_DIR).toFile());

		int result = chooser.showOpenDialog(getTopLevelAncestor());
		if (result == JFileChooser.CANCEL_OPTION) 
			return;

		fileText.setText(chooser.getSelectedFile().toString());
	}

	@Override
	public void handleEvent(Event event) throws Exception {
		boolean isConnected;

		if (event.getEventType() == EventType.DATABASE_CONNECTION_STATE)
			isConnected = ((DatabaseConnectionStateEvent)event).isConnected();
		else if (event.getEventType() == EventType.PROPERTY_CHANGE_EVENT
				&& ((PropertyChangeEvent)event).getPropertyName().equals(SrsOperation.DB_SRS_CHANGED_PROPERTY))
			isConnected = true;
		else
			return;

		if (!isConnected) {
			DatabaseSrs tmp = DatabaseSrs.createDefaultSrs();
			for (DatabaseSrs refSys : config.getDatabaseConfig().getReferenceSystems()) {
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
				} catch (UnsupportedFlavorException | IOException e) {
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

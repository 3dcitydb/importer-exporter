/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2017
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.gis.bgu.tum.de/
 * 
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 * 
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
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
package org.citydb.modules.database.gui.operations;

import org.citydb.config.Config;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.database.DBOperationType;
import org.citydb.config.project.database.DatabaseSrs;
import org.citydb.database.connection.DatabaseConnectionPool;
import org.citydb.database.connection.DatabaseMetaData;
import org.citydb.event.global.DatabaseConnectionStateEvent;
import org.citydb.event.global.PropertyChangeEvent;
import org.citydb.gui.components.dialog.StatusDialog;
import org.citydb.gui.factory.SrsComboBoxFactory;
import org.citydb.gui.util.GuiUtil;
import org.citydb.log.Logger;
import org.citydb.plugin.extension.view.ViewController;
import org.citydb.registry.ObjectRegistry;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.concurrent.locks.ReentrantLock;

public class SrsOperation extends DatabaseOperationView {
	public static final String DB_SRS_CHANGED_PROPERTY = "dbSrsChanged";

	private final ReentrantLock mainLock = new ReentrantLock();
	private final Logger log = Logger.getInstance();
	private final ViewController viewController;
	private final DatabaseConnectionPool dbConnectionPool;
	private final Config config;

	private JPanel component;
	private JLabel sridLabel;
	private JFormattedTextField sridText;
	private JLabel srsNameLabel;
	private JComboBox<String> srsNameComboBox;
	private SrsNameEditor srsNameEditor;
	private JButton checkSridButton;
	private JButton editSridButton;
	private JLabel geometriesLabel;
	private JRadioButton transformButton;
	private JRadioButton metadataButton;
	private JButton restoreButton;
	private JButton applyButton;

	public SrsOperation(DatabaseOperationsPanel parent, Config config) {
		this.config = config;

		viewController = parent.getViewController();
		dbConnectionPool = DatabaseConnectionPool.getInstance();

		init();
	}

	private void init() {
		component = new JPanel();
		component.setLayout(new GridBagLayout());

		sridLabel = new JLabel();
		srsNameLabel = new JLabel();
		geometriesLabel = new JLabel();

		DecimalFormat tileFormat = new DecimalFormat("##########");
		tileFormat.setMaximumIntegerDigits(10);
		tileFormat.setMinimumIntegerDigits(1);
		sridText = new JFormattedTextField(tileFormat);
		sridText.setValue(0);
		sridText.setText("");

		srsNameEditor = new SrsNameEditor();
		srsNameComboBox = new JComboBox<>();
		srsNameComboBox.setEditor(srsNameEditor);
		srsNameComboBox.addItem("urn:ogc:def:crs:EPSG::<SRID>");
		srsNameComboBox.addItem("urn:ogc:def:crs,crs:EPSG::<SRID>,crs:EPSG::<HEIGHT_SRID>");
		srsNameComboBox.addItem("EPSG:<SRID>");

		checkSridButton = new JButton();
		editSridButton = new JButton();

		metadataButton = new JRadioButton();
		transformButton = new JRadioButton();
		metadataButton.setIconTextGap(10);
		transformButton.setIconTextGap(10);

		ButtonGroup group = new ButtonGroup();
		group.add(transformButton);
		group.add(metadataButton);

		Box dbContentPanel = Box.createHorizontalBox();
		dbContentPanel.add(transformButton);
		dbContentPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		dbContentPanel.add(metadataButton);
		transformButton.setSelected(true);

		restoreButton = new JButton();
		applyButton = new JButton();

		Box buttonsPanel = Box.createHorizontalBox();
		buttonsPanel.add(restoreButton);
		buttonsPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		buttonsPanel.add(applyButton);

		component.add(sridLabel, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.BOTH, 10, 5, 5, 5));
		component.add(sridText, GuiUtil.setConstraints(1, 0, 1, 0, GridBagConstraints.HORIZONTAL, 10, 5, 5, 5));
		component.add(editSridButton, GuiUtil.setConstraints(2, 0, 0, 0, GridBagConstraints.HORIZONTAL, 10, 5, 5, 5));
		component.add(checkSridButton, GuiUtil.setConstraints(3, 0, 0, 0, GridBagConstraints.HORIZONTAL, 10, 5, 5, 5));
		component.add(srsNameLabel, GuiUtil.setConstraints(0, 1, 0, 0, GridBagConstraints.BOTH, 0, 5, 5, 5));
		component.add(srsNameComboBox, GuiUtil.setConstraints(1, 1, 1, 0, GridBagConstraints.HORIZONTAL, 0, 5, 5, 5));
		component.add(geometriesLabel, GuiUtil.setConstraints(0, 2, 0, 0, GridBagConstraints.BOTH, 0, 5, 5, 5));
		component.add(dbContentPanel, GuiUtil.setConstraints(1, 2, 0, 0, GridBagConstraints.BOTH, 0, 5, 5, 5));
		component.add(buttonsPanel, GuiUtil.setConstraints(0, 3, 4, 1, 0, 0, GridBagConstraints.NONE, 5, 5, 10, 5));

		sridText.addPropertyChangeListener(e -> {
			int srid = ((Number) sridText.getValue()).intValue();
			if (srid < 0 || srid == Integer.MAX_VALUE)
				sridText.setValue(0);
		});

		editSridButton.addActionListener(l -> {
			sridText.setEditable(true);
			checkSridButton.setEnabled(true);
			geometriesLabel.setEnabled(true);
			transformButton.setEnabled(true);
			metadataButton.setEnabled(true);
		});

		checkSridButton.addActionListener(e -> {
			DatabaseSrs srs = checkSrid();
			if (srs.isSupported()) {
				log.info("SRID " + srs.getSrid() + " is supported.");
				log.info("Database name: " + srs.getDatabaseSrsName());
				log.info("SRS type: " + srs.getType());
			} else
				log.warn("SRID " + srs.getSrid() + " is NOT supported.");
		});

		restoreButton.addActionListener(l -> {
			if (dbConnectionPool.isConnected()) {
				DatabaseSrs srs = dbConnectionPool.getActiveDatabaseAdapter().getConnectionMetaData().getReferenceSystem();
				sridText.setValue(srs.getSrid());
				srsNameEditor.setText(srs.getGMLSrsName());
			}
		});

		applyButton.addActionListener(l -> {
			new SwingWorker<Void, Void>() {
				protected Void doInBackground() {
					doOperation();
					return null;
				}
			}.execute();
		});
	}

	@Override
	public String getLocalizedTitle() {
		return Language.I18N.getString("db.label.operation.srs");
	}

	@Override
	public Component getViewComponent() {
		return component;
	}

	@Override
	public String getToolTip() {
		return null;
	}

	@Override
	public Icon getIcon() {
		return null;
	}

	@Override
	public DBOperationType getType() {
		return DBOperationType.SRS;
	}

	@Override
	public void doTranslation() {
		sridLabel.setText(Language.I18N.getString("pref.db.srs.label.srid"));
		srsNameLabel.setText(Language.I18N.getString("pref.db.srs.label.srsName"));
		checkSridButton.setText(Language.I18N.getString("pref.db.srs.button.check"));
		editSridButton.setText(Language.I18N.getString("common.button.edit"));
		geometriesLabel.setText(Language.I18N.getString("db.label.operation.srs.geometries"));
		transformButton.setText(Language.I18N.getString("db.label.operation.srs.transform"));
		metadataButton.setText(Language.I18N.getString("db.label.operation.srs.updateMetadata"));
		restoreButton.setText(Language.I18N.getString("common.button.restore"));
		applyButton.setText(Language.I18N.getString("common.button.apply"));
	}

	@Override
	public void setEnabled(boolean enable) {
		sridLabel.setEnabled(enable);
		sridText.setEnabled(enable);
		checkSridButton.setEnabled(false);
		editSridButton.setEnabled(enable);
		srsNameLabel.setEnabled(enable);
		srsNameComboBox.setEnabled(enable);
		geometriesLabel.setEnabled(false);
		transformButton.setEnabled(false);
		metadataButton.setEnabled(false);
		restoreButton.setEnabled(enable);
		applyButton.setEnabled(enable);

		sridText.setEditable(false);
		srsNameComboBox.setEditable(enable);
		srsNameComboBox.setSelectedItem(null);
	}

	@Override
	public void loadSettings() {
		// nothing to do here...
	}

	@Override
	public void setSettings() {
		// nothing to do here...
	}

	private DatabaseSrs checkSrid() {
		DatabaseSrs srs = DatabaseSrs.createDefaultSrs();

		try {
			sridText.commitEdit();
			srs.setSrid(((Number) sridText.getValue()).intValue());
			dbConnectionPool.getActiveDatabaseAdapter().getUtil().getSrsInfo(srs);
		} catch (SQLException | ParseException e) {
			log.error("Error while checking SRID: " + e.getMessage().trim());
		}

		return srs;
	}

	private void doOperation() {
		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			DatabaseSrs dbSrs = dbConnectionPool.getActiveDatabaseAdapter().getConnectionMetaData().getReferenceSystem();
			DatabaseSrs srs = checkSrid();
			srs.setGMLSrsName(srsNameEditor.getText());
			boolean changeSrid = srs.getSrid() != dbSrs.getSrid();

			if (!changeSrid && srs.getGMLSrsName().equals(dbSrs.getGMLSrsName())) {
				log.info("No changes made to the database reference system.");
				return;
			}

			if (!srs.isSupported()) {
				viewController.errorMessage(Language.I18N.getString("db.dialog.srs.window"),
						MessageFormat.format(Language.I18N.getString("db.dialog.error.sridNotSupported"),
						String.valueOf(srs.getSrid())));
				return;
			}

			if (changeSrid && config.getGui().isShowChangeSridWarning()) {
				JPanel confirmPanel = new JPanel(new GridBagLayout());
				JCheckBox confirmDialogNoShow = new JCheckBox(Language.I18N.getString("common.dialog.msg.noShow"));
				confirmDialogNoShow.setIconTextGap(10);

				JLabel headerLabel = new JLabel(Language.I18N.getString("db.dialog.srs.changeSrid"));
				JLabel sridLabel = new JLabel(Language.I18N.getString("pref.db.srs.label.srid") + ":");
				JLabel sridValue = new JLabel(String.valueOf(srs.getSrid()));
				JLabel geometriesLabel = new JLabel(Language.I18N.getString("db.label.operation.srs.geometries") + ":");
				JLabel geometriesValue = new JLabel((transformButton.isSelected() ?
						Language.I18N.getString("db.label.operation.srs.transform") :
						Language.I18N.getString("db.label.operation.srs.updateMetadata")));

				sridValue.setFont(sridValue.getFont().deriveFont(Font.BOLD));
				geometriesValue.setFont(sridValue.getFont());

				confirmPanel.add(headerLabel, GuiUtil.setConstraints(0, 0, 2, 1, 1, 0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 0));
				confirmPanel.add(sridLabel, GuiUtil.setConstraints(0, 1, 0, 0, GridBagConstraints.HORIZONTAL, 10, 0, 0, 0));
				confirmPanel.add(sridValue, GuiUtil.setConstraints(1, 1, 1, 0, GridBagConstraints.HORIZONTAL, 10, 5, 0, 0));
				confirmPanel.add(geometriesLabel, GuiUtil.setConstraints(0, 2, 0, 0, GridBagConstraints.HORIZONTAL, 5, 0, 0, 0));
				confirmPanel.add(geometriesValue, GuiUtil.setConstraints(1, 2, 1, 0, GridBagConstraints.HORIZONTAL, 5, 5, 0, 0));
				confirmPanel.add(confirmDialogNoShow, GuiUtil.setConstraints(0, 3, 2, 1, 1, 0, GridBagConstraints.BOTH, 10, 0, 0, 0));

				int result = JOptionPane.showConfirmDialog(viewController.getTopFrame(),
						confirmPanel,
						Language.I18N.getString("db.dialog.srs.window"),
						JOptionPane.YES_NO_CANCEL_OPTION);

				config.getGui().setShowChangeSridWarning(!confirmDialogNoShow.isSelected());
				if (result != JOptionPane.YES_OPTION)
					return;
			}

			viewController.setStatusText(Language.I18N.getString("main.status.database.srs.change.label"));

			StatusDialog srsDialog = new StatusDialog(viewController.getTopFrame(),
					Language.I18N.getString("db.dialog.srs.window"),
					Language.I18N.getString("db.dialog.srs.srid.title"),
					null,
					Language.I18N.getString("db.dialog.srs.srid.details"),
					true);

			if (changeSrid) {
				SwingUtilities.invokeLater(() -> {
					srsDialog.setLocationRelativeTo(viewController.getTopFrame());
					srsDialog.setVisible(true);
				});

				srsDialog.getButton().addActionListener(e -> SwingUtilities.invokeLater(
						() -> dbConnectionPool.getActiveDatabaseAdapter().getUtil().interruptDatabaseOperation()));
			}

			try {
				log.info("Changing database " + (changeSrid ? "reference system..." : "gml:srsName..."));

				String schema = dbConnectionPool.getActiveDatabaseAdapter().getConnectionDetails().getSchema();
				dbConnectionPool.getActiveDatabaseAdapter().getUtil().changeSrs(srs, transformButton.isSelected(), schema);

				// change database SRS
				if (changeSrid) {
					DatabaseMetaData metaData = dbConnectionPool.getActiveDatabaseAdapter().getUtil().getDatabaseInfo();
					dbSrs.setSrid(metaData.getReferenceSystem().getSrid());
					dbSrs.setGMLSrsName(metaData.getReferenceSystem().getGMLSrsName());
					dbSrs.setDatabaseSrsName(metaData.getReferenceSystem().getDatabaseSrsName());
					dbSrs.setWkText(metaData.getReferenceSystem().getWkText());
				} else
					dbSrs.setGMLSrsName(srs.getGMLSrsName());

				// reset contents of SRS combo boxes
				SrsComboBoxFactory.getInstance(config).resetAll(true);

				// trigger SRS property event
				ObjectRegistry.getInstance().getEventDispatcher().triggerEvent(
						new PropertyChangeEvent(DB_SRS_CHANGED_PROPERTY, false, true, this));

				// print result
				log.info("SRID: " + srs.getSrid() + " (" + srs.getType() + ')');
				log.info("gml:srsName: " + srs.getGMLSrsName());

				log.info("Changing database " + (changeSrid ? "reference system" : "gml:srsName") + " successfully finished.");
				SwingUtilities.invokeLater(srsDialog::dispose);
			} catch (SQLException e) {
				SwingUtilities.invokeLater(srsDialog::dispose);
				String sqlExMsg = e.getMessage().trim();

				JOptionPane.showMessageDialog(
						viewController.getTopFrame(),
						MessageFormat.format(Language.I18N.getString("db.dialog.srs.error"), sqlExMsg),
						Language.I18N.getString("common.dialog.error.db.title"),
						JOptionPane.ERROR_MESSAGE);

				log.error("SQL error: " + sqlExMsg);
			} finally {
				viewController.setStatusText(Language.I18N.getString("main.status.ready.label"));
			}

		} finally {
			lock.unlock();
		}
	}

	private class SrsNameEditor implements ComboBoxEditor {
		private final JTextField component;

		private SrsNameEditor() {
			component = new JTextField();
			component.setBorder(new EmptyBorder(0, 2, 0,2));
		}

		public void setText(String value) {
			component.setText(value);
		}

		public String getText() {
			return component.getText();
		}

		@Override
		public Component getEditorComponent() {
			return component;
		}

		@Override
		public void setItem(Object anObject) {
			if (anObject != null)
				component.setText(anObject.toString().replace("<SRID>", String.valueOf(sridText.getValue())));
		}

		@Override
		public Object getItem() {
			return component.getText();
		}

		@Override
		public void selectAll() { }

		@Override
		public void addActionListener(ActionListener ignored) { }

		@Override
		public void removeActionListener(ActionListener ignored) { }
	}

	@Override
	public void handleDatabaseConnectionStateEvent(DatabaseConnectionStateEvent event) {
		if (event.isConnected()) {
			DatabaseSrs srs = dbConnectionPool.getActiveDatabaseAdapter().getConnectionMetaData().getReferenceSystem();
			sridText.setValue(srs.getSrid());
			srsNameEditor.setText(srs.getGMLSrsName());
		} else {
			sridText.setText("");
			srsNameEditor.setText("");
		}
	}
}

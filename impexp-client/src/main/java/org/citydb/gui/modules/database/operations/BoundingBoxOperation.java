/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2020
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
package org.citydb.gui.modules.database.operations;

import org.citydb.ade.ADEExtension;
import org.citydb.ade.ADEExtensionManager;
import org.citydb.config.Config;
import org.citydb.config.geometry.BoundingBox;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.database.DatabaseOperationType;
import org.citydb.config.project.exporter.SimpleQuery;
import org.citydb.config.project.query.filter.type.FeatureTypeFilter;
import org.citydb.database.connection.DatabaseConnectionPool;
import org.citydb.database.schema.mapping.AbstractPathElement;
import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.database.schema.mapping.SchemaMapping;
import org.citydb.event.global.DatabaseConnectionStateEvent;
import org.citydb.event.global.ProgressBarEventType;
import org.citydb.event.global.StatusDialogProgressBar;
import org.citydb.gui.components.bbox.BoundingBoxClipboardHandler;
import org.citydb.gui.components.common.TitledPanel;
import org.citydb.gui.components.dialog.StatusDialog;
import org.citydb.gui.modules.common.filter.FeatureVersionFilterView;
import org.citydb.gui.util.GuiUtil;
import org.citydb.log.Logger;
import org.citydb.plugin.extension.view.ViewController;
import org.citydb.plugin.extension.view.components.BoundingBoxPanel;
import org.citydb.query.Query;
import org.citydb.query.builder.QueryBuildException;
import org.citydb.query.builder.config.ConfigQueryBuilder;
import org.citydb.registry.ObjectRegistry;
import org.citygml4j.model.module.citygml.CityGMLVersion;
import org.citygml4j.model.module.citygml.CoreModule;

import javax.swing.*;
import javax.xml.namespace.QName;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Comparator;
import java.util.concurrent.locks.ReentrantLock;

public class BoundingBoxOperation extends DatabaseOperationView {
	private final ReentrantLock mainLock = new ReentrantLock();
	private final Logger log = Logger.getInstance();
	private final DatabaseOperationsPanel parent;
	private final ViewController viewController;
	private final DatabaseConnectionPool dbConnectionPool;
	private final SchemaMapping schemaMapping;
	private final ADEExtensionManager adeManager;
	private final Config config;

	private JPanel component;
	private JLabel featureLabel;
	private JComboBox<FeatureType> featureComboBox;
	private BoundingBoxPanel bboxPanel;
	private JButton createAllButton;
	private JButton createMissingButton;
	private JButton calculateButton;

	private JCheckBox useFeatureVersionFilter;
	private TitledPanel featureVersionPanel;
	private FeatureVersionFilterView featureVersionFilter;

	private final FeatureType cityObject;
	private boolean isCreateBboxSupported;

	private enum BoundingBoxMode {
		FULL,
		PARTIAL
	}

	public BoundingBoxOperation(DatabaseOperationsPanel parent, Config config) {
		this.parent = parent;
		this.config = config;
		
		viewController = parent.getViewController();
		dbConnectionPool = DatabaseConnectionPool.getInstance();
		schemaMapping = ObjectRegistry.getInstance().getSchemaMapping();
		adeManager = ADEExtensionManager.getInstance();

		cityObject = schemaMapping.getFeatureType("_CityObject", CoreModule.v2_0_0.getNamespaceURI());
		init();
	}

	private void init() {
		component = new JPanel();
		component.setLayout(new GridBagLayout());

		featureLabel = new JLabel();
		bboxPanel = viewController.getComponentFactory().createBoundingBoxPanel();
		bboxPanel.setEditable(false);
		createAllButton = new JButton();
		createMissingButton = new JButton();
		calculateButton = new JButton();

		featureComboBox = new JComboBox<>();
		updateFeatureSelection();

		JPanel calcBboxPanel = new JPanel();
		calcBboxPanel.setLayout(new GridBagLayout());
		calcBboxPanel.add(featureLabel, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.BOTH, 0, 0, 0, 5));
		calcBboxPanel.add(featureComboBox, GuiUtil.setConstraints(1, 0, 1, 0, GridBagConstraints.BOTH, 0, 5, 0, 0));
		calcBboxPanel.add(bboxPanel, GuiUtil.setConstraints(0, 1, 2, 1, 1, 0, GridBagConstraints.BOTH, 5, 0, 0, 0));

		JPanel createBboxPanel = new JPanel();
		createBboxPanel.setLayout(new GridBagLayout());
		createBboxPanel.add(createMissingButton, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 0));
		createBboxPanel.add(createAllButton, GuiUtil.setConstraints(0, 1, 0, 1, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, 5, 0, 0, 0));

		useFeatureVersionFilter = new JCheckBox();
		featureVersionFilter = new FeatureVersionFilterView();
		featureVersionPanel = new TitledPanel()
				.withIcon(featureVersionFilter.getIcon())
				.withToggleButton(useFeatureVersionFilter)
				.showSeparator(false)
				.build(featureVersionFilter.getViewComponent());

		component.add(calcBboxPanel, GuiUtil.setConstraints(0, 1, 1, 0, GridBagConstraints.BOTH, 15, 0, 0, 0));
		component.add(createBboxPanel, GuiUtil.setConstraints(1, 1, 0, 0, GridBagConstraints.BOTH, 15, 20, 0, 0));
		component.add(featureVersionPanel, GuiUtil.setConstraints(0, 2, 2, 1, 0, 0, GridBagConstraints.BOTH, 10, 0, 0, 0));
		component.add(calculateButton, GuiUtil.setConstraints(0, 3, 2, 1, 0, 0, GridBagConstraints.NONE, 10, 0, 10, 0));

		featureComboBox.addItemListener(e -> {
			if (e.getStateChange() == ItemEvent.SELECTED)
				setEnabledBoundingBoxCalculation(true);
		});

		createAllButton.addActionListener(e -> new SwingWorker<Void, Void>() {
			protected Void doInBackground() {
				createBoundingBox(BoundingBoxMode.FULL);
				return null;
			}
		}.execute());

		createMissingButton.addActionListener(e -> new SwingWorker<Void, Void>() {
			protected Void doInBackground() {
				createBoundingBox(BoundingBoxMode.PARTIAL);
				return null;
			}
		}.execute());

		calculateButton.addActionListener(e -> new SwingWorker<Void, Void>() {
			protected Void doInBackground() {
				calcBoundingBox();
				return null;
			}
		}.execute());

		useFeatureVersionFilter.addActionListener(e -> setEnabledFeatureVersionFilter(true));
	}

	@Override
	public String getLocalizedTitle() {
		return Language.I18N.getString("db.label.operation.bbox");
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
	public DatabaseOperationType getType() {
		return DatabaseOperationType.BOUNDING_BOX;
	}

	@Override
	public void doTranslation() {
		featureLabel.setText(Language.I18N.getString("db.label.operation.bbox.feature"));
		createAllButton.setText(Language.I18N.getString("db.button.setbbox.all"));
		createMissingButton.setText(Language.I18N.getString("db.button.setbbox.missing"));
		calculateButton.setText(Language.I18N.getString("db.button.bbox"));
		featureVersionPanel.setTitle(featureVersionFilter.getLocalizedTitle());
	}

	@Override
	public void setEnabled(boolean enable) {
		featureLabel.setEnabled(enable);
		featureComboBox.setEnabled(enable);
		bboxPanel.setEnabled(enable);
		calculateButton.setEnabled(enable);
		setEnabledBoundingBoxCalculation(enable);
		featureVersionPanel.setEnabled(enable);
		setEnabledFeatureVersionFilter(enable);
	}

	public void setEnabledBoundingBoxCalculation(boolean enable) {
		if (enable) {
			FeatureType selected = (FeatureType)featureComboBox.getSelectedItem();
			if (selected != null) {		
				ADEExtension extension = adeManager.getExtensionByObjectClassId(selected.getObjectClassId());
				if (extension != null)
					enable = false;
			}
		}

		createAllButton.setEnabled(enable && isCreateBboxSupported);
		createMissingButton.setEnabled(enable && isCreateBboxSupported);
	}

	private void setEnabledFeatureVersionFilter(boolean enable) {
		featureVersionFilter.setEnabled(enable && useFeatureVersionFilter.isSelected());
	}

	@Override
	public void loadSettings() {
		FeatureType featureType = schemaMapping.getFeatureType(config.getDatabaseConfig().getOperation().getBoundingBoxTypeName());
		featureComboBox.setSelectedItem(featureType != null ? featureType : cityObject);
		bboxPanel.getSrsComboBox().setSelectedItem(config.getDatabaseConfig().getOperation().getBoundingBoxSrs());
		useFeatureVersionFilter.setSelected(config.getDatabaseConfig().getOperation().isUseFeatureVersionFilter());
		featureVersionFilter.loadSettings(config.getDatabaseConfig().getOperation().getFeatureVersionFilter());
	}

	@Override
	public void setSettings() {
		FeatureType featureType = (FeatureType)featureComboBox.getSelectedItem();
		QName typeName = new QName(featureType.getSchema().getNamespaces().get(0).getURI(), featureType.getPath());		
		config.getDatabaseConfig().getOperation().setBoundingBoxTypeName((typeName));
		config.getDatabaseConfig().getOperation().setBoundingBoxSrs(bboxPanel.getSrsComboBox().getSelectedItem());
		config.getDatabaseConfig().getOperation().setUseFeatureVersionFilter(useFeatureVersionFilter.isSelected());
		config.getDatabaseConfig().getOperation().setFeatureVersionFilter(featureVersionFilter.toSettings());
	}

	private void calcBoundingBox() {
		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			setSettings();

			viewController.clearConsole();
			viewController.setStatusText(Language.I18N.getString("main.status.database.bbox.label"));

			log.info("Calculating bounding box...");			

			final StatusDialog bboxDialog = new StatusDialog(viewController.getTopFrame(), 
					Language.I18N.getString("db.dialog.bbox.window"), 
					Language.I18N.getString("db.dialog.bbox.title"), 
					null,
					Language.I18N.getString("db.dialog.bbox.details"), 
					true);

			bboxDialog.getButton().addActionListener(e -> SwingUtilities.invokeLater(
					() -> dbConnectionPool.getActiveDatabaseAdapter().getUtil().interruptDatabaseOperation()));

			SwingUtilities.invokeLater(() -> {
				bboxDialog.setLocationRelativeTo(viewController.getTopFrame());
				bboxDialog.setVisible(true);
			});

			try {
				FeatureType featureType = (FeatureType)featureComboBox.getSelectedItem();
				Query query = buildQuery(featureType);
				query.setTargetSrs(bboxPanel.getSrsComboBox().getSelectedItem());

				BoundingBox bbox = dbConnectionPool.getActiveDatabaseAdapter().getUtil().calcBoundingBox(query, schemaMapping);

				if (bbox != null) {
					if (bbox.getLowerCorner().getX() != Double.MAX_VALUE &&
							bbox.getLowerCorner().getY() != Double.MAX_VALUE &&
							bbox.getUpperCorner().getX() != -Double.MAX_VALUE &&
							bbox.getUpperCorner().getY() != -Double.MAX_VALUE) {

						bboxPanel.setBoundingBox(bbox);
						BoundingBoxClipboardHandler.getInstance().putBoundingBox(bbox);
						log.info("Bounding box for " + featureType + " features successfully calculated.");
					} else {
						bboxPanel.clearBoundingBox();
						log.warn("The bounding box could not be calculated.");
						log.warn("Either the database does not contain valid " + featureType + " features or their ENVELOPE attribute is not set.");
					}
				} else {
					log.warn("Calculation of bounding box aborted.");
				}

				SwingUtilities.invokeLater(bboxDialog::dispose);

			} catch (SQLException | QueryBuildException e) {
				SwingUtilities.invokeLater(bboxDialog::dispose);

				bboxPanel.clearBoundingBox();

				String eMsg = e.getMessage().trim();
				String text = Language.I18N.getString("db.dialog.error.bbox");
				Object[] args = new Object[]{ eMsg };
				String result = MessageFormat.format(text, args);

				JOptionPane.showMessageDialog(
						viewController.getTopFrame(), 
						result, 
						Language.I18N.getString("common.dialog.error.db.title"),
						JOptionPane.ERROR_MESSAGE);

				log.error("Error: " + eMsg);
			} finally {		
				viewController.setStatusText(Language.I18N.getString("main.status.ready.label"));
			}

		} finally {
			lock.unlock();
		}
	}

	private void createBoundingBox(BoundingBoxMode mode) {
		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			setSettings();

			viewController.clearConsole();
			viewController.setStatusText(Language.I18N.getString("main.status.database.setbbox.label"));

			FeatureType featureType = (FeatureType)featureComboBox.getSelectedItem();			
			if (mode == BoundingBoxMode.FULL)
				log.info("Recreating all bounding boxes for " + featureType + " features...");
			else
				log.info("Creating missing bounding boxes for " + featureType + " features...");

			if (featureType == cityObject && !adeManager.getEnabledExtensions().isEmpty())
				log.warn("NOTE: This operation does not work on ADE features.");
			
			final StatusDialog bboxDialog = new StatusDialog(viewController.getTopFrame(),
					Language.I18N.getString("db.dialog.setbbox.window"), 
					Language.I18N.getString("db.dialog.setbbox.title"), 
					null,
					Language.I18N.getString("db.dialog.setbbox.details"), 
					true);

			bboxDialog.getButton().addActionListener(e -> SwingUtilities.invokeLater(
					() -> dbConnectionPool.getActiveDatabaseAdapter().getUtil().interruptDatabaseOperation()));

			SwingUtilities.invokeLater(() -> {
				bboxDialog.setLocationRelativeTo(viewController.getTopFrame());
				bboxDialog.setVisible(true);
			});

			try {
				Query query = buildQuery(featureType);
				query.setTargetSrs(bboxPanel.getSrsComboBox().getSelectedItem());

				BoundingBox bbox = dbConnectionPool.getActiveDatabaseAdapter().getUtil().createBoundingBoxes(
						query,
						schemaMapping, mode == BoundingBoxMode.PARTIAL,
						(hit) -> {
							try {
								if (hit > 0) {
									bboxDialog.handleEvent(new StatusDialogProgressBar(ProgressBarEventType.INIT, hit, this));
								} else {
									bboxDialog.handleEvent(new StatusDialogProgressBar(ProgressBarEventType.UPDATE, -1 * hit, this));
								}
							} catch (Exception e) {
								//
							}
						});

				if (bbox != null) {
					if (bbox.getLowerCorner().getX() != Double.MAX_VALUE &&
							bbox.getLowerCorner().getY() != Double.MAX_VALUE &&
							bbox.getUpperCorner().getX() != -Double.MAX_VALUE &&
							bbox.getUpperCorner().getY() != -Double.MAX_VALUE) {

						bboxPanel.setBoundingBox(bbox);
						BoundingBoxClipboardHandler.getInstance().putBoundingBox(bbox);
						log.info("Bounding box for " + featureType + " features successfully created.");
					} else {
						bboxPanel.clearBoundingBox();
						log.warn("The bounding boxes could not be created.");
						log.warn("Check whether the database contains valid " + featureType + " features" + (mode == BoundingBoxMode.PARTIAL ? " with missing bounding boxes." : "."));
					}
				} else {
					log.warn("Creation of bounding boxes aborted.");
				}

				SwingUtilities.invokeLater(bboxDialog::dispose);

			} catch (SQLException | QueryBuildException e) {
				SwingUtilities.invokeLater(bboxDialog::dispose);

				bboxPanel.clearBoundingBox();

				String eMsg = e.getMessage().trim();
				String text = Language.I18N.getString("db.dialog.error.setbbox");
				Object[] args = new Object[]{ eMsg };
				String result = MessageFormat.format(text, args);

				JOptionPane.showMessageDialog(
						viewController.getTopFrame(), 
						result, 
						Language.I18N.getString("common.dialog.error.db.title"),
						JOptionPane.ERROR_MESSAGE);

				log.error("Error: " + eMsg);
			} finally {		
				viewController.setStatusText(Language.I18N.getString("main.status.ready.label"));
			}

		} finally {
			lock.unlock();
		}
	}

	private void updateFeatureSelection() {
		featureComboBox.removeAllItems();
		
		featureComboBox.addItem(cityObject);
		schemaMapping.listTopLevelFeatureTypes(true).stream()
		.sorted(Comparator.comparing(AbstractPathElement::getPath))
		.forEach(featureType -> {
			ADEExtension extension = adeManager.getExtensionByObjectClassId(featureType.getObjectClassId());
			if (extension == null || extension.isEnabled())
				featureComboBox.addItem(featureType);			
		});
	}

	private Query buildQuery(FeatureType featureType) throws QueryBuildException {
		SimpleQuery simpleQuery = new SimpleQuery();

		// set feature type filter
		simpleQuery.setUseTypeNames(true);
		FeatureTypeFilter featureTypeFilter = new FeatureTypeFilter();
		QName typeName = new QName(featureType.getSchema().getNamespace(CityGMLVersion.DEFAULT).getURI(), featureType.getPath());
		featureTypeFilter.addTypeName(typeName);
		simpleQuery.setFeatureTypeFilter(featureTypeFilter);

		// set feature version filter
		simpleQuery.setUseFeatureVersionFilter(config.getDatabaseConfig().getOperation().isUseFeatureVersionFilter());
		simpleQuery.setFeatureVersionFilter(config.getDatabaseConfig().getOperation().getFeatureVersionFilter());

		// build query according to the above two filters
		ConfigQueryBuilder builder = new ConfigQueryBuilder(schemaMapping, dbConnectionPool.getActiveDatabaseAdapter());
		return builder.buildQuery(simpleQuery, ObjectRegistry.getInstance().getConfig().getNamespaceFilter());
	}

	@Override
	public void handleDatabaseConnectionStateEvent(DatabaseConnectionStateEvent event) {
		if (event.wasConnected())
			bboxPanel.clearBoundingBox();
		else {
			isCreateBboxSupported = dbConnectionPool.getActiveDatabaseAdapter().getConnectionMetaData().getCityDBVersion().compareTo(3, 1, 0) >= 0;
			SwingUtilities.invokeLater(() -> {
				FeatureType selected = (FeatureType)featureComboBox.getSelectedItem();
				updateFeatureSelection();
				featureComboBox.setSelectedItem(selected);
			});
		}
	}

}

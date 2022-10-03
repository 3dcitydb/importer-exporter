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
package org.citydb.gui.operation.database.operations;

import org.citydb.config.Config;
import org.citydb.config.geometry.BoundingBox;
import org.citydb.config.geometry.Position;
import org.citydb.config.gui.database.DatabaseGuiConfig;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.database.DatabaseOperation;
import org.citydb.config.project.database.DatabaseOperationType;
import org.citydb.config.project.database.DatabaseSrs;
import org.citydb.config.project.exporter.SimpleQuery;
import org.citydb.config.project.query.filter.type.FeatureTypeFilter;
import org.citydb.config.project.query.simple.SimpleFeatureVersionFilter;
import org.citydb.config.project.query.simple.SimpleFeatureVersionFilterMode;
import org.citydb.core.ade.ADEExtension;
import org.citydb.core.ade.ADEExtensionManager;
import org.citydb.core.database.connection.DatabaseConnectionPool;
import org.citydb.core.database.schema.mapping.AbstractPathElement;
import org.citydb.core.database.schema.mapping.FeatureType;
import org.citydb.core.database.schema.mapping.MappingConstants;
import org.citydb.core.database.schema.mapping.SchemaMapping;
import org.citydb.core.query.Query;
import org.citydb.core.query.builder.QueryBuildException;
import org.citydb.core.query.builder.config.ConfigQueryBuilder;
import org.citydb.core.query.builder.sql.BuildProperties;
import org.citydb.core.query.builder.sql.SQLQueryBuilder;
import org.citydb.core.registry.ObjectRegistry;
import org.citydb.gui.components.TitledPanel;
import org.citydb.gui.components.bbox.BoundingBoxClipboardHandler;
import org.citydb.gui.components.bbox.BoundingBoxPanel;
import org.citydb.gui.components.dialog.StatusDialog;
import org.citydb.gui.operation.common.filter.FeatureVersionFilterView;
import org.citydb.gui.plugin.view.ViewController;
import org.citydb.gui.util.GuiUtil;
import org.citydb.sqlbuilder.schema.Column;
import org.citydb.sqlbuilder.schema.Table;
import org.citydb.sqlbuilder.select.ProjectionToken;
import org.citydb.sqlbuilder.select.Select;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonFactory;
import org.citydb.sqlbuilder.select.projection.Function;
import org.citydb.sqlbuilder.select.projection.WildCardColumn;
import org.citydb.util.event.EventDispatcher;
import org.citydb.util.event.global.DatabaseConnectionStateEvent;
import org.citydb.util.event.global.ProgressBarEventType;
import org.citydb.util.event.global.StatusDialogProgressBar;
import org.citydb.util.log.Logger;
import org.citygml4j.model.module.citygml.CoreModule;

import javax.swing.*;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.namespace.QName;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Comparator;
import java.util.Locale;
import java.util.concurrent.locks.ReentrantLock;

public class BoundingBoxOperation extends DatabaseOperationView {
	private final ReentrantLock mainLock = new ReentrantLock();
	private final Logger log = Logger.getInstance();
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
	private volatile boolean shouldRun;

	private enum BoundingBoxMode {
		FULL,
		PARTIAL
	}

	public BoundingBoxOperation(DatabaseOperationsPanel parent, Config config) {
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
		bboxPanel = new BoundingBoxPanel(viewController);
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
				.withCollapseButton()
				.withToggleButton(useFeatureVersionFilter)
				.build(featureVersionFilter.getViewComponent());

		component.add(calcBboxPanel, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.BOTH, 15, 0, 0, 0));
		component.add(createBboxPanel, GuiUtil.setConstraints(1, 0, 0, 0, GridBagConstraints.BOTH, 15, 20, 0, 0));
		component.add(featureVersionPanel, GuiUtil.setConstraints(0, 1, 2, 1, 0, 0, GridBagConstraints.BOTH, 15, 0, 0, 0));
		component.add(calculateButton, GuiUtil.setConstraints(0, 2, 2, 1, 0, 0, GridBagConstraints.NONE, 5, 0, 10, 0));

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
	public void switchLocale(Locale locale) {
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
			FeatureType featureType = (FeatureType) featureComboBox.getSelectedItem();
			if (featureType != null) {
				ADEExtension extension = adeManager.getExtensionByObjectClassId(featureType.getObjectClassId());
				if (extension != null) {
					enable = false;
				}
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
		DatabaseOperation databaseOperation = config.getDatabaseConfig().getOperation();
		FeatureType featureType = schemaMapping.getFeatureType(databaseOperation.getBoundingBoxTypeName());
		featureComboBox.setSelectedItem(featureType != null ? featureType : cityObject);
		bboxPanel.getSrsComboBox().setSelectedItem(databaseOperation.getBoundingBoxSrs());
		useFeatureVersionFilter.setSelected(databaseOperation.isUseFeatureVersionFilter());
		featureVersionFilter.loadSettings(databaseOperation.getFeatureVersionFilter());

		DatabaseGuiConfig guiConfig = config.getGuiConfig().getDatabaseGuiConfig();
		featureVersionPanel.setCollapsed(guiConfig.isCollapseFeatureVersionFilter());
	}

	@Override
	public void setSettings() {
		DatabaseOperation databaseOperation = config.getDatabaseConfig().getOperation();
		FeatureType featureType = featureComboBox.getSelectedItem() != null ?
				(FeatureType) featureComboBox.getSelectedItem() :
				cityObject;

		QName typeName = new QName(featureType.getSchema().getNamespaces().get(0).getURI(), featureType.getPath());
		databaseOperation.setBoundingBoxTypeName((typeName));
		databaseOperation.setBoundingBoxSrs(bboxPanel.getSrsComboBox().getSelectedItem());
		databaseOperation.setUseFeatureVersionFilter(useFeatureVersionFilter.isSelected());
		databaseOperation.setFeatureVersionFilter(featureVersionFilter.toSettings());

		DatabaseGuiConfig guiConfig = config.getGuiConfig().getDatabaseGuiConfig();
		guiConfig.setCollapseFeatureVersionFilter(featureVersionPanel.isCollapsed());
	}

	private boolean checkSettings() {
		DatabaseOperation databaseOperation = config.getDatabaseConfig().getOperation();

		// feature version filter
		if (databaseOperation.isUseFeatureVersionFilter()) {
			SimpleFeatureVersionFilter featureVersionFilter = databaseOperation.getFeatureVersionFilter();

			if (featureVersionFilter.getMode() != SimpleFeatureVersionFilterMode.LATEST) {
				if (!featureVersionFilter.isSetStartDate()) {
					viewController.errorMessage(Language.I18N.getString("common.dialog.error.incorrectFilter"),
							Language.I18N.getString("export.dialog.error.featureVersion.startDate"));
					return false;
				}

				if (featureVersionFilter.getMode() == SimpleFeatureVersionFilterMode.BETWEEN) {
					if (!featureVersionFilter.isSetEndDate()) {
						viewController.errorMessage(Language.I18N.getString("common.dialog.error.incorrectFilter"),
								Language.I18N.getString("export.dialog.error.featureVersion.endDate"));
						return false;
					} else if (featureVersionFilter.getStartDate().compare(featureVersionFilter.getEndDate()) != DatatypeConstants.LESSER) {
						viewController.errorMessage(Language.I18N.getString("common.dialog.error.incorrectFilter"),
								Language.I18N.getString("export.dialog.error.featureVersion.range"));
						return false;
					}
				}
			}
		}

		return true;
	}

	private void calcBoundingBox() {
		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			viewController.clearConsole();
			setSettings();

			if (!checkSettings()) {
				return;
			}

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
				FeatureType featureType = featureComboBox.getSelectedItem() != null ?
						(FeatureType) featureComboBox.getSelectedItem() :
						cityObject;

				Query query = buildQuery(featureType);
				BoundingBox bbox = dbConnectionPool.getActiveDatabaseAdapter().getUtil().calcBoundingBox(query, schemaMapping);

				if (bbox != null) {
					if (bbox.getLowerCorner().getX() != Double.MAX_VALUE
							&& bbox.getLowerCorner().getY() != Double.MAX_VALUE
							&& bbox.getUpperCorner().getX() != -Double.MAX_VALUE
							&& bbox.getUpperCorner().getY() != -Double.MAX_VALUE) {
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
				viewController.errorMessage(Language.I18N.getString("common.dialog.error.db.title"),
						MessageFormat.format(Language.I18N.getString("db.dialog.error.bbox"), e.getMessage().trim()));

				log.error("Failed to calculate bounding box.", e);
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
			viewController.clearConsole();
			setSettings();

			if (!checkSettings()) {
				return;
			}

			viewController.setStatusText(Language.I18N.getString("main.status.database.setbbox.label"));

			FeatureType featureType = featureComboBox.getSelectedItem() != null ?
					(FeatureType) featureComboBox.getSelectedItem() :
					cityObject;

			if (mode == BoundingBoxMode.FULL) {
				log.info("Recreating all bounding boxes for " + featureType + " features...");
			} else {
				log.info("Creating missing bounding boxes for " + featureType + " features...");
			}

			if (featureType == cityObject && !adeManager.getEnabledExtensions().isEmpty()) {
				log.warn("NOTE: This operation does not work on ADE features.");
			}

			EventDispatcher eventDispatcher = ObjectRegistry.getInstance().getEventDispatcher();
			final StatusDialog bboxDialog = new StatusDialog(viewController.getTopFrame(),
					Language.I18N.getString("db.dialog.setbbox.window"), 
					Language.I18N.getString("db.dialog.setbbox.title"), 
					null,
					Language.I18N.getString("db.dialog.setbbox.details"), 
					true,
					eventDispatcher);

			shouldRun = true;
			bboxDialog.getButton().addActionListener(e -> SwingUtilities.invokeLater(() -> shouldRun = false));

			SwingUtilities.invokeLater(() -> {
				bboxDialog.setLocationRelativeTo(viewController.getTopFrame());
				bboxDialog.setVisible(true);
			});

			String schema = dbConnectionPool.getActiveDatabaseAdapter().getConnectionDetails().getSchema();
			try (Connection connection = dbConnectionPool.getConnection()) {
				connection.setAutoCommit(false);

				Query query = buildQuery(featureType);
				SQLQueryBuilder builder = new SQLQueryBuilder(
						schemaMapping,
						dbConnectionPool.getActiveDatabaseAdapter(),
						BuildProperties.defaults().addProjectionColumn(MappingConstants.ID));

				Select select = builder.buildQuery(query);
				if (mode == BoundingBoxMode.PARTIAL) {
					ProjectionToken token = select.getProjection().stream()
							.filter(v -> v instanceof Column && ((Column) v).getName().equals(MappingConstants.ID))
							.findFirst()
							.orElseThrow(() -> new QueryBuildException("Failed to build query due to unexpected SQL projection clause."));

					select.addSelection(ComparisonFactory.isNull(((Column) token).getTable().getColumn(MappingConstants.ENVELOPE)));
				}

				BoundingBox bbox = new BoundingBox(
						new Position(Double.MAX_VALUE, Double.MAX_VALUE),
						new Position(-Double.MAX_VALUE, -Double.MAX_VALUE),
						query.getTargetSrs()
				);

				long hits = getNumberMatched(select, connection);
				if (hits > 0) {
					eventDispatcher.triggerEvent(new StatusDialogProgressBar(ProgressBarEventType.INIT, (int) hits));

					try (PreparedStatement stmt = dbConnectionPool.getActiveDatabaseAdapter().getSQLAdapter().prepareStatement(select, connection);
						 ResultSet rs = stmt.executeQuery()) {
						while (shouldRun && rs.next()) {
							long objectId = rs.getLong(1);
							BoundingBox extent = dbConnectionPool.getActiveDatabaseAdapter().getUtil()
									.createBoundingBox(schema, objectId, mode == BoundingBoxMode.PARTIAL, connection);

							bbox.update(extent);
							eventDispatcher.triggerEvent(new StatusDialogProgressBar(ProgressBarEventType.UPDATE, 1));
						}
					} catch (SQLException e) {
						connection.rollback();
						throw e;
					}
				}

				if (shouldRun) {
					connection.commit();

					if (bbox.getLowerCorner().getX() != Double.MAX_VALUE
							&& bbox.getLowerCorner().getY() != Double.MAX_VALUE
							&& bbox.getUpperCorner().getX() != -Double.MAX_VALUE
							&& bbox.getUpperCorner().getY() != -Double.MAX_VALUE) {
						DatabaseSrs sourceSrs = dbConnectionPool.getActiveDatabaseAdapter().getConnectionMetaData().getReferenceSystem();
						DatabaseSrs targetSrs = query.getTargetSrs();

						if (targetSrs != null
								&& targetSrs.isSupported()
								&& targetSrs.getSrid() != sourceSrs.getSrid()) {
							try {
								bbox = dbConnectionPool.getActiveDatabaseAdapter().getUtil().transform2D(bbox, sourceSrs, targetSrs);
							} catch (SQLException e) {
								//
							}
						}

						bboxPanel.setBoundingBox(bbox);
						BoundingBoxClipboardHandler.getInstance().putBoundingBox(bbox);
						log.info("Bounding box for " + featureType + " features successfully created.");
					} else {
						bboxPanel.clearBoundingBox();
						log.warn("The bounding boxes could not be created.");
						log.warn("Check whether the database contains valid " + featureType + " features" +
								(mode == BoundingBoxMode.PARTIAL ? " with missing bounding boxes." : "."));
					}
				} else {
					log.warn("Creation of bounding boxes aborted.");
					connection.rollback();
				}

				SwingUtilities.invokeLater(bboxDialog::dispose);
			} catch (SQLException | QueryBuildException e) {
				SwingUtilities.invokeLater(bboxDialog::dispose);
				bboxPanel.clearBoundingBox();
				viewController.errorMessage(Language.I18N.getString("common.dialog.error.db.title"),
						MessageFormat.format(Language.I18N.getString("db.dialog.error.setbbox"), e.getMessage().trim()));

				log.error("Failed to create bounding boxes.", e);
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
		.sorted(Comparator.comparing(AbstractPathElement::toString))
		.forEach(featureType -> {
			ADEExtension extension = adeManager.getExtensionByObjectClassId(featureType.getObjectClassId());
			if (extension == null || extension.isEnabled())
				featureComboBox.addItem(featureType);			
		});
	}

	private Query buildQuery(FeatureType featureType) throws QueryBuildException {
		SimpleQuery simpleQuery = new SimpleQuery();

		// set target reference system
		simpleQuery.setTargetSrs(bboxPanel.getSrsComboBox().getSelectedItem());

		// set feature type filter
		simpleQuery.setUseTypeNames(true);
		FeatureTypeFilter featureTypeFilter = new FeatureTypeFilter();

		QName typeName = new QName(featureType.getSchema().getNamespaces().get(0).getURI(), featureType.getPath());
		featureTypeFilter.addTypeName(typeName);
		simpleQuery.setFeatureTypeFilter(featureTypeFilter);

		// set feature version filter
		simpleQuery.setUseFeatureVersionFilter(config.getDatabaseConfig().getOperation().isUseFeatureVersionFilter());
		simpleQuery.setFeatureVersionFilter(config.getDatabaseConfig().getOperation().getFeatureVersionFilter());

		// build query according to the above two filters
		ConfigQueryBuilder builder = new ConfigQueryBuilder(schemaMapping, dbConnectionPool.getActiveDatabaseAdapter());
		return builder.buildQuery(simpleQuery, config.getNamespaceFilter());
	}

	private long getNumberMatched(Select select, Connection conn) throws SQLException {
		Select hitsQuery = new Select(select)
				.unsetOrderBy()
				.removeProjectionIf(t -> !(t instanceof Column) || !((Column) t).getName().equals(MappingConstants.ID));

		hitsQuery = new Select().addProjection(new Function("count", new WildCardColumn(new Table(hitsQuery), false)));
		try (PreparedStatement stmt = dbConnectionPool.getActiveDatabaseAdapter().getSQLAdapter().prepareStatement(hitsQuery, conn);
			 ResultSet rs = stmt.executeQuery()) {
			return rs.next() ? rs.getLong(1) : 0;
		}
	}

	@Override
	public void handleDatabaseConnectionStateEvent(DatabaseConnectionStateEvent event) {
		if (event.wasConnected()) {
			bboxPanel.clearBoundingBox();
		} else {
			isCreateBboxSupported = dbConnectionPool.getActiveDatabaseAdapter().getConnectionMetaData().getCityDBVersion().compareTo(3, 1, 0) >= 0;
			SwingUtilities.invokeLater(() -> {
				FeatureType selected = (FeatureType)featureComboBox.getSelectedItem();
				updateFeatureSelection();
				featureComboBox.setSelectedItem(selected);
			});
		}
	}
}

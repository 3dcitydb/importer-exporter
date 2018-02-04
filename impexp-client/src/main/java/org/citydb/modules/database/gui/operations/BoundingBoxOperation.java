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

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.xml.namespace.QName;

import org.citydb.ade.ADEExtension;
import org.citydb.ade.ADEExtensionManager;
import org.citydb.config.Config;
import org.citydb.config.geometry.BoundingBox;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.database.DBOperationType;
import org.citydb.config.project.database.DatabaseSrs;
import org.citydb.config.project.database.Workspace;
import org.citydb.database.connection.DatabaseConnectionPool;
import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.database.schema.mapping.SchemaMapping;
import org.citydb.event.global.DatabaseConnectionStateEvent;
import org.citydb.gui.components.bbox.BoundingBoxClipboardHandler;
import org.citydb.gui.components.dialog.StatusDialog;
import org.citydb.gui.util.GuiUtil;
import org.citydb.log.Logger;
import org.citydb.plugin.extension.view.ViewController;
import org.citydb.plugin.extension.view.components.BoundingBoxPanel;
import org.citydb.registry.ObjectRegistry;
import org.citygml4j.model.module.citygml.CoreModule;

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

	private FeatureType cityObject;
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
		schemaMapping = (SchemaMapping)ObjectRegistry.getInstance().lookup(SchemaMapping.class.getName());
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

		featureComboBox = new JComboBox<FeatureType>();
		updateFeatureSelection();

		JPanel featureBox = new JPanel();
		featureBox.setLayout(new GridBagLayout());
		GridBagConstraints c = GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,10,5,0,5);
		c.gridwidth = 2;
		component.add(featureBox, c);
		featureBox.add(featureLabel, GuiUtil.setConstraints(0,0,0.0,0.0,GridBagConstraints.BOTH,0,0,0,5));
		featureBox.add(featureComboBox, GuiUtil.setConstraints(1,0,1.0,0.0,GridBagConstraints.BOTH,0,5,0,0));

		JPanel calcBboxPanel = new JPanel();
		calcBboxPanel.setLayout(new GridBagLayout());
		component.add(calcBboxPanel, GuiUtil.setConstraints(0,1,1.0,0.0,GridBagConstraints.BOTH,10,5,0,5));
		calcBboxPanel.add(bboxPanel, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,5,0,5,5));

		JPanel createBboxPanel = new JPanel();
		createBboxPanel.setLayout(new GridBagLayout());
		component.add(createBboxPanel, GuiUtil.setConstraints(1,1,0.0,1.0,GridBagConstraints.BOTH,10,0,0,5));
		createBboxPanel.add(createMissingButton, GuiUtil.setConstraints(0,0,0.0,0.0,GridBagConstraints.HORIZONTAL,5,0,0,0));
		createBboxPanel.add(createAllButton, GuiUtil.setConstraints(0,1,0.0,1.0,GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,5,0,0,0));

		c = GuiUtil.setConstraints(0,2,0.0,0.0,GridBagConstraints.NONE,10,0,5,5);
		c.gridwidth = 2;
		component.add(calculateButton, c);

		featureComboBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED)
					setEnabledBoundingBoxCalculation(true);
			}
		});

		createAllButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Thread thread = new Thread() {
					public void run() {
						createBoundingBox(BoundingBoxMode.FULL);
					}
				};
				thread.setDaemon(true);
				thread.start();
			}
		});

		createMissingButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Thread thread = new Thread() {
					public void run() {
						createBoundingBox(BoundingBoxMode.PARTIAL);
					}
				};
				thread.setDaemon(true);
				thread.start();
			}
		});

		calculateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {			
				Thread thread = new Thread() {
					public void run() {
						calcBoundingBox();
					}
				};
				thread.setDaemon(true);
				thread.start();
			}
		});
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
	public DBOperationType getType() {
		return DBOperationType.BOUNDING_BOX;
	}

	@Override
	public void doTranslation() {
		featureLabel.setText(Language.I18N.getString("db.label.operation.bbox.feature"));
		createAllButton.setText(Language.I18N.getString("db.button.setbbox.all"));
		createMissingButton.setText(Language.I18N.getString("db.button.setbbox.missing"));
		calculateButton.setText(Language.I18N.getString("db.button.bbox"));
	}

	@Override
	public void setEnabled(boolean enable) {
		featureLabel.setEnabled(enable);
		featureComboBox.setEnabled(enable);
		bboxPanel.setEnabled(enable);
		calculateButton.setEnabled(enable);
		setEnabledBoundingBoxCalculation(enable);
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

	@Override
	public void loadSettings() {
		FeatureType featureType = schemaMapping.getFeatureType(config.getProject().getDatabase().getOperation().getBoundingBoxTypeName()); 
		featureComboBox.setSelectedItem(featureType != null ? featureType : cityObject);
		bboxPanel.getSrsComboBox().setSelectedItem(config.getProject().getDatabase().getOperation().getBoundingBoxSRS());
	}

	@Override
	public void setSettings() {
		FeatureType featureType = (FeatureType)featureComboBox.getSelectedItem();
		QName typeName = new QName(featureType.getSchema().getNamespaces().get(0).getURI(), featureType.getPath());		
		config.getProject().getDatabase().getOperation().setBoundingBoxTypeName((typeName));
		config.getProject().getDatabase().getOperation().setBoundingBoxSRS(bboxPanel.getSrsComboBox().getSelectedItem());
	}

	private void calcBoundingBox() {
		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			Workspace workspace = parent.getWorkspace();
			if (workspace == null)
				return;

			viewController.clearConsole();
			viewController.setStatusText(Language.I18N.getString("main.status.database.bbox.label"));

			log.info("Calculating bounding box...");			
			if (dbConnectionPool.getActiveDatabaseAdapter().hasVersioningSupport() && !parent.existsWorkspace())
				return;

			final StatusDialog bboxDialog = new StatusDialog(viewController.getTopFrame(), 
					Language.I18N.getString("db.dialog.bbox.window"), 
					Language.I18N.getString("db.dialog.bbox.title"), 
					null,
					Language.I18N.getString("db.dialog.bbox.details"), 
					true);

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					bboxDialog.setLocationRelativeTo(viewController.getTopFrame());
					bboxDialog.setVisible(true);
				}
			});

			bboxDialog.getButton().addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							dbConnectionPool.getActiveDatabaseAdapter().getUtil().interruptDatabaseOperation();
						}
					});
				}
			});

			try {
				String schema = dbConnectionPool.getActiveDatabaseAdapter().getConnectionDetails().getSchema();
				FeatureType featureType = (FeatureType)featureComboBox.getSelectedItem();
				BoundingBox bbox = dbConnectionPool.getActiveDatabaseAdapter().getUtil().calcBoundingBox(workspace, schema, getObjectClassIds(featureType, true));

				if (bbox != null) {
					if (bbox.getLowerCorner().getX() != Double.MAX_VALUE && 
							bbox.getLowerCorner().getY() != Double.MAX_VALUE &&
							bbox.getUpperCorner().getX() != -Double.MAX_VALUE && 
							bbox.getUpperCorner().getY() != -Double.MAX_VALUE) {

						DatabaseSrs dbSrs = dbConnectionPool.getActiveDatabaseAdapter().getConnectionMetaData().getReferenceSystem();
						DatabaseSrs targetSrs = bboxPanel.getSrsComboBox().getSelectedItem();

						if (targetSrs.isSupported() && targetSrs.getSrid() != dbSrs.getSrid()) {
							try {
								bbox = dbConnectionPool.getActiveDatabaseAdapter().getUtil().transformBoundingBox(bbox, dbSrs, targetSrs);
							} catch (SQLException e) {
								//
							}					
						}

						bboxPanel.setBoundingBox(bbox);	
						bbox.setSrs(targetSrs);
						BoundingBoxClipboardHandler.getInstance(config).putBoundingBox(bbox);
						log.info("Bounding box for " + featureType + " features successfully calculated.");							
					} else {
						bboxPanel.clearBoundingBox();
						log.warn("The bounding box could not be calculated.");
						log.warn("Either the database does not contain " + featureType + " features or their ENVELOPE attribute is not set.");
					}

				} else
					log.warn("Calculation of bounding box aborted.");

				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						bboxDialog.dispose();
					}
				});

			} catch (SQLException sqlEx) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						bboxDialog.dispose();
					}
				});

				bboxPanel.clearBoundingBox();

				String sqlExMsg = sqlEx.getMessage().trim();
				String text = Language.I18N.getString("db.dialog.error.bbox");
				Object[] args = new Object[]{ sqlExMsg };
				String result = MessageFormat.format(text, args);

				JOptionPane.showMessageDialog(
						viewController.getTopFrame(), 
						result, 
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

	private void createBoundingBox(BoundingBoxMode mode) {
		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			Workspace workspace = parent.getWorkspace();
			if (workspace == null)
				return;

			viewController.clearConsole();
			viewController.setStatusText(Language.I18N.getString("main.status.database.setbbox.label"));

			FeatureType featureType = (FeatureType)featureComboBox.getSelectedItem();			
			if (mode == BoundingBoxMode.FULL)
				log.info("Recreating all bounding boxes for " + featureType + " features...");
			else
				log.info("Creating missing bounding boxes for " + featureType + " features...");

			if (featureType == cityObject && !adeManager.getEnabledExtensions().isEmpty())
				log.warn("NOTE: This operation does not work on ADE features.");
			
			if (dbConnectionPool.getActiveDatabaseAdapter().hasVersioningSupport() && !parent.existsWorkspace())
				return;

			final StatusDialog bboxDialog = new StatusDialog(viewController.getTopFrame(), 
					Language.I18N.getString("db.dialog.setbbox.window"), 
					Language.I18N.getString("db.dialog.setbbox.title"), 
					null,
					Language.I18N.getString("db.dialog.setbbox.details"), 
					true);

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					bboxDialog.setLocationRelativeTo(viewController.getTopFrame());
					bboxDialog.setVisible(true);
				}
			});

			bboxDialog.getButton().addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							dbConnectionPool.getActiveDatabaseAdapter().getUtil().interruptDatabaseOperation();
						}
					});
				}
			});

			try {
				String schema = dbConnectionPool.getActiveDatabaseAdapter().getConnectionDetails().getSchema();
				List<Integer> objectClassIds = getObjectClassIds(featureType, false);
				BoundingBox bbox = dbConnectionPool.getActiveDatabaseAdapter().getUtil().createBoundingBoxes(workspace, schema, objectClassIds, mode == BoundingBoxMode.PARTIAL ? true : false);

				if (bbox != null) {
					if (bbox.getLowerCorner().getX() != Double.MAX_VALUE && 
							bbox.getLowerCorner().getY() != Double.MAX_VALUE &&
							bbox.getUpperCorner().getX() != -Double.MAX_VALUE && 
							bbox.getUpperCorner().getY() != -Double.MAX_VALUE) {

						DatabaseSrs dbSrs = dbConnectionPool.getActiveDatabaseAdapter().getConnectionMetaData().getReferenceSystem();
						DatabaseSrs targetSrs = bboxPanel.getSrsComboBox().getSelectedItem();

						if (targetSrs.isSupported() && targetSrs.getSrid() != dbSrs.getSrid()) {
							try {
								bbox = dbConnectionPool.getActiveDatabaseAdapter().getUtil().transformBoundingBox(bbox, dbSrs, targetSrs);
							} catch (SQLException e) {
								//
							}					
						}

						bboxPanel.setBoundingBox(bbox);	
						bbox.setSrs(targetSrs);
						BoundingBoxClipboardHandler.getInstance(config).putBoundingBox(bbox);
						log.info("Bounding box for " + featureType + " features successfully created.");							
					} else {
						bboxPanel.clearBoundingBox();
						log.warn("The bounding boxes could not be created.");
						log.warn("Check whether the database contains " + featureType + " features" + (mode == BoundingBoxMode.PARTIAL ? " with missing bounding boxes." : "."));
					}

				} else
					log.warn("Creation of bounding boxes aborted.");

				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						bboxDialog.dispose();
					}
				});

			} catch (SQLException sqlEx) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						bboxDialog.dispose();
					}
				});

				bboxPanel.clearBoundingBox();

				String sqlExMsg = sqlEx.getMessage().trim();
				String text = Language.I18N.getString("db.dialog.error.setbbox");
				Object[] args = new Object[]{ sqlExMsg };
				String result = MessageFormat.format(text, args);

				JOptionPane.showMessageDialog(
						viewController.getTopFrame(), 
						result, 
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

	private void updateFeatureSelection() {
		featureComboBox.removeAllItems();
		
		featureComboBox.addItem(cityObject);
		schemaMapping.listTopLevelFeatureTypes(true).stream()
		.sorted((a, b) -> a.getPath().compareTo(b.getPath()))
		.forEach(featureType -> {
			ADEExtension extension = adeManager.getExtensionByObjectClassId(featureType.getObjectClassId());
			if (extension == null || extension.isEnabled())
				featureComboBox.addItem(featureType);			
		});
	}
	
	private List<Integer> getObjectClassIds(FeatureType featureType, boolean fanOutCityObject) {
		List<Integer> objectClassIds = new ArrayList<>();
		if (featureType == cityObject) {
			if (fanOutCityObject) {
				for (FeatureType topLevelType : schemaMapping.listTopLevelFeatureTypes(true))
					objectClassIds.add(topLevelType.getObjectClassId());
			} else
				objectClassIds.add(0);
		} else 
			objectClassIds.add(featureType.getObjectClassId());

		return objectClassIds;
	}

	@Override
	public void handleDatabaseConnectionStateEvent(DatabaseConnectionStateEvent event) {
		if (event.wasConnected())
			bboxPanel.clearBoundingBox();
		else {
			isCreateBboxSupported = dbConnectionPool.getActiveDatabaseAdapter().getConnectionMetaData().getCityDBVersion().compareTo(3, 1, 0) >= 0;
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					FeatureType selected = (FeatureType)featureComboBox.getSelectedItem();
					updateFeatureSelection();
					featureComboBox.setSelectedItem(selected);
				}
			});
		}
	}

}

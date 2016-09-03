/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2016
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
package org.citydb.gui.components.mapviewer.validation;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;

import org.citydb.api.controller.DatabaseController;
import org.citydb.api.database.DatabaseConfigurationException;
import org.citydb.api.database.DatabaseSrs;
import org.citydb.api.database.DatabaseVersionException;
import org.citydb.api.geometry.BoundingBox;
import org.citydb.api.registry.ObjectRegistry;
import org.citydb.config.Config;
import org.citydb.config.language.Language;
import org.citydb.config.project.database.Database;
import org.citydb.config.project.database.Database.PredefinedSrsName;
import org.citydb.gui.components.mapviewer.MapWindow;
import org.citydb.gui.factory.SrsComboBoxFactory;
import org.citydb.gui.factory.SrsComboBoxFactory.SrsComboBox;
import org.citydb.log.Logger;
import org.citydb.util.gui.GuiUtil;

public class BoundingBoxValidator {
	private final Logger LOG = Logger.getInstance();
	private final MapWindow map;
	private final Config config;
	private final DatabaseController dbController;

	public enum ValidationResult {
		CANCEL,
		SKIP,
		VALID,
		OUT_OF_RANGE,
		NO_AREA,
		INVISIBLE
	}

	private enum ValidatorDialogAction {
		TRANSFORM,
		SKIP,
		CLOSE,
		OK
	};

	public BoundingBoxValidator(MapWindow map, Config config) {
		this.map = map;
		this.config = config;

		dbController = ObjectRegistry.getInstance().getDatabaseController();
	}

	public ValidationResult validate(final BoundingBox bbox) {
		boolean isDBConnected = dbController.isConnected();

		// invalid bbox
		if (bbox.getLowerCorner().getX() == null ||
				bbox.getLowerCorner().getY() == null ||
				bbox.getUpperCorner().getX() == null ||
				bbox.getUpperCorner().getY() == null)
			return ValidationResult.SKIP;

		// unknown srs
		else if (!bbox.isSetSrs()) {
			ValidatorDialog validator = new ValidatorDialog(bbox, Language.I18N.getString("map.dialog.title.error"), config);
			validator.addErrorMessage(Language.I18N.getString("map.dialog.label.error.noSRS"));
			validator.addBoundingBox();
			validator.addAction(Language.I18N.getString("map.dialog.label.error.noSRS.hint"), 
					isDBConnected ? null : Language.I18N.getString("map.dialog.label.note.text"));
			validator.addSrsComboBox();
			validator.addTransformButtons();
			validator.showDialog();

			switch (validator.result) {
			case TRANSFORM:
				bbox.setSrs(validator.srsComboBox.getSelectedItem());
				return transformBoundingBox(bbox);
			case SKIP:
				return ValidationResult.SKIP;
			case CLOSE:
				return ValidationResult.CANCEL;
			default:
				return ValidationResult.CANCEL;
			}			
		} 

		// srs is known but not wgs84
		else if (bbox.getSrs().getSrid() != Database.PREDEFINED_SRS.get(PredefinedSrsName.WGS84_2D).getSrid()) {

			if (isDBConnected) {
				if (bbox.getSrs().isSupported())
					return transformBoundingBox(bbox);
			} else {
				ValidatorDialog validator = new ValidatorDialog(bbox, Language.I18N.getString("map.dialog.title.error"), config);
				validator.addErrorMessage(Language.I18N.getString("map.dialog.label.error.wgs84"));
				validator.addBoundingBox();
				validator.addAction(Language.I18N.getString("map.dialog.label.error.wgs84.hint"), Language.I18N.getString("map.dialog.label.note.text"));
				validator.addTransformButtons();
				validator.showDialog();

				switch (validator.result) {
				case TRANSFORM:
					return transformBoundingBox(bbox);
				case SKIP:
					return ValidationResult.SKIP;
				case CLOSE:
					return ValidationResult.CANCEL;
				default:
					return ValidationResult.CANCEL;
				}				
			}

			return ValidationResult.SKIP;
		}

		// srs is wgs84...
		else if (bbox.getSrs().getSrid() == Database.PREDEFINED_SRS.get(PredefinedSrsName.WGS84_2D).getSrid()) {

			// ...but coordinate values are out of range
			if (!(bbox.getLowerCorner().getX() != null && bbox.getLowerCorner().getX() >= -180 && bbox.getLowerCorner().getX() <= 180 && 
					bbox.getUpperCorner().getX() != null && bbox.getUpperCorner().getX() >= -180 && bbox.getUpperCorner().getX() <= 180 &&
					bbox.getLowerCorner().getY() != null && bbox.getLowerCorner().getY() >= -90 && bbox.getLowerCorner().getY() <= 90 &&
					bbox.getUpperCorner().getY() != null && bbox.getUpperCorner().getY() >= -90 && bbox.getUpperCorner().getY() <= 90)) {

				ValidatorDialog validator = new ValidatorDialog(bbox, Language.I18N.getString("map.dialog.title.error"), config);
				validator.addErrorMessage(Language.I18N.getString("map.dialog.label.error.range"));
				validator.addBoundingBox();
				validator.addOkButton();
				validator.showDialog();

				switch (validator.result) {
				case CLOSE:
					return ValidationResult.CANCEL;
				default:
					return ValidationResult.OUT_OF_RANGE;
				}	
			}
			
			// ...but coordinate values are invalid
			else if (bbox.getLowerCorner().getX() >= bbox.getUpperCorner().getX() ||
					bbox.getLowerCorner().getY() >= bbox.getUpperCorner().getY()) {
				ValidatorDialog validator = new ValidatorDialog(bbox, Language.I18N.getString("map.dialog.title.error"), config);
				validator.addErrorMessage(Language.I18N.getString("map.dialog.label.error.noArea"));
				validator.addBoundingBox();
				validator.addOkButton();
				validator.showDialog();
				
				switch (validator.result) {
				case CLOSE:
					return ValidationResult.CANCEL;
				default:
					return ValidationResult.NO_AREA;
				}
			}

			// ...but bounding box is not visible on screen
			else if (!map.isBoundingBoxVisible(bbox)) {
				ValidatorDialog validator = new ValidatorDialog(bbox, Language.I18N.getString("map.dialog.title.error"), config);
				validator.addErrorMessage(Language.I18N.getString("map.dialog.label.error.notVisible"));
				validator.addBoundingBox();
				validator.addOkButton();
				validator.showDialog();
				
				switch (validator.result) {
				case CLOSE:
					return ValidationResult.CANCEL;
				default:
					return ValidationResult.INVISIBLE;
				}
			}
		}

		return ValidationResult.VALID;
	}

	private ValidationResult transformBoundingBox(BoundingBox bbox) {
		final TransformDialog transform = new TransformDialog();

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				transform.setVisible(true);
			}
		});

		try {
			if (!dbController.isConnected()) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						transform.setMessage(Language.I18N.getString("main.status.database.connect.label"));
					}
				});

				dbController.connect(false);
			}

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					transform.setMessage(Language.I18N.getString("map.dialog.label.transform"));
				}
			});

			if (bbox.getSrs().isSupported()) {
				DatabaseSrs wgs84 = Database.PREDEFINED_SRS.get(PredefinedSrsName.WGS84_2D);
				for (DatabaseSrs srs : config.getProject().getDatabase().getReferenceSystems()) {
					if (srs.getSrid() == Database.PREDEFINED_SRS.get(PredefinedSrsName.WGS84_2D).getSrid()) {
						wgs84 = srs;
						break;
					}
				}
				
				bbox.copyFrom(dbController.getActiveDatabaseAdapter().getUtil().transformBoundingBox(bbox, bbox.getSrs(), wgs84));
				
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						transform.dispose();
					}
				});

				return validate(bbox);
			} else
				throw new SQLException("The spatial reference system '" + bbox.getSrs().getDescription() + "' is not supported.");

		} catch (SQLException e) {
			LOG.error("Failed to transform bounding box to WGS 84: " + e.getMessage());
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					transform.setErrorMessage(Language.I18N.getString("map.dialog.label.error.transform"));
				}
			});
		} catch (DatabaseConfigurationException | DatabaseVersionException e) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					transform.setErrorMessage(Language.I18N.getString("map.dialog.label.error.db"));
				}
			});
		}

		return ValidationResult.SKIP;
	}

	@SuppressWarnings("serial")
	public final class ValidatorDialog extends JDialog {
		private final BoundingBox bbox;
		private SrsComboBox srsComboBox;
		private int row = 0;

		private ValidatorDialogAction result = ValidatorDialogAction.SKIP;

		ValidatorDialog(BoundingBox bbox, String title, Config config) {
			super(map, title, true);

			this.bbox = bbox;
			srsComboBox = SrsComboBoxFactory.getInstance(config).createSrsComboBox(true);
			init();
		}

		private void init() {
			setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/resources/img/map/map_icon.png")));
			setLayout(new GridBagLayout());
			setBackground(Color.WHITE);			
		}

		private void addErrorMessage(String message) {
			message = message.replaceAll("\\n", "<br>");
			JLabel messageLabel = new JLabel("<html>" + message + "</html>");

			add(messageLabel, GuiUtil.setConstraints(0, row++, 1, 0, GridBagConstraints.BOTH, 10, 10, 10, 10));
		}

		private void addBoundingBox() {
			JPanel bboxPanel = new JPanel();
			bboxPanel.setLayout(new GridBagLayout());

			JLabel title = new JLabel(Language.I18N.getString("map.boundingBox.label"));
			title.setFont(title.getFont().deriveFont(Font.BOLD));
			title.setIcon(new ImageIcon(getClass().getResource("/resources/img/map/selection.png")));
			title.setIconTextGap(5);

			JLabel lowerLabel = new JLabel("Xmin / Ymin");
			JLabel upperLabel = new JLabel("Xmax / Ymax");

			JLabel lower = new JLabel(String.valueOf(bbox.getLowerCorner().getX()) + " / " + String.valueOf(bbox.getLowerCorner().getY()));
			JLabel upper = new JLabel(String.valueOf(bbox.getUpperCorner().getX()) + " / " + String.valueOf(bbox.getUpperCorner().getY()));

			bboxPanel.add(title, GuiUtil.setConstraints(0,0,0.0,0.0,GridBagConstraints.HORIZONTAL,0,2,0,5));
			bboxPanel.add(new JSeparator(JSeparator.HORIZONTAL), GuiUtil.setConstraints(1,0,1.0,0.0,GridBagConstraints.HORIZONTAL,0,10,0,5));

			GridBagConstraints c = GuiUtil.setConstraints(0,1,0.0,0.0,GridBagConstraints.NONE,5,5,0,5);
			c.anchor = GridBagConstraints.EAST;			
			bboxPanel.add(lowerLabel, c);
			bboxPanel.add(lower, GuiUtil.setConstraints(1,1,1.0,0.0,GridBagConstraints.HORIZONTAL,5,20,0,5));

			c = GuiUtil.setConstraints(0,2,0.0,0.0,GridBagConstraints.NONE,5,5,0,5);
			c.anchor = GridBagConstraints.EAST;
			bboxPanel.add(upperLabel, c);
			bboxPanel.add(upper, GuiUtil.setConstraints(1,2,1.0,0.0,GridBagConstraints.HORIZONTAL,5,20,0,5));

			if (bbox.isSetSrs()) {
				String sridText = bbox.getSrs().getSrid() != 0 ? String.valueOf(bbox.getSrs().getSrid()) : "n/a";
				JLabel description = new JLabel(bbox.getSrs().getDescription());
				JLabel srid = new JLabel(sridText);

				c = GuiUtil.setConstraints(0,3,0.0,0.0,GridBagConstraints.NONE,5,5,0,5);
				c.anchor = GridBagConstraints.EAST;
				bboxPanel.add(new JLabel(Language.I18N.getString("pref.db.srs.label.description")), c);				
				bboxPanel.add(description, GuiUtil.setConstraints(1,3,1.0,0.0,GridBagConstraints.HORIZONTAL,5,20,0,5));


				c = GuiUtil.setConstraints(0,4,0.0,0.0,GridBagConstraints.NONE,5,5,0,5);
				c.anchor = GridBagConstraints.EAST;
				bboxPanel.add(new JLabel(Language.I18N.getString("pref.db.srs.label.srid")), c);
				bboxPanel.add(srid, GuiUtil.setConstraints(1,4,1.0,0.0,GridBagConstraints.HORIZONTAL,5,20,0,5));
			}

			add(bboxPanel, GuiUtil.setConstraints(0, row++, 1, 0, GridBagConstraints.BOTH, 5, 5, 5, 5));
		}

		private void addAction(String action, String hint) {
			JPanel actionPanel = new JPanel();
			actionPanel.setLayout(new GridBagLayout());

			action = action.replaceAll("\\n", "<br>");
			GridBagConstraints c = GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.HORIZONTAL,5,5,0,5);
			c.gridwidth = 2;			
			actionPanel.add(new JLabel("<html>" + action + "</html>"), c);

			if (hint != null) {
				JLabel note = new JLabel(Language.I18N.getString("map.dialog.label.note") + ':');
				note.setFont(note.getFont().deriveFont(Font.ITALIC));
				c = GuiUtil.setConstraints(0,1,0.0,0.0,GridBagConstraints.NONE,5,5,0,5);
				c.anchor = GridBagConstraints.NORTHWEST;
				actionPanel.add(note, c);

				hint = hint.replaceAll("\\n", "<br>");
				JLabel hintLabel = new JLabel("<html>" + hint + "</html>");

				actionPanel.add(hintLabel, GuiUtil.setConstraints(1,1,0.0,0.0,GridBagConstraints.HORIZONTAL,5,5,0,5));
			}

			add(actionPanel, GuiUtil.setConstraints(0, row++, 0, 0, GridBagConstraints.BOTH, 5, 5, 5, 5));
		}

		private void addSrsComboBox() {			
			JPanel srsPanel = new JPanel();
			srsPanel.setLayout(new GridBagLayout());
			JLabel srsLabel = new JLabel(Language.I18N.getString("common.label.boundingBox.crs"));

			srsPanel.add(srsLabel, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.HORIZONTAL, 5, 5, 5, 5));
			srsPanel.add(srsComboBox, GuiUtil.setConstraints(1, 0, 1, 1, GridBagConstraints.HORIZONTAL, 5, 5, 5, 5));

			add(srsPanel, GuiUtil.setConstraints(0, row++, 1, 0, GridBagConstraints.BOTH, 5, 5, 5, 5));
		}

		private void addTransformButtons() {
			JPanel buttons = new JPanel();
			buttons.setLayout(new GridBagLayout());

			JButton transform = new JButton(Language.I18N.getString("map.dialog.button.transform"));
			JButton skip = new JButton(Language.I18N.getString("map.dialog.button.skip"));
			JButton close = new JButton(Language.I18N.getString("map.dialog.button.close"));

			buttons.add(transform, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.NONE, 10, 5, 0, 5));
			buttons.add(skip, GuiUtil.setConstraints(1, 0, 0, 0, GridBagConstraints.NONE, 10, 5, 0, 5));
			buttons.add(close, GuiUtil.setConstraints(2, 0, 0, 0, GridBagConstraints.NONE, 10, 5, 0, 5));

			transform.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					result = ValidatorDialogAction.TRANSFORM;
					dispose();
				}
			});

			skip.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					result = ValidatorDialogAction.SKIP;
					dispose();
				}
			});

			close.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					result = ValidatorDialogAction.CLOSE;
					dispose();
				}
			});

			add(buttons, GuiUtil.setConstraints(0, row++, 1, 0, GridBagConstraints.BOTH, 5, 5, 5, 5));
		}

		private void addOkButton() {
			JPanel buttons = new JPanel();
			buttons.setLayout(new GridBagLayout());

			JButton ok = new JButton(Language.I18N.getString("common.button.ok"));
			JButton close = new JButton(Language.I18N.getString("map.dialog.button.close"));

			buttons.add(ok, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.NONE, 10, 5, 0, 5));
			GridBagConstraints c = GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.NONE, 10, 5, 0, 5);
			c.anchor = GridBagConstraints.EAST;
			buttons.add(close, c);

			ok.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					result = ValidatorDialogAction.OK;
					dispose();
				}
			});

			close.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					result = ValidatorDialogAction.CLOSE;
					dispose();
				}
			});

			add(buttons, GuiUtil.setConstraints(0, row++, 1, 0, GridBagConstraints.BOTH, 5, 5, 5, 5));
		}

		private void showDialog() {
			pack();
			setLocationRelativeTo(getOwner());
			setResizable(false);
			setVisible(true);
		}

	}

	@SuppressWarnings("serial")
	public final class TransformDialog extends JDialog {		
		private JLabel messageLabel;
		private JButton button;

		TransformDialog() {
			super(map, Language.I18N.getString("map.dialog.title.transform"), true);
			init();
		}

		private void init() {
			setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
			setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/resources/img/map/map_icon.png")));
			setLayout(new GridBagLayout());
			setBackground(Color.WHITE);	

			messageLabel = new JLabel();
			messageLabel.setIcon(new ImageIcon(getClass().getResource("/resources/img/map/loader.gif")));
			messageLabel.setIconTextGap(10);

			button = new JButton(Language.I18N.getString("common.button.ok"));
			button.setVisible(false);

			add(messageLabel, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.HORIZONTAL, 10, 10, 10, 10));
			add(button, GuiUtil.setConstraints(0, 1, 0, 0, GridBagConstraints.NONE, 10, 5, 10, 5));

			button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					dispose();
				}
			});

			pack();
			setLocationRelativeTo(getOwner());
			setResizable(false);
		}

		private void setMessage(String message) {
			messageLabel.setText(message);
			messageLabel.setMinimumSize(messageLabel.getSize());
			pack();
			setLocationRelativeTo(getOwner());
		}

		private void setErrorMessage(String message) {
			message = message.replaceAll("\\n", "<br/>");
			setTitle(Language.I18N.getString("map.dialog.title.transform.error"));
			messageLabel.setIcon(new ImageIcon(getClass().getResource("/resources/img/map/error.png")));
			messageLabel.setIconTextGap(10);
			messageLabel.setVerticalTextPosition(JLabel.TOP);
			messageLabel.setText("<html>" + message + "</html>");
			messageLabel.setMinimumSize(messageLabel.getSize());
			button.setVisible(true);
			pack();
			setLocationRelativeTo(getOwner());
		}
	}
}

/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2012
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
package de.tub.citydb.gui.components.mapviewer.validation;

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

import de.tub.citydb.api.controller.DatabaseController;
import de.tub.citydb.api.database.DatabaseConfigurationException;
import de.tub.citydb.api.gui.BoundingBox;
import de.tub.citydb.api.registry.ObjectRegistry;
import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.database.Database;
import de.tub.citydb.config.project.database.Database.PredefinedSrsName;
import de.tub.citydb.gui.components.mapviewer.MapWindow;
import de.tub.citydb.gui.factory.SrsComboBoxFactory;
import de.tub.citydb.gui.factory.SrsComboBoxFactory.SrsComboBox;
import de.tub.citydb.log.Logger;
import de.tub.citydb.util.database.DBUtil;
import de.tub.citydb.util.gui.GuiUtil;

public class BoundingBoxValidator {
	private final Logger LOG = Logger.getInstance();
	private final MapWindow map;
	private final Config config;
	private final DatabaseController dbController;

	public enum ValidationResult {
		CANCEL,
		VALID,
		INVALID
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
		if (bbox.getLowerLeftCorner().getX() == null ||
				bbox.getLowerLeftCorner().getY() == null ||
				bbox.getUpperRightCorner().getX() == null ||
				bbox.getUpperRightCorner().getY() == null)
			return ValidationResult.INVALID;

		// unknown srs
		else if (!bbox.isSetSrs()) {
			ValidatorDialog validator = new ValidatorDialog(bbox, Internal.I18N.getString("map.dialog.title.error"), config);
			validator.addErrorMessage(Internal.I18N.getString("map.dialog.label.error.noSRS"));
			validator.addBoundingBox();
			validator.addAction(Internal.I18N.getString("map.dialog.label.error.noSRS.hint"), 
					isDBConnected ? null : Internal.I18N.getString("map.dialog.label.note.text"));
			validator.addSrsComboBox();
			validator.addTransformButtons();
			validator.showDialog();

			switch (validator.result) {
			case TRANSFORM:
				bbox.setSrs(validator.srsComboBox.getSelectedItem());
				return transformBoundingBox(bbox);
			case SKIP:
				return ValidationResult.INVALID;
			case CLOSE:
				return ValidationResult.CANCEL;
			}			
		} 

		// srs is known but not wgs84
		else if (bbox.getSrs().getSrid() != Database.PREDEFINED_SRS.get(PredefinedSrsName.WGS84_2D).getSrid()) {

			if (isDBConnected) {
				if (bbox.getSrs().isSupported())
					return transformBoundingBox(bbox);
			} else {
				ValidatorDialog validator = new ValidatorDialog(bbox, Internal.I18N.getString("map.dialog.title.error"), config);
				validator.addErrorMessage(Internal.I18N.getString("map.dialog.label.error.wgs84"));
				validator.addBoundingBox();
				validator.addAction(Internal.I18N.getString("map.dialog.label.error.wgs84.hint"), Internal.I18N.getString("map.dialog.label.note.text"));
				validator.addTransformButtons();
				validator.showDialog();

				switch (validator.result) {
				case TRANSFORM:
					return transformBoundingBox(bbox);
				case SKIP:
					return ValidationResult.INVALID;
				case CLOSE:
					return ValidationResult.CANCEL;
				}				
			}

			return ValidationResult.INVALID;
		}

		// srs is wgs84...
		else if (bbox.getSrs().getSrid() == Database.PREDEFINED_SRS.get(PredefinedSrsName.WGS84_2D).getSrid()) {

			// ...but coordinate values are out of range
			if (!(bbox.getLowerLeftCorner().getX() != null && bbox.getLowerLeftCorner().getX() >= -180 && bbox.getLowerLeftCorner().getX() <= 180 && 
					bbox.getUpperRightCorner().getX() != null && bbox.getUpperRightCorner().getX() >= -180 && bbox.getUpperRightCorner().getX() <= 180 &&
					bbox.getLowerLeftCorner().getY() != null && bbox.getLowerLeftCorner().getY() >= -90 && bbox.getLowerLeftCorner().getY() <= 90 &&
					bbox.getUpperRightCorner().getY() != null && bbox.getUpperRightCorner().getY() >= -90 && bbox.getUpperRightCorner().getY() <= 90)) {

				ValidatorDialog validator = new ValidatorDialog(bbox, Internal.I18N.getString("map.dialog.title.error"), config);
				validator.addErrorMessage(Internal.I18N.getString("map.dialog.label.error.range"));
				validator.addBoundingBox();
				validator.addOkButton();
				validator.showDialog();

				switch (validator.result) {
				case CLOSE:
					return ValidationResult.CANCEL;
				default:
					return ValidationResult.INVALID;
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
						transform.setMessage(Internal.I18N.getString("main.status.database.connect.label"));
					}
				});

				dbController.connect(false);
			}

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					transform.setMessage(Internal.I18N.getString("map.dialog.label.transform"));
				}
			});

			if (bbox.getSrs().isSupported()) {
				bbox.copyFrom(DBUtil.transformBBox(bbox, bbox.getSrs(), Database.PREDEFINED_SRS.get(PredefinedSrsName.WGS84_2D)));

				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						transform.dispose();
					}
				});

				return ValidationResult.VALID;
			} else
				throw new SQLException("The spatial reference system '" + bbox.getSrs().getDescription() + "' is not supported.");

		} catch (SQLException e) {
			LOG.error("Failed to transform bounding box to WGS 84: " + e.getMessage());
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					transform.setErrorMessage(Internal.I18N.getString("map.dialog.label.error.transform"));
				}
			});
		} catch (DatabaseConfigurationException e) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					transform.setErrorMessage(Internal.I18N.getString("map.dialog.label.error.db"));
				}
			});
		}

		return ValidationResult.INVALID;
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

			JLabel title = new JLabel(Internal.I18N.getString("map.boundingBox.label"));
			title.setFont(title.getFont().deriveFont(Font.BOLD));
			title.setIcon(new ImageIcon(getClass().getResource("/resources/img/map/selection.png")));
			title.setIconTextGap(5);

			JLabel lowerLabel = new JLabel("Xmin / Ymin");
			JLabel upperLabel = new JLabel("Xmax / Ymax");

			JLabel lower = new JLabel(String.valueOf(bbox.getLowerLeftCorner().getX()) + " / " + String.valueOf(bbox.getLowerLeftCorner().getY()));
			JLabel upper = new JLabel(String.valueOf(bbox.getUpperRightCorner().getX()) + " / " + String.valueOf(bbox.getUpperRightCorner().getY()));

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
				bboxPanel.add(new JLabel(Internal.I18N.getString("pref.db.srs.label.description")), c);				
				bboxPanel.add(description, GuiUtil.setConstraints(1,3,1.0,0.0,GridBagConstraints.HORIZONTAL,5,20,0,5));


				c = GuiUtil.setConstraints(0,4,0.0,0.0,GridBagConstraints.NONE,5,5,0,5);
				c.anchor = GridBagConstraints.EAST;
				bboxPanel.add(new JLabel(Internal.I18N.getString("pref.db.srs.label.srid")), c);
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
				JLabel note = new JLabel(Internal.I18N.getString("map.dialog.label.note") + ':');
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
			JLabel srsLabel = new JLabel(Internal.I18N.getString("common.label.boundingBox.crs"));

			srsPanel.add(srsLabel, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.HORIZONTAL, 5, 5, 5, 5));
			srsPanel.add(srsComboBox, GuiUtil.setConstraints(1, 0, 1, 1, GridBagConstraints.HORIZONTAL, 5, 5, 5, 5));

			add(srsPanel, GuiUtil.setConstraints(0, row++, 1, 0, GridBagConstraints.BOTH, 5, 5, 5, 5));
		}

		private void addTransformButtons() {
			JPanel buttons = new JPanel();
			buttons.setLayout(new GridBagLayout());

			JButton transform = new JButton(Internal.I18N.getString("map.dialog.button.transform"));
			JButton skip = new JButton(Internal.I18N.getString("map.dialog.button.skip"));
			JButton close = new JButton(Internal.I18N.getString("map.dialog.button.close"));

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

			JButton ok = new JButton(Internal.I18N.getString("common.button.ok"));
			JButton close = new JButton(Internal.I18N.getString("map.dialog.button.close"));

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
			super(map, Internal.I18N.getString("map.dialog.title.transform"), true);
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

			button = new JButton(Internal.I18N.getString("common.button.ok"));
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
			setTitle(Internal.I18N.getString("map.dialog.title.transform.error"));
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

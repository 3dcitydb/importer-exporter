/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2019
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

import com.formdev.flatlaf.extras.FlatSVGIcon;
import org.citydb.config.Config;
import org.citydb.config.geometry.BoundingBox;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.database.DatabaseConfig;
import org.citydb.config.project.database.DatabaseConfig.PredefinedSrsName;
import org.citydb.config.project.database.DatabaseSrs;
import org.citydb.database.DatabaseController;
import org.citydb.gui.components.common.TitledPanel;
import org.citydb.gui.components.mapviewer.MapWindow;
import org.citydb.gui.factory.PopupMenuDecorator;
import org.citydb.gui.factory.SrsComboBoxFactory;
import org.citydb.gui.factory.SrsComboBoxFactory.SrsComboBox;
import org.citydb.gui.util.GuiUtil;
import org.citydb.log.Logger;
import org.citydb.registry.ObjectRegistry;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class BoundingBoxValidator {
	private final Logger log = Logger.getInstance();
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
			ValidatorDialog validator = new ValidatorDialog(bbox, Language.I18N.getString("map.dialog.title.error"),
					Language.I18N.getString("map.dialog.label.error.noSRS"))
					.addBoundingBox()
					.addAction(Language.I18N.getString("map.dialog.label.error.noSRS.hint"),
							isDBConnected ? null : Language.I18N.getString("map.dialog.label.note.text"),
							true)
					.addTransformButtons()
					.showDialog();

			switch (validator.result) {
				case TRANSFORM:
					bbox.setSrs(validator.srsComboBox.getSelectedItem());
					return transformBoundingBox(bbox);
				case SKIP:
					return ValidationResult.SKIP;
				case CLOSE:
				default:
					return ValidationResult.CANCEL;
			}			
		} 

		// srs is known but not wgs84
		else if (bbox.getSrs().getSrid() != DatabaseConfig.PREDEFINED_SRS.get(PredefinedSrsName.WGS84_2D).getSrid()) {
			if (isDBConnected) {
				if (bbox.getSrs().isSupported())
					return transformBoundingBox(bbox);
			} else {
				ValidatorDialog validator = new ValidatorDialog(bbox, Language.I18N.getString("map.dialog.title.error"),
						Language.I18N.getString("map.dialog.label.error.wgs84"))
						.addBoundingBox()
						.addAction(Language.I18N.getString("map.dialog.label.error.wgs84.hint"),
								Language.I18N.getString("map.dialog.label.note.text"),
								false)
						.addTransformButtons()
						.showDialog();

				switch (validator.result) {
					case TRANSFORM:
						return transformBoundingBox(bbox);
					case SKIP:
						return ValidationResult.SKIP;
					case CLOSE:
					default:
						return ValidationResult.CANCEL;
				}				
			}

			return ValidationResult.SKIP;
		}

		// srs is wgs84...
		else if (bbox.getSrs().getSrid() == DatabaseConfig.PREDEFINED_SRS.get(PredefinedSrsName.WGS84_2D).getSrid()) {
			// ...but coordinate values are out of range
			if (!(bbox.getLowerCorner().getX() != null && bbox.getLowerCorner().getX() >= -180 && bbox.getLowerCorner().getX() <= 180 && 
					bbox.getUpperCorner().getX() != null && bbox.getUpperCorner().getX() >= -180 && bbox.getUpperCorner().getX() <= 180 &&
					bbox.getLowerCorner().getY() != null && bbox.getLowerCorner().getY() >= -90 && bbox.getLowerCorner().getY() <= 90 &&
					bbox.getUpperCorner().getY() != null && bbox.getUpperCorner().getY() >= -90 && bbox.getUpperCorner().getY() <= 90)) {

				ValidatorDialog validator = new ValidatorDialog(bbox, Language.I18N.getString("map.dialog.title.error"),
						Language.I18N.getString("map.dialog.label.error.range"))
						.addBoundingBox()
						.addOkButton()
						.showDialog();

				return validator.result == ValidatorDialogAction.CLOSE ?
						ValidationResult.CANCEL :
						ValidationResult.OUT_OF_RANGE;
			}
			
			// ...but coordinate values are invalid
			else if (bbox.getLowerCorner().getX() >= bbox.getUpperCorner().getX() ||
					bbox.getLowerCorner().getY() >= bbox.getUpperCorner().getY()) {
				ValidatorDialog validator = new ValidatorDialog(bbox, Language.I18N.getString("map.dialog.title.error"),
						Language.I18N.getString("map.dialog.label.error.noArea"))
						.addBoundingBox()
						.addOkButton()
						.showDialog();

				return validator.result == ValidatorDialogAction.CLOSE ?
						ValidationResult.CANCEL :
						ValidationResult.NO_AREA;
			}

			// ...but bounding box is not visible on screen
			else if (!map.isBoundingBoxVisible(bbox)) {
				ValidatorDialog validator = new ValidatorDialog(bbox, Language.I18N.getString("map.dialog.title.error"),
						Language.I18N.getString("map.dialog.label.error.notVisible"))
						.addBoundingBox()
						.addOkButton()
						.showDialog();

				return validator.result == ValidatorDialogAction.CLOSE ?
						ValidationResult.CANCEL :
						ValidationResult.INVISIBLE;
			}
		}

		return ValidationResult.VALID;
	}

	private ValidationResult transformBoundingBox(BoundingBox bbox) {
		TransformDialog transform = new TransformDialog();
		SwingUtilities.invokeLater(() -> transform.setVisible(true));

		if (!dbController.isConnected()) {
			SwingUtilities.invokeLater(() -> transform.setMessage(Language.I18N.getString("main.status.database.connect.label")));
			if (!dbController.connect(true)) {
				SwingUtilities.invokeLater(transform::dispose);
				JOptionPane.showMessageDialog(map, Language.I18N.getString("map.dialog.label.error.db"),
						Language.I18N.getString("map.dialog.title.transform.error"), JOptionPane.ERROR_MESSAGE);
				return ValidationResult.SKIP;
			}
		}

		SwingUtilities.invokeLater(() -> transform.setMessage(Language.I18N.getString("map.dialog.label.transform")));

		try {
			if (bbox.getSrs().isSupported()) {
				DatabaseSrs wgs84 = DatabaseConfig.PREDEFINED_SRS.get(PredefinedSrsName.WGS84_2D);
				for (DatabaseSrs srs : config.getDatabaseConfig().getReferenceSystems()) {
					if (srs.getSrid() == DatabaseConfig.PREDEFINED_SRS.get(PredefinedSrsName.WGS84_2D).getSrid()) {
						wgs84 = srs;
						break;
					}
				}
				
				bbox.copyFrom(dbController.getActiveDatabaseAdapter().getUtil().transformBoundingBox(bbox, bbox.getSrs(), wgs84));
				SwingUtilities.invokeLater(transform::dispose);
				return validate(bbox);
			} else
				throw new SQLException("The spatial reference system '" + bbox.getSrs().getDescription() + "' is not supported.");
		} catch (SQLException e) {
			log.error("Failed to transform bounding box to WGS 84.", e);
			SwingUtilities.invokeLater(transform::dispose);
			JOptionPane.showMessageDialog(map, Language.I18N.getString("map.dialog.label.error.db"),
					Language.I18N.getString("map.dialog.label.error.transform"), JOptionPane.ERROR_MESSAGE);
			return ValidationResult.SKIP;
		}
	}

	public final class ValidatorDialog extends JDialog {
		private final BoundingBox bbox;
		private final SrsComboBox srsComboBox;

		private final JLabel messageLabel;
		private TitledPanel bboxPanel;
		private JPanel actionPanel;
		private JPanel buttonsPanel;

		private ValidatorDialogAction result = ValidatorDialogAction.SKIP;

		ValidatorDialog(BoundingBox bbox, String title, String message) {
			super(map, title, true);
			this.bbox = bbox;
			srsComboBox = SrsComboBoxFactory.getInstance().createSrsComboBox(true);

			messageLabel = new JLabel("<html>" + message.replaceAll("\\n", "<br>") + "</html>");
			messageLabel.setIcon(new FlatSVGIcon("org/citydb/gui/icons/warning_dialog.svg"));
			messageLabel.setIconTextGap(10);
		}

		private ValidatorDialog addBoundingBox() {
			JPanel content = new JPanel();
			content.setLayout(new GridBagLayout());

			JLabel lowerLabel = new JLabel("<html>x<sub>min</sub>, y<sub>min</sub</html>");
			JLabel upperLabel = new JLabel("<html>x<sub>max</sub>, y<sub>max</sub></html>");
			JTextField lower = new JTextField(bbox.getLowerCorner().getX() + ", " + bbox.getLowerCorner().getY());
			JTextField upper = new JTextField(bbox.getUpperCorner().getX() + ", " + bbox.getUpperCorner().getY());
			lower.setEditable(false);
			upper.setEditable(false);

			PopupMenuDecorator.getInstance().decorate(upper, lower);

			content.add(lowerLabel, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 0));
			content.add(lower, GuiUtil.setConstraints(1, 0, 1, 0, GridBagConstraints.HORIZONTAL, 0, 20, 0, 0));
			content.add(upperLabel, GuiUtil.setConstraints(0, 2, 0, 0, GridBagConstraints.HORIZONTAL, 5, 0, 0, 0));
			content.add(upper, GuiUtil.setConstraints(1, 2, 1, 0, GridBagConstraints.HORIZONTAL, 5, 20, 0, 0));

			if (bbox.isSetSrs()) {
				String sridText = bbox.getSrs().getSrid() != 0 ? String.valueOf(bbox.getSrs().getSrid()) : "n/a";
				JTextField description = new JTextField(bbox.getSrs().getDescription());
				JTextField srid = new JTextField(sridText);
				description.setEditable(false);
				srid.setEditable(false);

				content.add(new JLabel(Language.I18N.getString("pref.db.srs.label.description")), GuiUtil.setConstraints(0, 3, 0, 0, GridBagConstraints.HORIZONTAL, 5, 0, 0, 0));
				content.add(description, GuiUtil.setConstraints(1, 3, 1, 0, GridBagConstraints.HORIZONTAL, 5, 20, 0, 0));
				content.add(new JLabel(Language.I18N.getString("pref.db.srs.label.srid")), GuiUtil.setConstraints(0, 4, 0, 0, GridBagConstraints.HORIZONTAL, 5, 0, 0, 0));
				content.add(srid, GuiUtil.setConstraints(1, 4, 1, 0, GridBagConstraints.HORIZONTAL, 5, 20, 0, 0));

				PopupMenuDecorator.getInstance().decorate(description, srid);
			}

			bboxPanel = new TitledPanel()
					.withTitle(Language.I18N.getString("map.boundingBox.label"))
					.withMargin(new Insets(0, 0, 0, 0))
					.build(content);

			return this;
		}

		private ValidatorDialog addAction(String action, String hint, boolean requiresSrsInput) {
			actionPanel = new JPanel();
			actionPanel.setLayout(new GridBagLayout());

			JLabel actionLabel = new JLabel("<html>" + action.replaceAll("\\n", "<br>") + "</html>");
			actionPanel.add(actionLabel,GuiUtil.setConstraints(0, 0, 2, 1, 1, 0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 0));

			if (requiresSrsInput) {
				JLabel srsLabel = new JLabel(Language.I18N.getString("common.label.boundingBox.crs"));
				actionPanel.add(srsLabel, GuiUtil.setConstraints(0, 1, 0, 0, GridBagConstraints.HORIZONTAL, 5, 0, 0, 5));
				actionPanel.add(srsComboBox, GuiUtil.setConstraints(1, 1, 1, 1, GridBagConstraints.HORIZONTAL, 5, 5, 0, 0));
			}

			if (hint != null) {
				JLabel note = new JLabel(Language.I18N.getString("map.dialog.label.note") + ":");
				note.setFont(note.getFont().deriveFont(Font.ITALIC));
				JLabel hintLabel = new JLabel("<html>" + hint.replaceAll("\\n", "<br>") + "</html>");
				actionPanel.add(note, GuiUtil.setConstraints(0, 2, 0, 0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, 5, 0, 0, 5));
				actionPanel.add(hintLabel, GuiUtil.setConstraints(1, 2, 0, 0, GridBagConstraints.HORIZONTAL, 5, 5, 0, 0));
			}

			return this;
		}

		private ValidatorDialog addTransformButtons() {
			buttonsPanel = new JPanel();
			buttonsPanel.setLayout(new GridBagLayout());

			JButton transform = new JButton(Language.I18N.getString("map.dialog.button.transform"));
			JButton skip = new JButton(Language.I18N.getString("map.dialog.button.skip"));
			JButton close = new JButton(Language.I18N.getString("map.dialog.button.close"));

			buttonsPanel.add(transform, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.NONE, 0, 0, 0, 5));
			buttonsPanel.add(skip, GuiUtil.setConstraints(1, 0, 0, 0, GridBagConstraints.NONE, 0, 5, 0, 5));
			buttonsPanel.add(close, GuiUtil.setConstraints(2, 0, 0, 0, GridBagConstraints.NONE, 0, 5, 0, 0));

			transform.addActionListener(e -> {
				result = ValidatorDialogAction.TRANSFORM;
				dispose();
			});

			skip.addActionListener(e -> {
				result = ValidatorDialogAction.SKIP;
				dispose();
			});

			close.addActionListener(e -> {
				result = ValidatorDialogAction.CLOSE;
				dispose();
			});

			return this;
		}

		private ValidatorDialog addOkButton() {
			buttonsPanel = new JPanel();
			buttonsPanel.setLayout(new GridBagLayout());

			JButton ok = new JButton(Language.I18N.getString("common.button.ok"));
			JButton close = new JButton(Language.I18N.getString("map.dialog.button.close"));

			buttonsPanel.add(ok, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.NONE, 0, 0, 0, 5));
			buttonsPanel.add(close, GuiUtil.setConstraints(1, 0, 0, 0, GridBagConstraints.NONE, 0, 5, 0, 0));

			ok.addActionListener(e -> {
				result = ValidatorDialogAction.OK;
				dispose();
			});

			close.addActionListener(e -> {
				result = ValidatorDialogAction.CLOSE;
				dispose();
			});

			return this;
		}

		private ValidatorDialog showDialog() {
			setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			setIconImage(new FlatSVGIcon("org/citydb/gui/icons/map.svg").getImage());
			setLayout(new GridBagLayout());

			int row = 0;
			JPanel main = new JPanel();
			main.setLayout(new GridBagLayout());
			main.add(messageLabel, GuiUtil.setConstraints(0, row++, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));

			if (bboxPanel != null) {
				main.add(bboxPanel, GuiUtil.setConstraints(0, row++, 1, 0, GridBagConstraints.BOTH, 15, 0, 0, 0));
			}

			if (actionPanel != null) {
				main.add(actionPanel, GuiUtil.setConstraints(0, row, 1, 0, GridBagConstraints.BOTH, 15, 0, 0, 0));
			}

			if (buttonsPanel == null) {
				addOkButton();
			}

			add(main, GuiUtil.setConstraints(0, 0, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, 10, 10, 0, 10));
			add(buttonsPanel, GuiUtil.setConstraints(0, 1, 1, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, 15, 10, 10, 10));

			pack();
			setLocationRelativeTo(getOwner());
			setVisible(true);
			return this;
		}
	}

	public final class TransformDialog extends JDialog {
		private JLabel messageLabel;

		TransformDialog() {
			super(map, Language.I18N.getString("map.dialog.title.transform"), true);
			init();
		}

		private void init() {
			setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
			setIconImage(new FlatSVGIcon("org/citydb/gui/icons/map.svg").getImage());
			setLayout(new GridBagLayout());

			messageLabel = new JLabel();
			messageLabel.setIcon(new ImageIcon(getClass().getResource("/org/citydb/gui/icons/loader.gif")));
			add(messageLabel, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, 10, 10, 10, 10));

			pack();
			setLocationRelativeTo(getOwner());
		}

		private void setMessage(String message) {
			messageLabel.setText(message);
			messageLabel.setMinimumSize(messageLabel.getSize());
			pack();
			setLocationRelativeTo(getOwner());
		}
	}
}

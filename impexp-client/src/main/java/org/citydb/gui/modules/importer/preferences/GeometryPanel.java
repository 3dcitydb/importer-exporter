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
package org.citydb.gui.modules.importer.preferences;

import org.citydb.config.Config;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.common.AffineTransformation;
import org.citydb.config.project.common.TransformationMatrix;
import org.citydb.gui.components.common.TitledPanel;
import org.citydb.gui.factory.PopupMenuDecorator;
import org.citydb.gui.modules.common.AbstractPreferencesComponent;
import org.citydb.gui.util.GuiUtil;
import org.citygml4j.geometry.Matrix;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GeometryPanel extends AbstractPreferencesComponent {
	private TitledPanel matrixPanel;
	private JPanel buttonsPanel;
	private JCheckBox useAffineTransformation;
	private JLabel matrixDescr;
	private JFormattedTextField[][] matrixField;
	private JLabel[] matrixLabels;
	private JButton identityMatrixButton;
	private JButton swapXYMatrixButton;
	
	public GeometryPanel(Config config) {
		super(config);
		initGui();
	}

	@Override
	public boolean isModified() {
		AffineTransformation affineTransformation = config.getImportConfig().getAffineTransformation();

		if (useAffineTransformation.isSelected() != affineTransformation.isEnabled()) return true;
		
		Matrix matrix = toMatrix3x4(affineTransformation.getTransformationMatrix());
		boolean isModified = false;
		for (int i = 0; i < matrixField.length; i++) {
			for (int j = 0; j < matrixField[i].length; j++) {
				try { matrixField[i][j].commitEdit(); } catch (ParseException ignored) { }
				if (!isModified)
					isModified = ((Number)matrixField[i][j].getValue()).doubleValue() != matrix.get(i, j);
			}
		}
		
		return isModified;
	}

	private void initGui() {
		useAffineTransformation = new JCheckBox();
		matrixDescr = new JLabel();
		matrixField = new JFormattedTextField[3][4];
		matrixLabels = new JLabel[3];
		identityMatrixButton = new JButton();
		swapXYMatrixButton = new JButton();

		matrixDescr.setFont(matrixDescr.getFont().deriveFont(Font.BOLD));

		setLayout(new GridBagLayout());
		{
			JPanel content = new JPanel();
			content.setLayout(new GridBagLayout());

			JPanel matrixFieldsPanel = new JPanel();
			matrixFieldsPanel.setLayout(new GridBagLayout());
			{
				NumberFormatter formatter = new NumberFormatter(new DecimalFormat("#.#########################",
						DecimalFormatSymbols.getInstance(Locale.ENGLISH)));
				formatter.setMaximum(Double.MAX_VALUE);
				formatter.setMinimum(-Double.MAX_VALUE);

				for (int i = 0; i < matrixField.length; i++) {
					StringBuilder label = new StringBuilder("<html>(m");
					for (int k = 0; k < matrixField[i].length; k++) {
						label.append("<sub>").append(i + 1).append(k + 1).append("</sub>");
						if (k < matrixField[i].length - 1)
							label.append(",m");
						else
							label.append(") =</html>");
					}

					matrixLabels[i] = new JLabel(label.toString());
					matrixFieldsPanel.add(matrixLabels[i], GuiUtil.setConstraints(0, i, 0, 0, GridBagConstraints.NONE, 0, 0, 5, 0));

					for (int j = 0; j < matrixField[i].length; j++) {
						matrixField[i][j] = new JFormattedTextField(formatter);
						matrixField[i][j].setColumns(8);
						matrixFieldsPanel.add(matrixField[i][j], GuiUtil.setConstraints(j + 1, i, 0.25, 0, GridBagConstraints.BOTH, 0, 5, 5, 0));
					}

					PopupMenuDecorator.getInstance().decorate(matrixField[i]);
				}
			}

			buttonsPanel = new JPanel();
			buttonsPanel.setLayout(new GridBagLayout());
			{
				buttonsPanel.add(identityMatrixButton, GuiUtil.setConstraints(0, 0, 0, 1, GridBagConstraints.BOTH, 0, 0, 0, 5));
				buttonsPanel.add(swapXYMatrixButton, GuiUtil.setConstraints(1, 0, 0, 1, GridBagConstraints.BOTH, 0, 5, 0, 0));
			}

			content.add(matrixDescr, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.BOTH, 0, 0, 5, 0));
			content.add(matrixFieldsPanel, GuiUtil.setConstraints(0, 1, 1, 1, GridBagConstraints.BOTH, 5, 0, 5, 0));
			content.add(buttonsPanel, GuiUtil.setConstraints(0, 2, 1, 1, GridBagConstraints.BOTH, 5, 0, 0, 0));

			matrixPanel = new TitledPanel()
					.withToggleButton(useAffineTransformation)
					.build(content);
		}

		add(matrixPanel, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));

		identityMatrixButton.addActionListener(e -> {
			Matrix matrix = Matrix.identity(3, 4);
			double[][] values = matrix.getArray();

			for (int i = 0; i < values.length; i++) {
				for (int j = 0; j < values[i].length; j++) {
					matrixField[i][j].setValue(values[i][j]);
				}
			}
		});
		
		swapXYMatrixButton.addActionListener(e -> {
			Matrix matrix = new Matrix(3, 4, 0);
			matrix.set(0, 1, 1);
			matrix.set(1, 0, 1);
			matrix.set(2, 2, 1);
			double[][] values = matrix.getArray();

			for (int i = 0; i < values.length; i++) {
				for (int j = 0; j < values[i].length; j++) {
					matrixField[i][j].setValue(values[i][j]);
				}
			}
		});
		
		useAffineTransformation.addActionListener(e -> setEnabledTransformation());
	}
	
	private void setEnabledTransformation() {
		boolean enabled = useAffineTransformation.isSelected();

		matrixDescr.setEnabled(enabled);
		for (int i = 0; i < matrixField.length; i++) {
			matrixLabels[i].setEnabled(enabled);
			for (int j = 0; j < matrixField[i].length; j++) {
				matrixField[i][j].setEnabled(enabled);
			}
		}
				
		identityMatrixButton.setEnabled(enabled);
		swapXYMatrixButton.setEnabled(enabled);
	}

	@Override
	public void doTranslation() {
		matrixPanel.setTitle(Language.I18N.getString("common.pref.geometry.border.transformation"));
		matrixDescr.setText(Language.I18N.getString("common.pref.geometry.label.matrix"));
		identityMatrixButton.setText(Language.I18N.getString("common.pref.geometry.button.identity"));
		swapXYMatrixButton.setText(Language.I18N.getString("common.pref.geometry.button.swapXY"));
	}

	@Override
	public void loadSettings() {
		AffineTransformation affineTransformation = config.getImportConfig().getAffineTransformation();
			
		useAffineTransformation.setSelected(affineTransformation.isEnabled());

		Matrix matrix = toMatrix3x4(affineTransformation.getTransformationMatrix());
		double[][] values = matrix.getArray();

		for (int i = 0; i < values.length; i++)
			for (int j = 0; j < values[i].length; j++)
				matrixField[i][j].setValue(values[i][j]);

		setEnabledTransformation();
	}

	@Override
	public void setSettings() {
		AffineTransformation affineTransformation = config.getImportConfig().getAffineTransformation();
		
		affineTransformation.setEnabled(useAffineTransformation.isSelected());
		
		List<Double> values = new ArrayList<>();
		for (JFormattedTextField[] fields : matrixField)
			for (JFormattedTextField field : fields)
				values.add(((Number) field.getValue()).doubleValue());
		
		affineTransformation.getTransformationMatrix().setValue(values);
		
		// disable affine transformation if transformation matrix equals identity matrix
		if (toMatrix3x4(affineTransformation.getTransformationMatrix()).eq(Matrix.identity(3, 4))) {
			affineTransformation.setEnabled(false);
			useAffineTransformation.setSelected(false);
			setEnabledTransformation();
		}
	}
	
	@Override
	public String getTitle() {
		return Language.I18N.getString("pref.tree.import.geometry");
	}
	
	private Matrix toMatrix3x4(TransformationMatrix transformationMatrix) {
		return transformationMatrix.isSetValue()
				&& transformationMatrix.getValue().size() == 12 ?
				new Matrix(transformationMatrix.getValue(), 3) :
				Matrix.identity(3, 4);
	}

}

/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2013
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
package de.tub.citydb.modules.citygml.importer.gui.preferences;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import org.citygml4j.geometry.Matrix;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.general.AffineTransformation;
import de.tub.citydb.gui.factory.PopupMenuDecorator;
import de.tub.citydb.gui.preferences.AbstractPreferencesComponent;
import de.tub.citydb.modules.common.filter.FilterMode;
import de.tub.citydb.util.gui.GuiUtil;

@SuppressWarnings("serial")
public class GeometryPanel extends AbstractPreferencesComponent {	
	private JPanel block1;
	private JPanel block2;
	private JCheckBox useAffineTransformation;
	private JLabel matrixDescr;
	private JFormattedTextField[][] matrixField;
	private JLabel[] matrixLabel;
	private JButton identityMatrixButton;
	private JButton swapXYMatrixButton;
	
	private FilterMode mode;

	public GeometryPanel(FilterMode mode, Config config) {
		super(config);
		this.mode = mode;
		initGui();
	}

	@Override
	public boolean isModified() {
		AffineTransformation affineTransformation = null;
		if (mode == FilterMode.IMPORT)
			affineTransformation = config.getProject().getImporter().getAffineTransformation();

		if (useAffineTransformation.isSelected() != affineTransformation.isSetUseAffineTransformation()) return true;
		
		Matrix matrix = affineTransformation.getTransformationMatrix().toMatrix3x4();
		boolean isModified = false;
		for (int i = 0; i < matrixField.length; i++) {
			for (int j = 0; j < matrixField[i].length; j++) {
				try { matrixField[i][j].commitEdit(); } catch (ParseException e) { };
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
		matrixLabel = new JLabel[3];
		identityMatrixButton = new JButton();
		swapXYMatrixButton = new JButton();

		setLayout(new GridBagLayout());
		{
			block1 = new JPanel();
			add(block1, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));
			block1.setBorder(BorderFactory.createTitledBorder(""));
			block1.setLayout(new GridBagLayout());
			{
				JPanel row1 = new JPanel();
				row1.setBorder(BorderFactory.createEmptyBorder());
				block1.add(row1, GuiUtil.setConstraints(0,0,1.0,1.0,GridBagConstraints.BOTH,0,0,5,0));
				row1.setLayout(new GridBagLayout());
				{
					useAffineTransformation.setIconTextGap(10);
					matrixDescr.setFont(matrixDescr.getFont().deriveFont(Font.BOLD));
					row1.add(useAffineTransformation, GuiUtil.setConstraints(0,0,1.0,1.0,GridBagConstraints.BOTH,0,5,15,5));
					row1.add(matrixDescr, GuiUtil.setConstraints(0,1,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
				}

				JPanel row2 = new JPanel();
				row2.setBorder(BorderFactory.createEmptyBorder());
				block1.add(row2, GuiUtil.setConstraints(0,1,1.0,1.0,GridBagConstraints.BOTH,0,0,0,5));
				row2.setLayout(new GridBagLayout());
				{
					DecimalFormat format = new DecimalFormat("####################.#########################", DecimalFormatSymbols.getInstance(Locale.ENGLISH));	
					format.setMaximumIntegerDigits(20);
					format.setMinimumIntegerDigits(1);
					format.setMaximumFractionDigits(25);
					format.setMinimumFractionDigits(0);
					
					for (int i = 0; i < matrixField.length; i++) {
						StringBuilder label = new StringBuilder("<html>(m");
						for (int k = 0; k < matrixField[i].length; k++) {
							label.append("<sub>").append(i+1).append(k+1).append("</sub>");

							if (k < matrixField[i].length - 1)
								label.append(",m");
							else
								label.append(") =</html>");
						}

						matrixLabel[i] = new JLabel(label.toString());
						row2.add(matrixLabel[i], GuiUtil.setConstraints(0,i,0.0,0.0,GridBagConstraints.NONE,0,5,5,0));
						
						for (int j = 0; j < matrixField[i].length; j++) {
							matrixField[i][j] = new JFormattedTextField(format);							
							row2.add(matrixField[i][j], GuiUtil.setConstraints(j+1,i,0.25,0.0,GridBagConstraints.BOTH,0,5,5,0));
							matrixField[i][j].setPreferredSize(matrixField[i][j].getPreferredSize());
						}
						
						PopupMenuDecorator.getInstance().decorate(matrixField[i]);
					}
				}
			}
						
			block2 = new JPanel();
			block2.setBorder(BorderFactory.createTitledBorder(""));
			add(block2, GuiUtil.setConstraints(0,1,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));
			block2.setBorder(BorderFactory.createTitledBorder(""));
			block2.setLayout(new GridBagLayout());
			{
				block2.add(identityMatrixButton, GuiUtil.setConstraints(0,0,0.0,1.0,GridBagConstraints.BOTH,5,5,5,5));
				block2.add(swapXYMatrixButton, GuiUtil.setConstraints(1,0,0.0,1.0,GridBagConstraints.BOTH,5,5,5,5));
			}
		}

		identityMatrixButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Matrix matrix = Matrix.identity(3, 4);
				double[][] values = matrix.getArray();

				for (int i = 0; i < values.length; i++) {
					for (int j = 0; j < values[i].length; j++) {
						matrixField[i][j].setValue(values[i][j]);
					}
				}
			}
		});
		
		swapXYMatrixButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Matrix matrix = new Matrix(3, 4, 0.0);
				matrix.set(0, 1, 1.0);
				matrix.set(1, 0, 1.0);
				matrix.set(2, 2, 1.0);
				double[][] values = matrix.getArray();
				
				for (int i = 0; i < values.length; i++) {
					for (int j = 0; j < values[i].length; j++) {
						matrixField[i][j].setValue(values[i][j]);
					}
				}
			}
		});
		
		useAffineTransformation.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setEnabledTransformation();
			}
		});
	}

	private void setEnabledBorderTitle() {
		boolean enabled = useAffineTransformation.isSelected();

		Color titleColor = enabled ? UIManager.getColor("TitledBorder.titleColor") : UIManager.getColor("Label.disabledForeground");
		((TitledBorder)block2.getBorder()).setTitleColor(titleColor);
		block2.repaint();
	}
	
	private void setEnabledTransformation() {
		boolean enabled = useAffineTransformation.isSelected();
		Color labelColor = enabled ? UIManager.getColor("Label.enableForeground") : UIManager.getColor("Label.disabledForeground");
		
		matrixDescr.setEnabled(enabled);
		for (int i = 0; i < matrixField.length; i++) {
			matrixLabel[i].setForeground(labelColor);
			for (int j = 0; j < matrixField[i].length; j++) {
				matrixField[i][j].setEnabled(enabled);
			}
		}
				
		identityMatrixButton.setEnabled(enabled);
		swapXYMatrixButton.setEnabled(enabled);
		setEnabledBorderTitle();
	}

	@Override
	public void doTranslation() {
		((TitledBorder)block1.getBorder()).setTitle(Internal.I18N.getString("common.pref.geometry.border.transformation"));	
		((TitledBorder)block2.getBorder()).setTitle(Internal.I18N.getString("common.pref.geometry.border.preset"));	

		useAffineTransformation.setText(Internal.I18N.getString("common.pref.geometry.label.useTransformation"));
		matrixDescr.setText(Internal.I18N.getString("common.pref.geometry.label.matrix"));
		identityMatrixButton.setText(Internal.I18N.getString("common.pref.geometry.button.identity"));
		swapXYMatrixButton.setText(Internal.I18N.getString("common.pref.geometry.button.swapXY"));
		
		setEnabledBorderTitle();
	}

	@Override
	public void loadSettings() {
		AffineTransformation affineTransformation = null;
		if (mode == FilterMode.IMPORT)
			affineTransformation = config.getProject().getImporter().getAffineTransformation();
			
		useAffineTransformation.setSelected(affineTransformation.isSetUseAffineTransformation());

		Matrix matrix = affineTransformation.getTransformationMatrix().toMatrix3x4();
		double[][] values = matrix.getArray();

		for (int i = 0; i < values.length; i++)
			for (int j = 0; j < values[i].length; j++)
				matrixField[i][j].setValue(values[i][j]);

		setEnabledTransformation();
	}

	@Override
	public void setSettings() {
		AffineTransformation affineTransformation = null;
		if (mode == FilterMode.IMPORT)
			affineTransformation = config.getProject().getImporter().getAffineTransformation();
		
		affineTransformation.setUseAffineTransformation(useAffineTransformation.isSelected());
		
		List<Double> values = new ArrayList<Double>();
		for (int i = 0; i < matrixField.length; i++)
			for (int j = 0; j < matrixField[i].length; j++)
				values.add(((Number)matrixField[i][j].getValue()).doubleValue());
		
		affineTransformation.getTransformationMatrix().setValue(values);
		
		// disable affine transformation if transformation matrix equals identity matrix
		if (affineTransformation.getTransformationMatrix().toMatrix3x4().eq(Matrix.identity(3, 4)))
			affineTransformation.setUseAffineTransformation(false);
	}
	
	@Override
	public String getTitle() {
		return Internal.I18N.getString("pref.tree.import.geometry");
	}

}

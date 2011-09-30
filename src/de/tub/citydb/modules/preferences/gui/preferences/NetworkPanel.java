/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2011
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
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
package de.tub.citydb.modules.preferences.gui.preferences;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.text.ParseException;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JPasswordField;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.gui.factory.PopupMenuDecorator;
import de.tub.citydb.gui.preferences.AbstractPreferencesComponent;
import de.tub.citydb.util.gui.GuiUtil;

@SuppressWarnings("serial")
public class NetworkPanel extends AbstractPreferencesComponent{
	private JPanel block1;
	private JLabel proxyHostLabel;
	private JTextField proxyHostText;
	private JLabel proxyPortLabel;
	private JFormattedTextField proxyPortText;	
	private JLabel proxyUserLabel;
	private JTextField proxyUserText;
	private JLabel proxyPasswordLabel;
	private JPasswordField proxyPasswordText;
	private JCheckBox passwordCheck;

	public NetworkPanel(Config config) {
		super(config);
		initGui();
	}

	@Override
	public boolean isModified() {
		try { proxyPortText.commitEdit(); } catch (ParseException e) { }

		if (!proxyHostText.getText().trim().equals(config.getProject().getGlobal().getNetwork().getProxyHost())) return true;
		if (((Number)proxyPortText.getValue()).intValue() != config.getProject().getGlobal().getNetwork().getProxyPort()) return true;
		if (!proxyUserText.getText().trim().equals(config.getProject().getGlobal().getNetwork().getProxyUser())) return true;
		if (passwordCheck.isSelected() != config.getProject().getGlobal().getNetwork().isSavePassword()) return true;
		
		if (!String.valueOf(proxyPasswordText.getPassword()).equals(config.getProject().getGlobal().getNetwork().getInternalProxyPassword())) return true;
		if (passwordCheck.isSelected() &&
			!String.valueOf(proxyPasswordText.getPassword()).equals(config.getProject().getGlobal().getNetwork().getProxyPassword())) return true;

		return false;
	}

	private void initGui(){
		block1 = new JPanel();
		proxyHostLabel = new JLabel();
		proxyPortLabel = new JLabel();
		proxyUserLabel = new JLabel();
		proxyPasswordLabel = new JLabel();

		DecimalFormat fiveIntFormat = new DecimalFormat("#####");	
		fiveIntFormat.setMaximumIntegerDigits(5);
		fiveIntFormat.setMinimumIntegerDigits(1);
		proxyHostText = new JTextField();
		proxyPortText = new JFormattedTextField(fiveIntFormat);
		proxyUserText = new JTextField();
		proxyPasswordText = new JPasswordField();
		passwordCheck = new JCheckBox();
		passwordCheck.setIconTextGap(10);
		
		PopupMenuDecorator.getInstance().decorate(proxyHostText, proxyPortText, proxyUserText, proxyPasswordText);
		
		proxyPortText.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				checkNonNegativeRange(proxyPortText, 0, 65535);
			}
		});
		
		setLayout(new GridBagLayout());
		add(block1, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));
		block1.setBorder(BorderFactory.createTitledBorder(""));
		block1.setLayout(new GridBagLayout());
		{
			block1.add(proxyHostLabel, GuiUtil.setConstraints(0,0,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block1.add(proxyHostText, GuiUtil.setConstraints(1,0,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block1.add(proxyPortLabel, GuiUtil.setConstraints(0,1,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block1.add(proxyPortText, GuiUtil.setConstraints(1,1,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block1.add(proxyUserLabel, GuiUtil.setConstraints(0,2,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block1.add(proxyUserText, GuiUtil.setConstraints(1,2,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block1.add(proxyPasswordLabel, GuiUtil.setConstraints(0,3,0.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block1.add(proxyPasswordText, GuiUtil.setConstraints(1,3,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));
			block1.add(passwordCheck, GuiUtil.setConstraints(1,4,0.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
		}

	}
	
	private void checkNonNegativeRange(JFormattedTextField field, int min, int max) {
		if (((Number)field.getValue()).intValue() < min)
			field.setValue(min);
		else if (((Number)field.getValue()).intValue() > max)
			field.setValue(max);
	}

	@Override
	public void doTranslation() {
		block1.setBorder(BorderFactory.createTitledBorder(Internal.I18N.getString("common.pref.network.border.proxySettings")));
		proxyHostLabel.setText(Internal.I18N.getString("common.pref.network.label.proxyHost"));
		proxyPortLabel.setText(Internal.I18N.getString("common.pref.network.label.proxyPort"));
		proxyUserLabel.setText(Internal.I18N.getString("common.pref.network.label.proxyUser"));
		proxyPasswordLabel.setText(Internal.I18N.getString("common.pref.network.label.proxyPassword"));
		passwordCheck.setText(Internal.I18N.getString("db.label.passwordCheck"));
	}

	@Override
	public void loadSettings() {
		proxyHostText.setText(config.getProject().getGlobal().getNetwork().getProxyHost());
		proxyPortText.setValue(config.getProject().getGlobal().getNetwork().getProxyPort());
		proxyUserText.setText(config.getProject().getGlobal().getNetwork().getProxyUser());
		passwordCheck.setSelected(config.getProject().getGlobal().getNetwork().isSavePassword());
		if (passwordCheck.isSelected())
			proxyPasswordText.setText(config.getProject().getGlobal().getNetwork().getProxyPassword());
		else
			proxyPasswordText.setText(config.getProject().getGlobal().getNetwork().getInternalProxyPassword());
	}

	@Override
	public void setSettings() {
		config.getProject().getGlobal().getNetwork().setProxyHost(proxyHostText.getText().trim());
		config.getProject().getGlobal().getNetwork().setProxyPort(((Number)proxyPortText.getValue()).intValue());
		config.getProject().getGlobal().getNetwork().setProxyUser(proxyUserText.getText().trim());
		config.getProject().getGlobal().getNetwork().setInternalProxyPassword(String.valueOf(proxyPasswordText.getPassword()));
		config.getProject().getGlobal().getNetwork().setSavePassword(passwordCheck.isSelected());
		if (passwordCheck.isSelected()) {
			config.getProject().getGlobal().getNetwork().setProxyPassword(String.valueOf(proxyPasswordText.getPassword()));
		}
		else {
			config.getProject().getGlobal().getNetwork().setProxyPassword("");
		}
	}
	
	@Override
	public String getTitle() {
		return Internal.I18N.getString("pref.tree.general.network");
	}
}


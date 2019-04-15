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
package org.citydb.gui.components.dialog;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.citydb.config.i18n.Language;
import org.citydb.gui.util.GuiUtil;

public class ADESupportWarningDialog {
	private boolean shouldShow = true;

	public int show(JFrame topFrame) {
		int option = JOptionPane.OK_OPTION;
		
		if (shouldShow) {
			JPanel confirmPanel = new JPanel(new GridBagLayout());
			JCheckBox confirmDialogNoShow = new JCheckBox(Language.I18N.getString("common.dialog.msg.noShow"));
			confirmDialogNoShow.setIconTextGap(10);
			confirmPanel.add(new JLabel(Language.I18N.getString("common.dialog.warning.ade.unsupported")), GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,0,0,0,0));
			confirmPanel.add(confirmDialogNoShow, GuiUtil.setConstraints(0,2,1.0,0.0,GridBagConstraints.BOTH,10,0,0,0));
			
			option = JOptionPane.showConfirmDialog(topFrame, confirmPanel, Language.I18N.getString("common.dialog.warning.title"), JOptionPane.OK_CANCEL_OPTION);
			
			if (confirmDialogNoShow.isSelected()) {
				shouldShow = false;
			}
		}
		
		return option;
	}
	
}

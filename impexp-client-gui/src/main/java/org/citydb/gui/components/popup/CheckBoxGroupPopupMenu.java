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
package org.citydb.gui.components.popup;

import org.citydb.config.i18n.Language;
import org.citydb.util.event.Event;
import org.citydb.util.event.EventHandler;
import org.citydb.util.event.global.EventType;
import org.citydb.core.registry.ObjectRegistry;

import javax.swing.*;
import java.util.Arrays;

public class CheckBoxGroupPopupMenu extends AbstractPopupMenu implements EventHandler {
	private JMenuItem selectOthers;
	private JMenuItem deselectOthers;
	private JMenuItem selectAll;
	private JMenuItem deselectAll;
	private JMenuItem invert;

	private JCheckBox checkBox;
	private JCheckBox[] group;
	
	public CheckBoxGroupPopupMenu() {
		ObjectRegistry.getInstance().getEventDispatcher().addEventHandler(EventType.SWITCH_LOCALE, this);
	}
	
	public void init(JCheckBox checkBox, JCheckBox... group) {
		this.checkBox = checkBox;
		this.group = group;

		selectOthers = new JMenuItem();
		deselectOthers = new JMenuItem();
		selectAll = new JMenuItem();
		deselectAll = new JMenuItem();
		invert = new JMenuItem();

		selectOthers.addActionListener(e -> setSelected(group, true, true));
		deselectOthers.addActionListener(e -> setSelected(group, false, true));
		selectAll.addActionListener(e -> setSelected(group, true, false));
		deselectAll.addActionListener(e -> setSelected(group, false, false));
		
		invert.addActionListener(e -> {
			for (JCheckBox member : group) {
				member.setSelected(!member.isSelected());
			}
		});
		
		add(selectOthers);
		add(deselectOthers);
		addSeparator();		
		add(selectAll);
		add(deselectAll);
		add(invert);
	}

	public void prepare() {
		selectOthers.setEnabled(Arrays.stream(group).anyMatch(c -> c != checkBox && !c.isSelected()));
		deselectOthers.setEnabled(Arrays.stream(group).anyMatch(c -> c != checkBox && c.isSelected()));
		selectAll.setEnabled(Arrays.stream(group).anyMatch(c -> !c.isSelected()));
		deselectAll.setEnabled(Arrays.stream(group).anyMatch(AbstractButton::isSelected));
	}

	private void setSelected(JCheckBox[] group, boolean selected, boolean skipSelf) {
		for (JCheckBox member : group) {
			if (skipSelf && member == checkBox) {
				continue;
			}

			member.setSelected(selected);
		}
	}
	
	@Override
	public void doTranslation() {
		selectOthers.setText(Language.I18N.getString("common.popup.checkbox.selectOthers"));
		deselectOthers.setText(Language.I18N.getString("common.popup.checkbox.deselectOthers"));
		selectAll.setText(Language.I18N.getString("common.popup.checkbox.selectAll"));
		deselectAll.setText(Language.I18N.getString("common.popup.checkbox.deselectAll"));
		invert.setText(Language.I18N.getString("common.popup.checkbox.invert"));
	}

	@Override
	public void handleEvent(Event event) throws Exception {
		doTranslation();
	}

}

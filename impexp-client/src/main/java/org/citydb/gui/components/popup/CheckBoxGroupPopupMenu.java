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
package org.citydb.gui.components.popup;

import org.citydb.config.i18n.Language;
import org.citydb.event.Event;
import org.citydb.event.EventHandler;
import org.citydb.event.global.EventType;
import org.citydb.registry.ObjectRegistry;

import javax.swing.*;

public class CheckBoxGroupPopupMenu extends AbstractPopupMenu implements EventHandler {
	private JMenuItem selectOthers;
	private JMenuItem deselectOthers;
	private JMenuItem selectAll;
	private JMenuItem deselectAll;
	private JMenuItem invert;
	
	public CheckBoxGroupPopupMenu() {
		ObjectRegistry.getInstance().getEventDispatcher().addEventHandler(EventType.SWITCH_LOCALE, this);
	}
	
	public void init(final int index, final JCheckBox... group) {
		selectOthers = new JMenuItem();
		deselectOthers = new JMenuItem();
		selectAll = new JMenuItem();
		deselectAll = new JMenuItem();
		invert = new JMenuItem();

		selectOthers.addActionListener(e -> {
			for (int i = 0; i < group.length; i++) {
				if (i == index) {
					continue;
				}

				group[i].setSelected(true);
			}
		});
		
		deselectOthers.addActionListener(e -> {
			for (int i = 0; i < group.length; i++) {
				if (i == index) {
					continue;
				}

				group[i].setSelected(false);
			}
		});
		
		selectAll.addActionListener(e -> {
			for (JCheckBox jCheckBox : group) {
				jCheckBox.setSelected(true);
			}
		});
		
		deselectAll.addActionListener(e -> {
			for (JCheckBox jCheckBox : group) {
				jCheckBox.setSelected(false);
			}
		});
		
		invert.addActionListener(e -> {
			for (JCheckBox jCheckBox : group) {
				jCheckBox.setSelected(!jCheckBox.isSelected());
			}
		});
		
		add(selectOthers);
		add(deselectOthers);
		addSeparator();		
		add(selectAll);
		add(deselectAll);
		add(invert);
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

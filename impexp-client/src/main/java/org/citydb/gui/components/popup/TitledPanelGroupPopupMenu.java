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
import org.citydb.gui.components.common.TitledPanel;
import org.citydb.registry.ObjectRegistry;

import javax.swing.*;
import java.util.Arrays;

public class TitledPanelGroupPopupMenu extends AbstractPopupMenu implements EventHandler {
	private JMenuItem expand;
	private JMenuItem expandAll;
	private JMenuItem collapse;
	private JMenuItem collapseAll;
	private Separator separator;
	private JMenuItem selectOthers;
	private JMenuItem deselectOthers;
	private JMenuItem selectAll;
	private JMenuItem deselectAll;
	private JMenuItem invert;

	private TitledPanel titledPanel;
	private TitledPanel[] group;

	public TitledPanelGroupPopupMenu() {
		ObjectRegistry.getInstance().getEventDispatcher().addEventHandler(EventType.SWITCH_LOCALE, this);
	}
	
	public void init(final int index, TitledPanel titledPanel, TitledPanel... group) {
		this.titledPanel = titledPanel;
		this.group = group;

		if (titledPanel.isCollapsible()) {
			expand = new JMenuItem();
			expandAll = new JMenuItem();
			collapse = new JMenuItem();
			collapseAll = new JMenuItem();
			separator = new Separator();

			expand.addActionListener(e -> titledPanel.setCollapsed(false));
			expandAll.addActionListener(e -> setCollapsed(group, false));
			collapse.addActionListener(e -> titledPanel.setCollapsed(true));
			collapseAll.addActionListener(e -> setCollapsed(group, true));

			add(expand);
			add(expandAll);
			add(separator);
			add(collapse);
			add(collapseAll);

			if (titledPanel.hasToggleButton()) {
				addSeparator();
			}
		}

		if (titledPanel.hasToggleButton()) {
			selectOthers = new JMenuItem();
			deselectOthers = new JMenuItem();
			selectAll = new JMenuItem();
			deselectAll = new JMenuItem();
			invert = new JMenuItem();

			selectOthers.addActionListener(e -> setSelected(group, index, true));
			deselectOthers.addActionListener(e -> setSelected(group, index, false));
			selectAll.addActionListener(e -> setSelected(group, true));
			deselectAll.addActionListener(e -> setSelected(group, false));

			invert.addActionListener(e -> {
				for (TitledPanel panel : group) {
					if (panel.hasToggleButton()) {
						panel.getToggleButton().setSelected(!panel.getToggleButton().isSelected());
					}
				}
			});

			add(selectOthers);
			add(deselectOthers);
			addSeparator();
			add(selectAll);
			add(deselectAll);
			add(invert);
		}
	}

	public void prepare() {
		if (titledPanel.isCollapsible()) {
			expand.setVisible(titledPanel.isCollapsed());
			collapse.setVisible(!titledPanel.isCollapsed());
			expandAll.setVisible(Arrays.stream(group)
					.anyMatch(p -> p != titledPanel && p.isCollapsible() && p.isCollapsed()));
			collapseAll.setVisible(Arrays.stream(group)
					.anyMatch(p -> p != titledPanel && p.isCollapsible() && !p.isCollapsed()));
			separator.setVisible((expand.isVisible() || expandAll.isVisible())
					&& (collapse.isVisible() || collapseAll.isVisible()));
		}
	}

	private void setCollapsed(TitledPanel[] group, boolean collapsed) {
		for (TitledPanel panel : group) {
			panel.setCollapsed(collapsed);
		}
	}

	private void setSelected(TitledPanel[] group, int index, boolean selected) {
		for (int i = 0; i < group.length; i++) {
			if (i == index || !group[i].hasToggleButton()) {
				continue;
			}

			group[i].getToggleButton().setSelected(selected);
		}
	}

	private void setSelected(TitledPanel[] group, boolean selected) {
		for (TitledPanel titledPanel : group) {
			if (titledPanel.hasToggleButton()) {
				titledPanel.getToggleButton().setSelected(selected);
			}
		}
	}

	@Override
	public void doTranslation() {
		if (titledPanel.isCollapsible()) {
			expand.setText(Language.I18N.getString("common.popup.expand"));
			expandAll.setText(Language.I18N.getString("common.popup.expandAll"));
			collapse.setText(Language.I18N.getString("common.popup.collapse"));
			collapseAll.setText(Language.I18N.getString("common.popup.collapseAll"));
		}

		if (titledPanel.hasToggleButton()) {
			selectOthers.setText(Language.I18N.getString("common.popup.checkbox.selectOthers"));
			deselectOthers.setText(Language.I18N.getString("common.popup.checkbox.deselectOthers"));
			selectAll.setText(Language.I18N.getString("common.popup.checkbox.selectAll"));
			deselectAll.setText(Language.I18N.getString("common.popup.checkbox.deselectAll"));
			invert.setText(Language.I18N.getString("common.popup.checkbox.invert"));
		}
	}

	@Override
	public void handleEvent(Event event) throws Exception {
		doTranslation();
	}
}

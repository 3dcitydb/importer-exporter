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
			expandAll.addActionListener(e -> {
				for (TitledPanel panel : group) {
					panel.setCollapsed(false);
				}
			});

			collapse.addActionListener(e -> titledPanel.setCollapsed(true));
			collapseAll.addActionListener(e -> {
				for (TitledPanel panel : group) {
					panel.setCollapsed(true);
				}
			});

			add(expand);
			add(expandAll);
			add(separator);
			add(collapse);
			add(collapseAll);
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

	@Override
	public void doTranslation() {
		if (titledPanel.isCollapsible()) {
			expand.setText(Language.I18N.getString("pref.popup.expand"));
			expandAll.setText(Language.I18N.getString("pref.popup.expandAll"));
			collapse.setText(Language.I18N.getString("pref.popup.collapse"));
			collapseAll.setText(Language.I18N.getString("pref.popup.collapseAll"));
		}
	}

	@Override
	public void handleEvent(Event event) throws Exception {
		doTranslation();
	}
}

/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2024
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
import org.citydb.core.registry.ObjectRegistry;
import org.citydb.gui.components.TitledPanel;
import org.citydb.util.event.Event;
import org.citydb.util.event.EventHandler;
import org.citydb.util.event.global.EventType;
import org.citydb.util.event.global.SwitchLocaleEvent;

import javax.swing.*;
import java.util.Arrays;
import java.util.Locale;

public class TitledPanelGroupPopupMenu extends AbstractPopupMenu implements EventHandler {
    private JMenuItem expand;
    private JMenuItem expandAll;
    private JMenuItem collapse;
    private JMenuItem collapseAll;
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

    public void init(TitledPanel titledPanel, TitledPanel... group) {
        this.titledPanel = titledPanel;
        this.group = group;

        if (titledPanel.isCollapsible()) {
            expand = new JMenuItem();
            expandAll = new JMenuItem();
            collapse = new JMenuItem();
            collapseAll = new JMenuItem();

            expand.addActionListener(e -> titledPanel.setCollapsed(false));
            expandAll.addActionListener(e -> setCollapsed(group, false));
            collapse.addActionListener(e -> titledPanel.setCollapsed(true));
            collapseAll.addActionListener(e -> setCollapsed(group, true));

            add(expand);
            add(expandAll);
            addSeparator();
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

            selectOthers.addActionListener(e -> setSelected(group, true, true));
            deselectOthers.addActionListener(e -> setSelected(group, false, true));
            selectAll.addActionListener(e -> setSelected(group, true, false));
            deselectAll.addActionListener(e -> setSelected(group, false, false));

            invert.addActionListener(e -> {
                for (TitledPanel member : group) {
                    if (member.hasToggleButton()) {
                        member.getToggleButton().setSelected(!member.getToggleButton().isSelected());
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
            expandAll.setEnabled(Arrays.stream(group).anyMatch(p -> p.isCollapsible() && p.isCollapsed()));
            collapse.setVisible(!titledPanel.isCollapsed());
            collapseAll.setEnabled(Arrays.stream(group).anyMatch(p -> p.isCollapsible() && !p.isCollapsed()));
        }

        if (titledPanel.hasToggleButton()) {
            selectOthers.setEnabled(Arrays.stream(group)
                    .anyMatch(p -> p != titledPanel && p.hasToggleButton() && !p.getToggleButton().isSelected()));
            deselectOthers.setEnabled(Arrays.stream(group)
                    .anyMatch(p -> p != titledPanel && p.hasToggleButton() && p.getToggleButton().isSelected()));
            selectAll.setEnabled(Arrays.stream(group)
                    .anyMatch(p -> p.hasToggleButton() && !p.getToggleButton().isSelected()));
            deselectAll.setEnabled(Arrays.stream(group)
                    .anyMatch(p -> p.hasToggleButton() && p.getToggleButton().isSelected()));
        }
    }

    private void setCollapsed(TitledPanel[] group, boolean collapsed) {
        for (TitledPanel panel : group) {
            panel.setCollapsed(collapsed);
        }
    }

    private void setSelected(TitledPanel[] group, boolean selected, boolean skipSelf) {
        for (TitledPanel member : group) {
            if (skipSelf && member == titledPanel) {
                continue;
            }

            if (member.hasToggleButton()) {
                member.getToggleButton().setSelected(selected);
            }
        }
    }

    @Override
    public void switchLocale(Locale locale) {
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
        switchLocale(((SwitchLocaleEvent) event).getLocale());
    }
}

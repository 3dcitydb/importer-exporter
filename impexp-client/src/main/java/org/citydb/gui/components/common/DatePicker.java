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

package org.citydb.gui.components.common;

import org.citydb.config.i18n.Language;
import org.citydb.event.Event;
import org.citydb.event.EventHandler;
import org.citydb.event.global.EventType;
import org.citydb.event.global.SwitchLocaleEvent;
import org.citydb.registry.ObjectRegistry;
import org.jdesktop.swingx.JXDatePicker;
import org.jdesktop.swingx.JXMonthView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DateFormatSymbols;
import java.util.Calendar;

public class DatePicker extends JXDatePicker implements EventHandler {
    private JPanel linkPanel;
    private JLabel todayLink;

    public DatePicker() {
        ObjectRegistry.getInstance().getEventDispatcher().addEventHandler(EventType.SWITCH_LOCALE, this);
        setFormats("yyyy-MM-dd", "dd.MM.yyyy");
    }

    @Override
    public JPanel getLinkPanel() {
        return linkPanel != null ? linkPanel : createLinkPanel();
    }

    private JPanel createLinkPanel() {
        linkPanel = new JPanel();
        linkPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

        todayLink = new JLabel();
        todayLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        linkPanel.add(todayLink);

        todayLink.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    Action delegate = getActionMap().get(e.getClickCount() != 2 ? JXDatePicker.HOME_NAVIGATE_KEY : JXDatePicker.HOME_COMMIT_KEY);
                    if (delegate != null && delegate.isEnabled())
                        delegate.actionPerformed(null);
                }
            }
        });

        return linkPanel;
    }

    @Override
    public void updateUI() {
        super.updateUI();
        getEditor().putClientProperty("JTextField.placeholderText", "YYYY-MM-DD");
        getEditor().setColumns(8);

        if (linkPanel != null) {
            SwingUtilities.updateComponentTreeUI(linkPanel);
        }
    }

    @Override
    public void handleEvent(Event event) {
        SwitchLocaleEvent localeEvent = (SwitchLocaleEvent) event;
        setLocale(localeEvent.getLocale());

        todayLink.setText("<html><a href=\"#\">" + Language.I18N.getString("common.label.datePicker.today") + "</a></html>");

        String[] daysOfTheWeek = new String[JXMonthView.DAYS_IN_WEEK];
        String[] dateFormatSymbols = DateFormatSymbols.getInstance(localeEvent.getLocale()).getShortWeekdays();
        for (int i = Calendar.SUNDAY; i <= Calendar.SATURDAY; i++) {
            daysOfTheWeek[i - 1] = dateFormatSymbols[i].replaceAll("\\.$", "");
        }

        getMonthView().setDaysOfTheWeek(daysOfTheWeek);
        getMonthView().setFirstDayOfWeek(Calendar.MONDAY);
        getMonthView().setPreferredSize(null);
    }
}

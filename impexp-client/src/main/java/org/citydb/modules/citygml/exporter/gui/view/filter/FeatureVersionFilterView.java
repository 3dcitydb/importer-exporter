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

package org.citydb.modules.citygml.exporter.gui.view.filter;

import org.citydb.config.Config;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.query.simple.SimpleFeatureVersionFilter;
import org.citydb.config.project.query.simple.SimpleFeatureVersionFilterMode;
import org.citydb.gui.components.common.DatePicker;
import org.citydb.gui.factory.PopupMenuDecorator;
import org.citydb.gui.util.GuiUtil;
import org.jdesktop.swingx.JXFormattedTextField;
import org.jdesktop.swingx.prompt.PromptSupport;

import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.text.DateFormatter;
import javax.swing.text.DefaultFormatterFactory;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;

public class FeatureVersionFilterView extends FilterView {
    private DatatypeFactory datatypeFactory;
    private JPanel component;

    private JComboBox<FeatureFilterMode> featureFilterMode;
    private JComboBox<DateMode> dateMode;
    private DatePicker startDate;
    private JXFormattedTextField startTime;
    private JLabel endDateLabel;
    private DatePicker endDate;
    private JXFormattedTextField endTime;

    public FeatureVersionFilterView(Config config) {
        super(config);

        try {
            datatypeFactory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new IllegalStateException("Failed to create a new instance of DatatypeFactory");
        }

        init();
    }

    private void init() {
        component = new JPanel();
        component.setLayout(new GridBagLayout());

        startDate = new DatePicker();
        startTime = new JXFormattedTextField();
        endDateLabel = new JLabel("bis");
        endDate = new DatePicker();
        endTime = new JXFormattedTextField();

        startTime.setPromptForeground(Color.LIGHT_GRAY);
        startTime.setFocusBehavior(PromptSupport.FocusBehavior.SHOW_PROMPT);
        startTime.setPrompt("HH:MM:SS");
        endTime.setPromptForeground(Color.LIGHT_GRAY);
        endTime.setFocusBehavior(PromptSupport.FocusBehavior.SHOW_PROMPT);
        endTime.setPrompt("HH:MM:SS");

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        startTime.setFormatterFactory(new DefaultFormatterFactory(new DateFormatter(timeFormat)));
        endTime.setFormatterFactory(new DefaultFormatterFactory(new DateFormatter(timeFormat)));

        featureFilterMode = new JComboBox<>();
        for (FeatureFilterMode mode : FeatureFilterMode.values())
            featureFilterMode.addItem(mode);

        dateMode = new JComboBox<>();
        for (DateMode mode : DateMode.values())
            dateMode.addItem(mode);

        component.add(featureFilterMode, GuiUtil.setConstraints(0,0,1,0, GridBagConstraints.HORIZONTAL,0,0,0,5));
        component.add(dateMode, GuiUtil.setConstraints(1,0,0,0,GridBagConstraints.HORIZONTAL,0,20,0,5));
        component.add(startDate, GuiUtil.setConstraints(2,0,1,0,GridBagConstraints.HORIZONTAL,0,5,0,5));
        component.add(startTime, GuiUtil.setConstraints(3,0,1,0,GridBagConstraints.HORIZONTAL,0,5,0,5));
        component.add(endDateLabel, GuiUtil.setConstraints(4,0,0,0,GridBagConstraints.HORIZONTAL,0,10,0,5));
        component.add(endDate, GuiUtil.setConstraints(5,0,1,0,GridBagConstraints.HORIZONTAL,0,5,0,5));
        component.add(endTime, GuiUtil.setConstraints(6,0,1,0,GridBagConstraints.HORIZONTAL,0,5,0,0));
        startTime.setPreferredSize(startTime.getPreferredSize());
        endTime.setPreferredSize(endTime.getPreferredSize());

        startTime.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                if (startTime.getValue() != null && startTime.getText().trim().length() == 0)
                    startTime.setValue(null);
            }
        });

        endTime.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                if (endTime.getValue() != null && startTime.getText().trim().length() == 0)
                    endTime.setValue(null);
            }
        });

        featureFilterMode.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                setEnabledStartDate();
                setEnabledEndDate();
            }
        });

        dateMode.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED)
                setEnabledEndDate();
        });

        PopupMenuDecorator.getInstance().decorate(startDate.getEditor(), startTime, endDate.getEditor(), endTime);
    }

    @Override
    public void doTranslation() {
        endDateLabel.setText(Language.I18N.getString("filter.label.featureVersion.to"));
        featureFilterMode.updateUI();
        dateMode.updateUI();
    }

    @Override
    public void setEnabled(boolean enable) {
        featureFilterMode.setEnabled(enable);

        if (enable) {
            setEnabledStartDate();
            setEnabledEndDate();
        } else {
            dateMode.setEnabled(false);
            startDate.setEnabled(false);
            startTime.setEnabled(false);
            endDateLabel.setEnabled(false);
            endDate.setEnabled(false);
            endTime.setEnabled(false);
        }
    }

    private void setEnabledStartDate() {
        boolean enable = featureFilterMode.getSelectedItem() == FeatureFilterMode.VALID;
        dateMode.setEnabled(enable);
        startDate.setEnabled(enable);
        startTime.setEnabled(enable);
    }

    private void setEnabledEndDate() {
        boolean enable = featureFilterMode.getSelectedItem() == FeatureFilterMode.VALID && dateMode.getSelectedItem() == DateMode.FROM;
        endDateLabel.setEnabled(enable);
        endDate.setEnabled(enable);
        endTime.setEnabled(enable);
    }

    @Override
    public void loadSettings() {
        SimpleFeatureVersionFilter featureVersionFilter = config.getProject().getExporter().getSimpleQuery().getFeatureVersionFilter();

        switch (featureVersionFilter.getMode()) {
            case LATEST:
                featureFilterMode.setSelectedItem(FeatureFilterMode.LATEST);
                break;
            case AT:
                featureFilterMode.setSelectedItem(FeatureFilterMode.VALID);
                dateMode.setSelectedItem(DateMode.AT);
                break;
            case BETWEEN:
                featureFilterMode.setSelectedItem(FeatureFilterMode.VALID);
                dateMode.setSelectedItem(DateMode.FROM);
                break;
        }

        if (featureVersionFilter.isSetStartDate()) {
            ZonedDateTime dateTime = featureVersionFilter.getStartDate().toGregorianCalendar().toZonedDateTime();
            Date date = Date.from(dateTime.toInstant());
            startDate.setDate(date);
            startTime.setValue(!dateTime.toLocalTime().equals(LocalTime.MIN) ? date : null);
        } else {
            startDate.setDate(null);
            startTime.setValue(null);
        }

        if (featureVersionFilter.isSetEndDate()) {
            ZonedDateTime dateTime = featureVersionFilter.getEndDate().toGregorianCalendar().toZonedDateTime();
            Date date = Date.from(dateTime.toInstant());
            endDate.setDate(date);
            endTime.setValue(!dateTime.toLocalTime().equals(LocalTime.MIN) ? date : null);
        } else {
            endDate.setDate(null);
            endTime.setValue(null);
        }
    }

    @Override
    public void setSettings() {
        SimpleFeatureVersionFilter featureVersionFilter = config.getProject().getExporter().getSimpleQuery().getFeatureVersionFilter();

        if (featureFilterMode.getSelectedItem() == FeatureFilterMode.VALID) {
            if (dateMode.getSelectedItem() == DateMode.AT)
                featureVersionFilter.setMode(SimpleFeatureVersionFilterMode.AT);
            else if (dateMode.getSelectedItem() == DateMode.FROM)
                featureVersionFilter.setMode(SimpleFeatureVersionFilterMode.BETWEEN);
        } else
            featureVersionFilter.setMode(SimpleFeatureVersionFilterMode.LATEST);

        featureVersionFilter.setStartDate(toCalendar(startDate.getDate(), (Date) startTime.getValue()));
        featureVersionFilter.setEndDate(toCalendar(endDate.getDate(), (Date) endTime.getValue()));
    }

    @Override
    public String getLocalizedTitle() {
        return null;
    }

    @Override
    public Component getViewComponent() {
        return component;
    }

    @Override
    public String getToolTip() {
        return null;
    }

    @Override
    public Icon getIcon() {
        return null;
    }

    private XMLGregorianCalendar toCalendar(Date date, Date time) {
        if (date != null) {
            LocalDate localDate = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()).toLocalDate();
            LocalTime localTime = time != null ?
                    LocalDateTime.ofInstant(time.toInstant(), ZoneId.systemDefault()).toLocalTime() :
                    LocalTime.MIN;

            ZonedDateTime dateTime = ZonedDateTime.of(localDate, localTime, ZoneId.systemDefault());
            return datatypeFactory.newXMLGregorianCalendar(
                    dateTime.getYear(),
                    dateTime.getMonthValue(),
                    dateTime.getDayOfMonth(),
                    dateTime.getHour(),
                    dateTime.getMinute(),
                    dateTime.getSecond(),
                    DatatypeConstants.FIELD_UNDEFINED,
                    dateTime.getOffset() != ZoneOffset.UTC ? dateTime.getOffset().getTotalSeconds() / 60 : DatatypeConstants.FIELD_UNDEFINED);
        } else
            return null;
    }

    private enum FeatureFilterMode {
        LATEST,
        VALID;

        @Override
        public String toString() {
            switch (this) {
                case LATEST:
                    return Language.I18N.getString("filter.label.featureVersion.latest");
                case VALID:
                    return Language.I18N.getString("filter.label.featureVersion.valid");
                default:
                    return "";
            }
        }
    }

    private enum DateMode {
        AT,
        FROM;

        @Override
        public String toString() {
            switch (this) {
                case AT:
                    return Language.I18N.getString("filter.label.featureVersion.at");
                case FROM:
                    return Language.I18N.getString("filter.label.featureVersion.from");
                default:
                    return "";
            }
        }
    }
}

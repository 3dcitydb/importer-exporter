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

package org.citydb.gui.operation.common.filter;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.query.simple.SimpleFeatureVersionFilter;
import org.citydb.config.project.query.simple.SimpleFeatureVersionFilterMode;
import org.citydb.core.registry.ObjectRegistry;
import org.citydb.gui.components.DatePicker;
import org.citydb.gui.components.popup.PopupMenuDecorator;
import org.citydb.gui.util.GuiUtil;

import javax.swing.*;
import javax.swing.text.DateFormatter;
import javax.swing.text.DefaultFormatterFactory;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class FeatureVersionFilterView extends FilterView<SimpleFeatureVersionFilter> {
    private final DatatypeFactory datatypeFactory;
    private JPanel component;
    private JComboBox<FeatureFilterMode> featureFilterMode;

    private JComboBox<TimestampMode> validMode;
    private DatePicker validStartDate;
    private JFormattedTextField validStartTime;
    private JLabel validEndDateLabel;
    private DatePicker validEndDate;
    private JFormattedTextField validEndTime;

    private JComboBox<TimestampMode> terminatedMode;
    private DatePicker terminatedAtDate;
    private JFormattedTextField terminatedAtTime;

    public FeatureVersionFilterView() {
        datatypeFactory = ObjectRegistry.getInstance().getDatatypeFactory();
        init();
    }

    private void init() {
        component = new JPanel();
        component.setLayout(new GridBagLayout());

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        timeFormat.setTimeZone(TimeZone.getTimeZone(ZoneOffset.UTC));
        DefaultFormatterFactory formatterFactory = new DefaultFormatterFactory(new DateFormatter(timeFormat));

        featureFilterMode = new JComboBox<>();
        for (FeatureFilterMode mode : FeatureFilterMode.values()) {
            featureFilterMode.addItem(mode);
        }

        JPanel timestampPanel = new JPanel();
        timestampPanel.setLayout(new CardLayout());
        {
            timestampPanel.add(Box.createHorizontalGlue(), FeatureFilterMode.LATEST.name());
        }
        {
            JPanel content = new JPanel();
            content.setLayout(new GridBagLayout());

            validMode = new JComboBox<>();
            validMode.addItem(TimestampMode.AT);
            validMode.addItem(TimestampMode.FROM);

            validStartDate = new DatePicker();
            validStartTime = new JFormattedTextField();
            validEndDateLabel = new JLabel();
            validEndDate = new DatePicker();
            validEndTime = new JFormattedTextField();

            validStartDate.setTimeZone(TimeZone.getTimeZone(ZoneOffset.UTC));
            validEndDate.setTimeZone(TimeZone.getTimeZone(ZoneOffset.UTC));
            validStartTime.putClientProperty("JTextField.placeholderText", "HH:MM:SS");
            validEndTime.putClientProperty("JTextField.placeholderText", "HH:MM:SS");
            validStartTime.setFormatterFactory(formatterFactory);
            validEndTime.setFormatterFactory(formatterFactory);

            content.add(validMode, GuiUtil.setConstraints(1, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 5));
            content.add(validStartDate, GuiUtil.setConstraints(2, 0, 1, 0, GridBagConstraints.HORIZONTAL, 0, 5, 0, 5));
            content.add(validStartTime, GuiUtil.setConstraints(3, 0, 1, 0, GridBagConstraints.HORIZONTAL, 0, 5, 0, 5));
            content.add(validEndDateLabel, GuiUtil.setConstraints(4, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 10, 0, 5));
            content.add(validEndDate, GuiUtil.setConstraints(5, 0, 1, 0, GridBagConstraints.HORIZONTAL, 0, 5, 0, 5));
            content.add(validEndTime, GuiUtil.setConstraints(6, 0, 1, 0, GridBagConstraints.HORIZONTAL, 0, 5, 0, 0));
            validStartTime.setPreferredSize(validStartTime.getPreferredSize());
            validEndTime.setPreferredSize(validEndTime.getPreferredSize());

            timestampPanel.add(content, FeatureFilterMode.VALID.name());
        }
        {
            JPanel content = new JPanel();
            content.setLayout(new GridBagLayout());

            terminatedMode = new JComboBox<>();
            terminatedMode.addItem(TimestampMode.ALL);
            terminatedMode.addItem(TimestampMode.AT);

            terminatedAtDate = new DatePicker();
            terminatedAtTime = new JFormattedTextField();
            terminatedAtTime.putClientProperty("JTextField.placeholderText", "HH:MM:SS");
            terminatedAtTime.setFormatterFactory(formatterFactory);

            content.add(terminatedMode, GuiUtil.setConstraints(1, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 5));
            content.add(terminatedAtDate, GuiUtil.setConstraints(2, 0, 1, 0, GridBagConstraints.HORIZONTAL, 0, 5, 0, 5));
            content.add(terminatedAtTime, GuiUtil.setConstraints(3, 0, 1, 0, GridBagConstraints.HORIZONTAL, 0, 5, 0, 0));
            terminatedAtTime.setPreferredSize(terminatedAtTime.getPreferredSize());

            timestampPanel.add(content, FeatureFilterMode.TERMINATED.name());
        }

        component.add(featureFilterMode, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 0));
        component.add(timestampPanel, GuiUtil.setConstraints(1, 0, 1, 0, GridBagConstraints.HORIZONTAL, 0, 25, 0, 0));

        featureFilterMode.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                FeatureFilterMode mode = (FeatureFilterMode) e.getItem();
                ((CardLayout) timestampPanel.getLayout()).show(timestampPanel, mode.name());

                if (mode == FeatureFilterMode.VALID) {
                    setEnabledValidMode();
                } else if (mode == FeatureFilterMode.TERMINATED) {
                    setEnabledTerminatedMode();
                }
            }
        });

        validMode.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                setEnabledValidEndDate();
            }
        });

        terminatedMode.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                setEnabledTerminatedAtDate();
            }
        });

        FocusAdapter focusLostAdapter = new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                if (e.getSource() instanceof JFormattedTextField) {
                    JFormattedTextField source = (JFormattedTextField) e.getSource();
                    if (source.getValue() != null && source.getText().trim().length() == 0) {
                        source.setValue(null);
                    }
                }
            }
        };

        validStartTime.addFocusListener(focusLostAdapter);
        validEndTime.addFocusListener(focusLostAdapter);
        terminatedAtTime.addFocusListener(focusLostAdapter);

        PopupMenuDecorator.getInstance().decorate(validStartDate.getEditor(), validStartTime, validEndDate.getEditor(),
                validEndTime, terminatedAtDate.getEditor(), terminatedAtTime);
    }

    @Override
    public String getLocalizedTitle() {
        return Language.I18N.getString("filter.border.featureVersion");
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
        return new FlatSVGIcon("org/citydb/gui/filter/history.svg");
    }

    @Override
    public void switchLocale(Locale locale) {
        validEndDateLabel.setText(Language.I18N.getString("filter.label.featureVersion.to"));
        featureFilterMode.updateUI();
        validMode.updateUI();
        terminatedMode.updateUI();
    }

    @Override
    public void setEnabled(boolean enable) {
        featureFilterMode.setEnabled(enable);
        setEnabledValidMode();
        setEnabledTerminatedMode();
    }

    private void setEnabledValidMode() {
        setEnabledValidStartDate();
        setEnabledValidEndDate();
    }

    private void setEnabledValidStartDate() {
        boolean enable = featureFilterMode.isEnabled()
                && featureFilterMode.getSelectedItem() == FeatureFilterMode.VALID;
        validMode.setEnabled(enable);
        validStartDate.setEnabled(enable);
        validStartTime.setEnabled(enable);
    }

    private void setEnabledValidEndDate() {
        boolean enable = featureFilterMode.isEnabled()
                && featureFilterMode.getSelectedItem() == FeatureFilterMode.VALID
                && validMode.getSelectedItem() == TimestampMode.FROM;
        validEndDateLabel.setEnabled(enable);
        validEndDate.setEnabled(enable);
        validEndTime.setEnabled(enable);
    }

    private void setEnabledTerminatedMode() {
        boolean enable = featureFilterMode.isEnabled()
                && featureFilterMode.getSelectedItem() == FeatureFilterMode.TERMINATED;
        terminatedMode.setEnabled(enable);
        setEnabledTerminatedAtDate();
    }

    private void setEnabledTerminatedAtDate() {
        boolean enable = featureFilterMode.isEnabled()
                && featureFilterMode.getSelectedItem() == FeatureFilterMode.TERMINATED
                && terminatedMode.getSelectedItem() == TimestampMode.AT;
        terminatedAtDate.setEnabled(enable);
        terminatedAtTime.setEnabled(enable);
    }

    @Override
    public void loadSettings(SimpleFeatureVersionFilter featureVersionFilter) {
        switch (featureVersionFilter.getMode()) {
            case LATEST:
                featureFilterMode.setSelectedItem(FeatureFilterMode.LATEST);
                break;
            case AT:
                featureFilterMode.setSelectedItem(FeatureFilterMode.VALID);
                validMode.setSelectedItem(TimestampMode.AT);
                break;
            case BETWEEN:
                featureFilterMode.setSelectedItem(FeatureFilterMode.VALID);
                validMode.setSelectedItem(TimestampMode.FROM);
                break;
            case TERMINATED:
                featureFilterMode.setSelectedItem(FeatureFilterMode.TERMINATED);
                terminatedMode.setSelectedItem(TimestampMode.ALL);
                break;
            case TERMINATED_AT:
                featureFilterMode.setSelectedItem(FeatureFilterMode.TERMINATED);
                terminatedMode.setSelectedItem(TimestampMode.AT);
                break;
        }

        ZonedDateTime startDate = featureVersionFilter.isSetStartDate() ?
                toZonedDateTime(featureVersionFilter.getStartDate()) :
                null;

        ZonedDateTime endDate = featureVersionFilter.isSetEndDate() ?
                toZonedDateTime(featureVersionFilter.getEndDate()) :
                null;

        if (featureFilterMode.getSelectedItem() == FeatureFilterMode.VALID) {
            setDateTime(startDate, validStartDate, validStartTime);
            setDateTime(endDate, validEndDate, validEndTime);
        } else if (featureFilterMode.getSelectedItem() == FeatureFilterMode.TERMINATED) {
            setDateTime(startDate, terminatedAtDate, terminatedAtTime);
        }
    }

    @Override
    public SimpleFeatureVersionFilter toSettings() {
        SimpleFeatureVersionFilter featureVersionFilter = new SimpleFeatureVersionFilter();
        if (featureFilterMode.getSelectedItem() == FeatureFilterMode.VALID) {
            if (validMode.getSelectedItem() == TimestampMode.AT) {
                featureVersionFilter.setMode(SimpleFeatureVersionFilterMode.AT);
            } else if (validMode.getSelectedItem() == TimestampMode.FROM) {
                featureVersionFilter.setMode(SimpleFeatureVersionFilterMode.BETWEEN);
            }

            featureVersionFilter.setStartDate(toCalendar(validStartDate.getDate(), (Date) validStartTime.getValue()));
            featureVersionFilter.setEndDate(toCalendar(validEndDate.getDate(), (Date) validEndTime.getValue()));
        } else if (featureFilterMode.getSelectedItem() == FeatureFilterMode.TERMINATED) {
            if (terminatedMode.getSelectedItem() == TimestampMode.ALL) {
                featureVersionFilter.setMode(SimpleFeatureVersionFilterMode.TERMINATED);
            } else if (terminatedMode.getSelectedItem() == TimestampMode.AT) {
                featureVersionFilter.setMode(SimpleFeatureVersionFilterMode.TERMINATED_AT);
            }

            featureVersionFilter.setStartDate(toCalendar(terminatedAtDate.getDate(), (Date) terminatedAtTime.getValue()));
            featureVersionFilter.setEndDate(null);
        } else {
            featureVersionFilter.setMode(SimpleFeatureVersionFilterMode.LATEST);
        }

        return featureVersionFilter;
    }

    private XMLGregorianCalendar toCalendar(Date date, Date time) {
        if (date != null) {
            LocalDate localDate = LocalDateTime.ofInstant(date.toInstant(), ZoneOffset.UTC).toLocalDate();
            LocalTime localTime = time != null ?
                    LocalDateTime.ofInstant(time.toInstant(), ZoneOffset.UTC).toLocalTime() :
                    LocalTime.MAX.withNano(0);

            ZonedDateTime dateTime = ZonedDateTime.of(localDate, localTime, ZoneOffset.UTC);
            return datatypeFactory.newXMLGregorianCalendar(
                    dateTime.getYear(),
                    dateTime.getMonthValue(),
                    dateTime.getDayOfMonth(),
                    dateTime.getHour(),
                    dateTime.getMinute(),
                    dateTime.getSecond(),
                    DatatypeConstants.FIELD_UNDEFINED,
                    dateTime.getOffset() != ZoneOffset.UTC ?
                            dateTime.getOffset().getTotalSeconds() / 60 :
                            DatatypeConstants.FIELD_UNDEFINED);
        } else {
            return null;
        }
    }

    private ZonedDateTime toZonedDateTime(XMLGregorianCalendar calendar) {
        if (calendar.getTimezone() == DatatypeConstants.FIELD_UNDEFINED) {
            calendar.setTimezone(0);
        }

        return calendar.toGregorianCalendar().toZonedDateTime();
    }

    private void setDateTime(ZonedDateTime dateTime, DatePicker datePicker, JFormattedTextField timeField) {
        if (dateTime != null) {
            Date date = Date.from(dateTime.toInstant());
            datePicker.setDate(date);
            timeField.setValue(!dateTime.toLocalTime().equals(LocalTime.MAX.withNano(0)) ? date : null);
        } else {
            datePicker.setDate(null);
            timeField.setValue(null);
        }
    }

    private enum FeatureFilterMode {
        LATEST,
        VALID,
        TERMINATED;

        @Override
        public String toString() {
            switch (this) {
                case LATEST:
                    return Language.I18N.getString("filter.label.featureVersion.latest");
                case VALID:
                    return Language.I18N.getString("filter.label.featureVersion.valid");
                case TERMINATED:
                    return Language.I18N.getString("filter.label.featureVersion.terminated");
                default:
                    return "";
            }
        }
    }

    private enum TimestampMode {
        ALL,
        AT,
        FROM;

        @Override
        public String toString() {
            switch (this) {
                case ALL:
                    return Language.I18N.getString("filter.label.featureVersion.all");
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

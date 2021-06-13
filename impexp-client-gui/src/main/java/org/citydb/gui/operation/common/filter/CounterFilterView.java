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

package org.citydb.gui.operation.common.filter;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.query.filter.counter.CounterFilter;
import org.citydb.gui.components.common.BlankNumberFormatter;
import org.citydb.gui.factory.PopupMenuDecorator;
import org.citydb.gui.util.GuiUtil;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;

public class CounterFilterView extends FilterView<CounterFilter> {
    private JPanel component;
    private JLabel countLabel;
    private JLabel startIndexLabel;
    private JFormattedTextField countText;
    private JFormattedTextField startIndexText;

    public CounterFilterView() {
        init();
    }

    private void init() {
        component = new JPanel();
        component.setLayout(new GridBagLayout());

        countLabel = new JLabel();
        startIndexLabel = new JLabel();

        BlankNumberFormatter countFormatter = new BlankNumberFormatter(new DecimalFormat("#"));
        countFormatter.setLimits(0L, Long.MAX_VALUE);
        countText = new JFormattedTextField(countFormatter);
        countText.setColumns(10);

        BlankNumberFormatter startIndexFormatter = new BlankNumberFormatter(new DecimalFormat("#"));
        startIndexFormatter.setLimits(0L, Long.MAX_VALUE);
        startIndexText = new JFormattedTextField(startIndexFormatter);
        startIndexText.setColumns(10);

        component.add(countLabel, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.NONE, 0, 0, 0, 5));
        component.add(countText, GuiUtil.setConstraints(1, 0, 1, 0, GridBagConstraints.HORIZONTAL, 0, 5, 0, 5));
        component.add(startIndexLabel, GuiUtil.setConstraints(2, 0, 0, 0, GridBagConstraints.NONE, 0, 10, 0, 5));
        component.add(startIndexText, GuiUtil.setConstraints(3, 0, 1, 0, GridBagConstraints.HORIZONTAL, 0, 5, 0, 0));

        PopupMenuDecorator.getInstance().decorate(countText, startIndexText);
    }

    @Override
    public String getLocalizedTitle() {
        return Language.I18N.getString("filter.border.counter");
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
        return new FlatSVGIcon("org/citydb/gui/filter/counter.svg");
    }

    @Override
    public void doTranslation() {
        countLabel.setText(Language.I18N.getString("filter.label.counter.count"));
        startIndexLabel.setText(Language.I18N.getString("filter.label.counter.startIndex"));
    }

    @Override
    public void setEnabled(boolean enabled) {
        countLabel.setEnabled(enabled);
        startIndexLabel.setEnabled(enabled);
        countText.setEnabled(enabled);
        startIndexText.setEnabled(enabled);
    }

    @Override
    public void loadSettings(CounterFilter counterFilter) {
        countText.setValue(counterFilter.getCount());
        startIndexText.setValue(counterFilter.getStartIndex());
    }

    @Override
    public CounterFilter toSettings() {
        CounterFilter counterFilter = new CounterFilter();
        if (countText.isEditValid() && countText.getValue() != null) {
            counterFilter.setCount(((Number) countText.getValue()).longValue());
        } else {
            counterFilter.setCount(null);
        }

        if (startIndexText.isEditValid() && startIndexText.getValue() != null) {
            counterFilter.setStartIndex(((Number) startIndexText.getValue()).longValue());
        } else {
            counterFilter.setStartIndex(null);
        }

        return counterFilter;
    }

}

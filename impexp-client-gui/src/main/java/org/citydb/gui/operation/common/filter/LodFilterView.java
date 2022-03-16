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
import org.citydb.config.project.query.filter.lod.LodFilter;
import org.citydb.config.project.query.filter.lod.LodFilterMode;
import org.citydb.config.project.query.filter.lod.LodSearchMode;
import org.citydb.gui.components.popup.PopupMenuDecorator;
import org.citydb.gui.util.GuiUtil;

import javax.swing.*;
import java.awt.*;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class LodFilterView extends FilterView<LodFilter> {
    private JPanel component;
    private JCheckBox[] lods;
    private JLabel lodModeLabel;
    private JComboBox<LodFilterMode> lodMode;
    private JLabel lodDepthLabel;
    private JSpinner lodDepth;

    public LodFilterView() {
        init();
    }

    private void init() {
        component = new JPanel();
        component.setLayout(new GridBagLayout());

        lodModeLabel = new JLabel();
        lodDepthLabel = new JLabel();
        lodDepth = new JSpinner(new SpinnerListModel(Stream.concat(Stream.of("*"),
                IntStream.rangeClosed(0, 10).mapToObj(String::valueOf)).collect(Collectors.toList())));

        lodMode = new JComboBox<>();
        for (LodFilterMode mode : LodFilterMode.values()) {
            lodMode.addItem(mode);
        }

        lods = new JCheckBox[5];
        for (int lod = 0; lod < lods.length; lod++) {
            lods[lod] = new JCheckBox("LoD" + lod);
            component.add(lods[lod], GuiUtil.setConstraints(lod, 0, 0, 0, GridBagConstraints.NONE, 0, 0, 0, 10));
        }

        component.add(lodModeLabel, GuiUtil.setConstraints(5, 0, 0, 0, GridBagConstraints.NONE, 0, 10, 0, 5));
        component.add(lodMode, GuiUtil.setConstraints(6, 0, 0.5, 1, GridBagConstraints.HORIZONTAL, 0, 5, 0, 0));
        component.add(lodDepthLabel, GuiUtil.setConstraints(7, 0, 0, 0, GridBagConstraints.NONE, 0, 20, 0, 5));
        component.add(lodDepth, GuiUtil.setConstraints(8, 0, 0.5, 0, GridBagConstraints.HORIZONTAL, 0, 5, 0, 0));

        for (JCheckBox lod : lods) {
            lod.addItemListener(e -> setEnabledLodFilterMode());
        }

        PopupMenuDecorator.getInstance().decorateCheckBoxGroup(lods);
        PopupMenuDecorator.getInstance().decorate(((JSpinner.DefaultEditor) lodDepth.getEditor()).getTextField());
    }

    @Override
    public String getLocalizedTitle() {
        return Language.I18N.getString("filter.border.lod");
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
        return new FlatSVGIcon("org/citydb/gui/filter/lod.svg");
    }

    @Override
    public void switchLocale(Locale locale) {
        lodModeLabel.setText(Language.I18N.getString("filter.label.lod.mode"));
        lodDepthLabel.setText(Language.I18N.getString("filter.label.lod.depth"));
    }

    @Override
    public void setEnabled(boolean enabled) {
        for (JCheckBox lod : lods) {
            lod.setEnabled(enabled);
        }

        if (enabled) {
            setEnabledLodFilterMode();
        } else {
            lodModeLabel.setEnabled(false);
            lodMode.setEnabled(false);
        }

        lodDepthLabel.setEnabled(enabled);
        lodDepth.setEnabled(enabled);
    }

    private void setEnabledLodFilterMode() {
        int selected = 0;
        for (JCheckBox lod : lods) {
            if (lod.isEnabled() && lod.isSelected()) {
                selected++;
            }
        }

        lodModeLabel.setEnabled(selected > 1);
        lodMode.setEnabled(selected > 1);
    }

    @Override
    public void loadSettings(LodFilter lodFilter) {
        lodMode.setSelectedItem(lodFilter.getMode());
        for (int lod = 0; lod < lods.length; lod++) {
            lods[lod].setSelected(lodFilter.isSetLod(lod));
        }

        if (lodFilter.getSearchMode() == LodSearchMode.ALL) {
            lodDepth.setValue("*");
        } else {
            int searchDepth = lodFilter.getSearchDepth();
            lodDepth.setValue(searchDepth >= 0 && searchDepth < 10 ? String.valueOf(searchDepth) : "1");
        }
    }

    @Override
    public LodFilter toSettings() {
        LodFilter lodFilter = new LodFilter();
        lodFilter.setMode(lodMode.getItemAt(lodMode.getSelectedIndex()));
        for (int lod = 0; lod < lods.length; lod++) {
            lodFilter.setLod(lod, lods[lod].isSelected());
        }

        String searchDepth = lodDepth.getValue().toString();
        if (searchDepth.equals("*")) {
            lodFilter.setSearchMode(LodSearchMode.ALL);
            lodFilter.unsetSearchDepth();
        } else {
            lodFilter.setSearchMode(LodSearchMode.DEPTH);
            try {
                lodFilter.setSearchDepth(Integer.parseInt(searchDepth));
            } catch (NumberFormatException e) {
                lodFilter.setSearchDepth(1);
            }
        }

        return lodFilter;
    }
}

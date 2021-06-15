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
import org.citydb.config.project.query.filter.selection.comparison.LikeOperator;
import org.citydb.config.project.query.filter.selection.id.ResourceIdOperator;
import org.citydb.config.project.query.simple.SimpleAttributeFilter;
import org.citydb.gui.components.popup.PopupMenuDecorator;
import org.citydb.gui.util.GuiUtil;
import org.citydb.core.util.Util;

import javax.swing.*;
import java.awt.*;

public class AttributeFilterView extends FilterView<SimpleAttributeFilter> {
    private JPanel component;
    private JLabel resourceIdLabel;
    private JTextField resourceIdText;
    private JLabel nameLabel;
    private JTextField nameText;
    private JLabel lineageLabel;
    private JTextField lineageText;
    private int row;

    private boolean useNameFilter;
    private boolean useLineageFilter;

    public AttributeFilterView() {
        init();
    }

    public AttributeFilterView withNameFilter() {
        useNameFilter = true;
        nameLabel = new JLabel();
        nameText = new JTextField();
        component.add(nameLabel, GuiUtil.setConstraints(0, ++row, 0, 0, GridBagConstraints.HORIZONTAL, 5, 0, 0, 5));
        component.add(nameText, GuiUtil.setConstraints(1, row, 1, 0, GridBagConstraints.HORIZONTAL, 5, 5, 0, 0));
        PopupMenuDecorator.getInstance().decorate(nameText);

        return this;
    }

    public AttributeFilterView withLineageFilter() {
        useLineageFilter = true;
        lineageLabel = new JLabel();
        lineageText = new JTextField();
        component.add(lineageLabel, GuiUtil.setConstraints(0, ++row, 0, 0, GridBagConstraints.HORIZONTAL, 5, 0, 0, 5));
        component.add(lineageText, GuiUtil.setConstraints(1, row, 1, 0, GridBagConstraints.HORIZONTAL, 5, 5, 0, 0));
        PopupMenuDecorator.getInstance().decorate(lineageText);

        return this;
    }

    private void init() {
        component = new JPanel();
        component.setLayout(new GridBagLayout());

        // resource id filter
        resourceIdLabel = new JLabel();
        resourceIdText = new JTextField();
        component.add(resourceIdLabel, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 5));
        component.add(resourceIdText, GuiUtil.setConstraints(1, 0, 1, 0, GridBagConstraints.HORIZONTAL, 0, 5, 0, 0));
        PopupMenuDecorator.getInstance().decorate(resourceIdText);
    }

    @Override
    public String getLocalizedTitle() {
        return Language.I18N.getString("filter.border.attributes");
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
        return new FlatSVGIcon("org/citydb/gui/filter/attribute.svg");
    }

    @Override
    public void doTranslation() {
        resourceIdLabel.setText(Language.I18N.getString("filter.label.id"));

        if (useNameFilter) {
            nameLabel.setText(Language.I18N.getString("filter.label.name"));
        }

        if (useLineageFilter) {
            lineageLabel.setText(Language.I18N.getString("filter.label.lineage"));
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        resourceIdLabel.setEnabled(enabled);
        resourceIdText.setEnabled(enabled);

        if (useNameFilter) {
            nameLabel.setEnabled(enabled);
            nameText.setEnabled(enabled);
        }

        if (useLineageFilter) {
            lineageLabel.setEnabled(enabled);
            lineageText.setEnabled(enabled);
        }
    }

    public void loadSettings(ResourceIdOperator resourceIdFilter, LikeOperator nameFilter, LikeOperator lineageFilter) {
        resourceIdText.setText(String.join(",", resourceIdFilter.getResourceIds()));

        if (useNameFilter) {
            nameText.setText(nameFilter.getLiteral());
        }

        if (useLineageFilter) {
            lineageText.setText(lineageFilter.getLiteral());
        }
    }

    @Override
    public void loadSettings(SimpleAttributeFilter attributeFilter) {
        loadSettings(attributeFilter.getResourceIdFilter(),
                attributeFilter.getNameFilter(),
                attributeFilter.getLineageFilter());
    }

    @Override
    public SimpleAttributeFilter toSettings() {
        SimpleAttributeFilter attributeFilter = new SimpleAttributeFilter();

        ResourceIdOperator resourceIdFilter = attributeFilter.getResourceIdFilter();
        if (!resourceIdText.getText().trim().isEmpty()) {
            String trimmed = resourceIdText.getText().replaceAll("\\s+", "");
            resourceIdFilter.setResourceIds(Util.string2string(trimmed, ","));
        }

        if (useNameFilter) {
            LikeOperator nameFilter = attributeFilter.getNameFilter();
            if (!nameText.getText().trim().isEmpty()) {
                nameFilter.setLiteral(nameText.getText().trim());
            }
        }

        if (useLineageFilter) {
            LikeOperator lineageFilter = attributeFilter.getLineageFilter();
            if (!lineageText.getText().trim().isEmpty()) {
                lineageFilter.setLiteral(lineageText.getText().trim());
            }
        }

        return attributeFilter;
    }
}

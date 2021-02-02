/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2020
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

package org.citydb.gui.modules.common.filter;

import org.citydb.config.i18n.Language;
import org.citydb.config.project.exporter.SimpleQuery;
import org.citydb.config.project.query.filter.selection.comparison.LikeOperator;
import org.citydb.config.project.query.filter.selection.id.ResourceIdOperator;
import org.citydb.gui.factory.PopupMenuDecorator;
import org.citydb.gui.util.GuiUtil;
import org.citydb.util.Util;

import javax.swing.*;
import java.awt.*;
import java.util.function.Supplier;

public class AttributeFilterView extends FilterView {
    private JPanel component;

    private JLabel resourceIdLabel;
    private JTextField resourceIdText;
    private JLabel nameLabel;
    private JTextField nameText;
    private JLabel lineageLabel;
    private JTextField lineageText;

    public AttributeFilterView(Supplier<SimpleQuery> simpleQuerySupplier) {
        super(simpleQuerySupplier);
        init();
    }

    private void init() {
        component = new JPanel();
        component.setLayout(new GridBagLayout());

        resourceIdLabel = new JLabel();
        resourceIdText = new JTextField();
        nameLabel = new JLabel();
        nameText = new JTextField();
        lineageLabel = new JLabel();
        lineageText = new JTextField();

        // resource id filter
        component.add(resourceIdLabel, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 0, 5, 5));
        component.add(resourceIdText, GuiUtil.setConstraints(1, 0, 1, 0, GridBagConstraints.HORIZONTAL, 0, 5, 5, 0));

        // name filter
        component.add(nameLabel, GuiUtil.setConstraints(0, 1, 0, 0, GridBagConstraints.HORIZONTAL, 0, 0, 5, 5));
        component.add(nameText, GuiUtil.setConstraints(1, 1, 1, 0, GridBagConstraints.HORIZONTAL, 0, 5, 5, 0));

        // citydb:lineage filter
        component.add(lineageLabel, GuiUtil.setConstraints(0, 2, 0, 0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 5));
        component.add(lineageText, GuiUtil.setConstraints(1, 2, 1, 0, GridBagConstraints.HORIZONTAL, 0, 5, 0, 0));

        PopupMenuDecorator.getInstance().decorate(nameText, resourceIdText, lineageText);
    }


    @Override
    public void doTranslation() {
        resourceIdLabel.setText(Language.I18N.getString("filter.label.id"));
        nameLabel.setText(Language.I18N.getString("filter.label.name"));
        lineageLabel.setText(Language.I18N.getString("filter.label.lineage"));
    }

    @Override
    public void setEnabled(boolean enable) {
        resourceIdLabel.setEnabled(enable);
        resourceIdText.setEnabled(enable);
        nameLabel.setEnabled(enable);
        nameText.setEnabled(enable);
        lineageLabel.setEnabled(enable);
        lineageText.setEnabled(enable);
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
        return null;
    }

    @Override
    public void loadSettings() {
        SimpleQuery query = simpleQuerySupplier.get();

        // resource id filter
        ResourceIdOperator gmlIdFilter = query.getAttributeFilter().getResourceIdFilter();
        resourceIdText.setText(String.join(",", gmlIdFilter.getResourceIds()));

        // name
        LikeOperator gmlNameFilter = query.getAttributeFilter().getNameFilter();
        nameText.setText(gmlNameFilter.getLiteral());

        // citydb:lineage
        LikeOperator lineageFilter = query.getAttributeFilter().getLineageFilter();
        lineageText.setText(lineageFilter.getLiteral());
    }

    @Override
    public void setSettings() {
        SimpleQuery query = simpleQuerySupplier.get();

        // resource id filter
        ResourceIdOperator gmlIdFilter = query.getAttributeFilter().getResourceIdFilter();
        gmlIdFilter.reset();
        if (!resourceIdText.getText().trim().isEmpty()) {
            String trimmed = resourceIdText.getText().replaceAll("\\s+", "");
            gmlIdFilter.setResourceIds(Util.string2string(trimmed, ","));
        }

        // name
        LikeOperator gmlNameFilter = query.getAttributeFilter().getNameFilter();
        gmlNameFilter.reset();
        if (!nameText.getText().trim().isEmpty())
            gmlNameFilter.setLiteral(nameText.getText().trim());

        // citydb:lineage
        LikeOperator lineageFilter = query.getAttributeFilter().getLineageFilter();
        lineageFilter.reset();
        if (!lineageText.getText().trim().isEmpty())
            lineageFilter.setLiteral(lineageText.getText().trim());
    }
}

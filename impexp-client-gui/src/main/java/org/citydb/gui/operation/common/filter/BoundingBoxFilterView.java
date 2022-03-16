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
import org.citydb.config.geometry.BoundingBox;
import org.citydb.config.i18n.Language;
import org.citydb.gui.components.bbox.BoundingBoxPanel;
import org.citydb.gui.plugin.view.ViewController;

import javax.swing.*;
import java.util.Locale;

public class BoundingBoxFilterView extends FilterView<BoundingBox> {
    private final ViewController viewController;
    private BoundingBoxPanel component;

    public BoundingBoxFilterView(ViewController viewController) {
        this.viewController = viewController;
        init();
    }

    private void init() {
        component = new BoundingBoxPanel(viewController);
    }

    @Override
    public String getLocalizedTitle() {
        return Language.I18N.getString("filter.border.boundingBox");
    }

    @Override
    public BoundingBoxPanel getViewComponent() {
        return component;
    }

    @Override
    public String getToolTip() {
        return null;
    }

    @Override
    public Icon getIcon() {
        return new FlatSVGIcon("org/citydb/gui/filter/bbox.svg");
    }

    @Override
    public void switchLocale(Locale locale) {
        // nothing to do
    }

    @Override
    public void setEnabled(boolean enabled) {
        component.setEnabled(enabled);
    }

    @Override
    public void loadSettings(BoundingBox bbox) {
        component.setBoundingBox(bbox);
    }

    @Override
    public BoundingBox toSettings() {
        return component.getBoundingBox();
    }

}

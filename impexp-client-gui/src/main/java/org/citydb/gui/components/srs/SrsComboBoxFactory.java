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
package org.citydb.gui.components.srs;

import org.citydb.config.Config;
import org.citydb.config.project.database.DatabaseSrs;
import org.citydb.core.registry.ObjectRegistry;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

public class SrsComboBoxFactory {
    private static SrsComboBoxFactory instance = null;
    private final Set<SrsComboBox> srsComboBoxes = Collections.newSetFromMap(new WeakHashMap<>());
    private final Config config;

    private SrsComboBoxFactory() {
        // just to thwart instantiation
        config = ObjectRegistry.getInstance().getConfig();
    }

    public static synchronized SrsComboBoxFactory getInstance() {
        if (instance == null) {
            instance = new SrsComboBoxFactory();
            instance.resetAll(true);
        }

        return instance;
    }

    public SrsComboBox createSrsComboBox(boolean onlyShowSupported) {
        SrsComboBox srsComboBox = new SrsComboBox(onlyShowSupported, config);
        srsComboBox.init();
        srsComboBoxes.add(srsComboBox);

        return srsComboBox;
    }

    public SrsComboBox createSrsComboBox() {
        return createSrsComboBox(true);
    }

    public void updateAll(boolean sort) {
        processSrsComboBoxes(sort, true);
    }

    public void resetAll(boolean sort) {
        // by default, any reference system is not supported. In GUI mode we can
        // override this because the SRS combo boxes will take care.
        for (DatabaseSrs srs : config.getDatabaseConfig().getReferenceSystems()) {
            srs.setSupported(true);
        }

        processSrsComboBoxes(sort, false);
    }

    private void processSrsComboBoxes(boolean sort, boolean update) {
        if (sort) {
            Collections.sort(config.getDatabaseConfig().getReferenceSystems());
        }

        for (SrsComboBox srsComboBox : srsComboBoxes) {
            if (update) {
                srsComboBox.updateContent();
            } else {
                srsComboBox.reset();
            }

            srsComboBox.repaint();
        }
    }
}

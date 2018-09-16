/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2018
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
package org.citydb.modules.database.gui.util;

import javax.swing.*;

public class ADEInfoRow {
    public static final ADEInfoRow NO_ADES_ENTRY = new ADEInfoRow(null, "n/a", "n/a", false, false);
    private static final ImageIcon isSupported;
    private static final ImageIcon isNotSupported;

    static {
        isSupported = new ImageIcon(ADEInfoRow.class.getResource("/org/citydb/gui/images/common/done.png"));
        isNotSupported = new ImageIcon(ADEInfoRow.class.getResource("/org/citydb/gui/images/common/clear.png"));
    }

    private final String id;
    private String name;
    private String version;
    private boolean databaseSupport;
    private boolean impexpSupport;

    public ADEInfoRow(String id, String name, String version, boolean hasDBSupport, boolean hasImpexpSupport) {
        this.id = id;
        this.name = name;
        this.version = version;
        this.databaseSupport = hasDBSupport;
        this.impexpSupport = hasImpexpSupport;
    }

    public Object getValueAt(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return name;
            case 1:
                return version;
            case 2:
                return databaseSupport ? isSupported : isNotSupported;
            case 3:
                return impexpSupport ? isSupported : isNotSupported;
            default:
                return null;
        }
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean hasDatabaseSupport() {
        return databaseSupport;
    }

    public void setDatabaseSupport(boolean databaseSupport) {
        this.databaseSupport = databaseSupport;
    }

    public boolean hasImpexpSupport() {
        return impexpSupport;
    }

    public void setImpexpSupport(boolean impexpSupport) {
        this.impexpSupport = impexpSupport;
    }

    public static int getDefaultRowHeight() {
        return Math.max(UIManager.getFont("Table.font").getSize(), isSupported.getIconHeight()) + 2;
    }

}

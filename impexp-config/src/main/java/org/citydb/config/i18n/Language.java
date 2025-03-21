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
package org.citydb.config.i18n;

import java.util.Locale;
import java.util.ResourceBundle;

public class Language {
    // language pack
    public static ResourceBundle I18N;

    static {
        Locale locale = new Locale(System.getProperty("user.language"));
        if (!existsLanguagePack(locale))
            locale = Locale.ENGLISH;

        I18N = ResourceBundle.getBundle("org.citydb.config.i18n.language", locale);
    }

    public static boolean existsLanguagePack(Locale locale) {
        return Language.class.getResource("/org/citydb/config/i18n/language_" + locale.getLanguage() + ".properties") != null;
    }

}

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
package org.citydb.core.query.filter.type;

import org.citydb.core.ade.ADEExtension;
import org.citydb.core.ade.ADEExtensionManager;
import org.citydb.core.database.schema.mapping.FeatureType;
import org.citydb.core.query.filter.FilterException;
import org.citygml4j.model.module.citygml.CityGMLVersion;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FeatureTypeFilter {
    private final Set<FeatureType> featureTypes;
    private final boolean useStrictMode;
    private final ADEExtensionManager adeManager;

    public FeatureTypeFilter(boolean useStrictMode) {
        featureTypes = new HashSet<>();
        this.useStrictMode = useStrictMode;

        adeManager = ADEExtensionManager.getInstance();
    }

    public FeatureTypeFilter(FeatureType featureType, boolean useStrictMode) throws FilterException {
        this(useStrictMode);
        addFeatureType(featureType);
    }

    public FeatureTypeFilter(Set<FeatureType> featureTypes, boolean useStrictMode) throws FilterException {
        this(useStrictMode);
        for (FeatureType featureType : featureTypes)
            addFeatureType(featureType);
    }

    public FeatureTypeFilter() {
        this(true);
    }

    public FeatureTypeFilter(FeatureType featureType) throws FilterException {
        this(featureType, true);
    }

    public FeatureTypeFilter(Set<FeatureType> featureTypes) throws FilterException {
        this(featureTypes, true);
    }

    public int size() {
        return featureTypes.size();
    }

    public boolean isEmpty() {
        return featureTypes.isEmpty();
    }

    public void clear() {
        featureTypes.clear();
    }

    public boolean isAllowedFeatureType(FeatureType featureType) {
        if (useStrictMode && (!featureType.isTopLevel() || !featureType.isQueryable()))
            return false;

        ADEExtension extension = adeManager.getExtensionByObjectClassId(featureType.getObjectClassId());
        if (extension != null && !extension.isEnabled())
            return false;

        return true;
    }

    public List<FeatureType> getFeatureTypes() {
        return new ArrayList<FeatureType>(featureTypes);
    }

    public List<FeatureType> getFeatureTypes(CityGMLVersion version) {
        ArrayList<FeatureType> result = new ArrayList<>();
        for (FeatureType featureType : featureTypes) {
            if (featureType.isAvailableForCityGML(version))
                result.add(featureType);
        }

        return result;
    }

    public void addFeatureType(FeatureType featureType) throws FilterException {
        if (featureType.isAbstract()) {
            for (FeatureType subType : featureType.listSubTypes(true)) {
                if (!isAllowedFeatureType(subType))
                    continue;

                featureTypes.add(subType);
            }
        } else {
            if (!isAllowedFeatureType(featureType))
                throw new FilterException("The feature type must be both top-level and queryable.");

            featureTypes.add(featureType);
        }
    }

    public void removeFeatureType(FeatureType featureType) {
        featureTypes.remove(featureType);
    }

    public boolean containsFeatureType(FeatureType featureType) {
        return featureTypes.contains(featureType);
    }
}

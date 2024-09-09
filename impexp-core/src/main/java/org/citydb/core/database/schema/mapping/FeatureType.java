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
package org.citydb.core.database.schema.mapping;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@XmlType(name = "featureType", propOrder = {
        "extension",
        "properties",
        "adeHook"
})
public class FeatureType extends AbstractObjectType<FeatureType> {
    @XmlAttribute
    protected Boolean topLevel;
    protected FeatureTypeExtension extension;
    protected ADEHook adeHook;

    @XmlTransient
    private boolean[] lods;

    protected FeatureType() {
    }

    public FeatureType(String id, String path, String table, int objectClassId, AppSchema schema, SchemaMapping schemaMapping) {
        super(id, path, table, objectClassId, schema, schemaMapping);
    }

    @Override
    public AbstractExtension<FeatureType> getExtension() {
        return extension;
    }

    @Override
    public boolean isSetExtension() {
        return extension != null;
    }

    @Override
    public void setExtension(AbstractExtension<FeatureType> extension) {
        this.extension = (FeatureTypeExtension) extension;
    }

    @Override
    public List<FeatureType> listSubTypes(boolean skipAbstractTypes) {
        return listSubTypes(schemaMapping.featureTypes, skipAbstractTypes);
    }

    public boolean isTopLevel() {
        return topLevel != null ? topLevel.booleanValue() : false;
    }

    public boolean isSetTopLevel() {
        return topLevel != null;
    }

    public void setTopLevel(boolean topLevel) {
        this.topLevel = topLevel;
    }

    public ADEHook getADEHook() {
        return adeHook;
    }

    public boolean isSetADEHook() {
        return adeHook != null;
    }

    public void setADEHook(ADEHook adeHook) {
        this.adeHook = adeHook;
    }

    public boolean hasLodProperties() {
        if (lods == null)
            calcLodProperties();

        for (int lod = 0; lod < 5; lod++) {
            if (lods[lod])
                return true;
        }

        return false;
    }

    public boolean isAvailableForLod(int lod) {
        if (lod < 0 || lod > 4)
            return false;

        if (lods == null)
            calcLodProperties();

        return lods[lod];
    }

    private void calcLodProperties() {
        lods = new boolean[5];
        Matcher matcher = Pattern.compile("(?i)^lod([0-4]).*", Pattern.UNICODE_CHARACTER_CLASS).matcher("");

        for (AbstractProperty property : listProperties(false, true)) {
            if (property.getElementType() == PathElementType.SIMPLE_ATTRIBUTE
                    && ((SimpleAttribute) property).getType() == SimpleType.INTEGER
                    && property.getPath().equalsIgnoreCase("lod")) {
                for (int i = 0; i < lods.length; i++)
                    lods[i] = true;
                break;
            }

            int lod = -1;

            if (property.getElementType() == PathElementType.GEOMETRY_PROPERTY) {
                GeometryProperty geometryProperty = (GeometryProperty) property;
                lod = geometryProperty.getLod();
                if (lod < 0) {
                    matcher.reset(geometryProperty.getPath());
                    if (!matcher.matches())
                        continue;

                    lod = Integer.valueOf(matcher.group(1));
                }
            } else if (property.getElementType() == PathElementType.IMPLICIT_GEOMETRY_PROPERTY)
                lod = ((ImplicitGeometryProperty) property).getLod();

            if (lod >= 0 && lod < 5)
                lods[lod] = true;
        }
    }

    @Override
    public PathElementType getElementType() {
        return PathElementType.FEATURE_TYPE;
    }

    @Override
    protected void validate(SchemaMapping schemaMapping, Object parent) throws SchemaMappingException {
        super.validate(schemaMapping, parent);
        schema.addFeatureType(this);

        if (isTopLevel() && isAbstract())
            throw new SchemaMappingException("The attribute 'topLevel' may only be used with non-abstract feature types.");

        if (isSetExtension())
            extension.validate(schemaMapping, this);
    }

}

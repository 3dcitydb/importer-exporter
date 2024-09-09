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

package org.citydb.core.operation.exporter.util;

import org.citydb.core.operation.exporter.CityGMLExportException;
import org.citydb.core.util.CoreConstants;
import org.citygml4j.model.citygml.appearance.*;
import org.citygml4j.model.gml.base.AbstractGML;
import org.citygml4j.model.gml.base.AssociationByRepOrRef;
import org.citygml4j.model.gml.feature.AbstractFeature;
import org.citygml4j.model.gml.feature.FeatureProperty;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.citygml4j.model.gml.geometry.GeometryProperty;
import org.citygml4j.util.gmlid.DefaultGMLIdManager;
import org.citygml4j.util.walker.GMLWalker;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.UUID;

public class IdReplacer extends GMLWalker {
    private final MessageDigest md5;
    private String prefix = DefaultGMLIdManager.getInstance().getDefaultPrefix();

    public IdReplacer() throws CityGMLExportException {
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new CityGMLExportException("Failed to create ID replacer.", e);
        }
    }

    public IdReplacer withPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    @Override
    public void visit(AbstractGML object) {
        if (object.isSetId()) {
            object.setLocalProperty(CoreConstants.OBJECT_ORIGINAL_GMLID, object.getId());
            object.setId(replaceId(object.getId()));
        }

        super.visit(object);
    }

    @Override
    public <T extends AbstractFeature> void visit(FeatureProperty<T> property) {
        process(property);
        super.visit(property);
    }

    @Override
    public <T extends AbstractGeometry> void visit(GeometryProperty<T> property) {
        process(property);
        super.visit(property);
    }

    @Override
    public <T extends AbstractGML> void visit(AssociationByRepOrRef<T> property) {
        process(property);
        super.visit(property);
    }

    @Override
    public void visit(ParameterizedTexture texture) {
        if (texture.isSetTarget()) {
            for (TextureAssociation association : texture.getTarget()) {
                visit(association);
            }
        }

        super.visit(texture);
    }

    @Override
    public void visit(TexCoordList texCoordList) {
        if (texCoordList.isSetTextureCoordinates()) {
            for (TextureCoordinates coordinates : texCoordList.getTextureCoordinates()) {
                if (coordinates.isSetRing()) {
                    coordinates.setRing(process(coordinates.getRing()));
                }
            }
        }

        super.visit(texCoordList);
    }

    @Override
    public void visit(GeoreferencedTexture texture) {
        if (texture.isSetTarget()) {
            process(texture.getTarget());
        }

        super.visit(texture);
    }

    @Override
    public void visit(X3DMaterial material) {
        if (material.isSetTarget()) {
            process(material.getTarget());
        }

        super.visit(material);
    }

    private void visit(TextureAssociation association) {
        process(association);
        if (association.isSetUri()) {
            association.setUri(process(association.getUri()));
        }
    }

    private void process(AssociationByRepOrRef<? extends AbstractGML> property) {
        if (property.isSetHref()) {
            property.setHref(process(property.getHref()));
        }
    }

    private void process(List<String> references) {
        references.replaceAll(this::process);
    }

    private String process(String reference) {
        String prefix = "";
        String id = reference;

        int index = reference.lastIndexOf("#");
        if (index != -1) {
            prefix = reference.substring(0, index + 1);
            id = reference.substring(index + 1);
        }

        return !id.isEmpty() ? prefix + replaceId(id) : reference;
    }

    public String replaceId(String id) {
        return prefix + UUID.nameUUIDFromBytes(md5.digest(id.getBytes()));
    }
}

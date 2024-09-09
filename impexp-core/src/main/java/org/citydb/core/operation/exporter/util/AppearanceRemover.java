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

import org.citygml4j.model.citygml.appearance.*;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.common.base.ModelObject;
import org.citygml4j.model.gml.base.AbstractGML;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurface;
import org.citygml4j.model.gml.geometry.primitives.AbstractSurface;
import org.citygml4j.util.walker.GMLWalker;

import java.util.*;

public class AppearanceRemover {

    public void cleanupAppearances(AbstractGML object) {
        List<Appearance> appearances = new ArrayList<>();
        object.accept(new GMLWalker() {
            @Override
            public void visit(Appearance appearance) {
                appearances.add(appearance);
            }
        });

        Set<String> targets = getTargets(object);
        Set<String> removed = unsetSurfaceData(appearances, targets);
        unsetReferences(appearances, removed);
        unsetAppearance(object);
    }

    public void cleanupAppearances(Collection<Appearance> appearances, Set<String> targets) {
        Set<String> removed = unsetSurfaceData(appearances, targets);
        unsetReferences(appearances, removed);
        appearances.removeIf(appearance -> !appearance.isSetSurfaceDataMember());
    }

    private Set<String> getTargets(AbstractGML object) {
        Set<String> targets = new HashSet<>();

        object.accept(new GMLWalker() {
            @Override
            public void visit(AbstractSurface surface) {
                addTarget(surface);
                super.visit(surface);
            }

            @Override
            public void visit(MultiSurface surface) {
                addTarget(surface);
                super.visit(surface);
            }

            private void addTarget(AbstractGeometry geometry) {
                if (geometry.isSetId())
                    targets.add("#" + geometry.getId());
            }
        });

        return targets;
    }

    private Set<String> unsetSurfaceData(Collection<Appearance> appearances, Set<String> targets) {
        Set<String> removed = new HashSet<>();

        for (Appearance appearance : appearances) {
            Iterator<SurfaceDataProperty> iter = appearance.getSurfaceDataMember().iterator();
            while (iter.hasNext()) {
                AbstractSurfaceData surfaceData = iter.next().getSurfaceData();
                if (surfaceData == null)
                    continue;

                boolean remove = false;
                if (surfaceData instanceof ParameterizedTexture) {
                    ParameterizedTexture texture = (ParameterizedTexture) surfaceData;
                    texture.getTarget().removeIf(target -> !targets.contains(target.getUri()));
                    remove = !texture.isSetTarget();
                } else if (surfaceData instanceof X3DMaterial) {
                    X3DMaterial material = (X3DMaterial) surfaceData;
                    material.getTarget().removeIf(target -> !targets.contains(target));
                    remove = !material.isSetTarget();
                } else if (surfaceData instanceof GeoreferencedTexture) {
                    GeoreferencedTexture texture = (GeoreferencedTexture) surfaceData;
                    texture.getTarget().removeIf(target -> !targets.contains(target));
                    remove = !texture.isSetTarget();
                }

                if (remove) {
                    iter.remove();
                    if (surfaceData.isSetId())
                        removed.add("#" + surfaceData.getId());
                }
            }
        }

        return removed;
    }

    private void unsetReferences(Collection<Appearance> appearances, Set<String> removed) {
        appearances.forEach(appearance -> appearance.getSurfaceDataMember()
                .removeIf(member -> member.isSetHref() && removed.contains(member.getHref())));
    }

    private void unsetAppearance(AbstractGML object) {
        object.accept(new GMLWalker() {
            @Override
            public void visit(Appearance appearance) {
                if (!appearance.isSetSurfaceDataMember()) {
                    ModelObject property = appearance.getParent();
                    if (property instanceof AppearanceProperty) {
                        ModelObject parent = ((AppearanceProperty) property).getParent();
                        if (parent instanceof AbstractCityObject)
                            ((AbstractCityObject) parent).unsetAppearance((AppearanceProperty) property);
                    }
                }
            }
        });
    }
}

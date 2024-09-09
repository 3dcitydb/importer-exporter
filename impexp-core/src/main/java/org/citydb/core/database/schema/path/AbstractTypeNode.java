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
package org.citydb.core.database.schema.path;

import org.citydb.core.database.schema.mapping.*;
import org.citydb.core.database.schema.path.predicate.comparison.ComparisonPredicateName;
import org.citydb.core.database.schema.path.predicate.comparison.EqualToPredicate;
import org.citydb.core.database.schema.path.predicate.logical.BinaryLogicalPredicate;

public abstract class AbstractTypeNode<T extends AbstractType<T>> extends AbstractNode<T> {

    AbstractTypeNode(T objectType) {
        super(objectType);
    }

    AbstractTypeNode(AbstractTypeNode<T> other) {
        super(other);
    }

    @Override
    protected boolean isValidChild(AbstractPathElement candidate) {
        if (candidate instanceof AbstractProperty)
            return pathElement.listProperties(false, true).contains(candidate);

        return false;
    }

    @Override
    protected boolean isValidPredicate(AbstractNodePredicate candidate) {
        if (candidate.getPredicateName() == ComparisonPredicateName.EQUAL_TO) {
            EqualToPredicate predicate = (EqualToPredicate) candidate;
            boolean found = false;

            for (AbstractProperty property : pathElement.listProperties(false, true)) {
                if (predicate.getLeftOperand() == property) {
                    found = true;
                    break;
                }

                if (predicate.getLeftOperand().getElementType() == PathElementType.SIMPLE_ATTRIBUTE &&
                        property.getElementType() == PathElementType.COMPLEX_ATTRIBUTE &&
                        ((SimpleAttribute) predicate.getLeftOperand()).hasParentAttributeType() &&
                        ((SimpleAttribute) predicate.getLeftOperand()).getParentAttributeType() == ((ComplexAttribute) property).getType()) {
                    found = true;
                    break;
                }
            }

            if (found)
                return predicate.getRightOperand().evaluatesToSchemaType(predicate.getLeftOperand().getType());
        } else {
            BinaryLogicalPredicate predicate = (BinaryLogicalPredicate) candidate;
            if (isValidPredicate(predicate.getLeftOperand()))
                return isValidPredicate(predicate.getRightOperand());
        }

        return false;
    }

}

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
package org.citydb.core.database.schema.path.predicate.logical;

import org.citydb.core.database.schema.path.AbstractNodePredicate;
import org.citydb.core.database.schema.path.predicate.comparison.EqualToPredicate;

public class BinaryLogicalPredicate extends AbstractNodePredicate {
    private final EqualToPredicate leftOperand;
    private final AbstractNodePredicate rightOperand;
    private final LogicalPredicateName name;

    public BinaryLogicalPredicate(EqualToPredicate leftOperand, LogicalPredicateName name, AbstractNodePredicate rightOperand) {
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
        this.name = name;
    }

    public EqualToPredicate getLeftOperand() {
        return leftOperand;
    }

    public AbstractNodePredicate getRightOperand() {
        return rightOperand;
    }

    public LogicalPredicateName getName() {
        return name;
    }

    @Override
    public boolean isEqualTo(AbstractNodePredicate other) {
        if (other == this)
            return true;

        if (!(other instanceof BinaryLogicalPredicate))
            return false;

        BinaryLogicalPredicate predicate = (BinaryLogicalPredicate) other;
        if (name != predicate.name)
            return false;

        return ((leftOperand.isEqualTo(predicate.leftOperand) && rightOperand.isEqualTo(predicate.rightOperand))
                || (leftOperand.isEqualTo(predicate.rightOperand) && rightOperand.isEqualTo(predicate.leftOperand)));
    }

    @Override
    public LogicalPredicateName getPredicateName() {
        return name;
    }

    @Override
    public String toString(boolean removeAttributePrefixes) {
        return new StringBuilder()
                .append(leftOperand.toString(removeAttributePrefixes))
                .append(" ").append(name.getSymbol()).append(" ")
                .append(rightOperand.toString(removeAttributePrefixes)).toString();
    }

}

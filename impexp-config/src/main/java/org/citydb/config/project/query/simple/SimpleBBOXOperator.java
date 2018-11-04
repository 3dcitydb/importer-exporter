package org.citydb.config.project.query.simple;

import org.citydb.config.project.query.filter.selection.spatial.BBOXOperator;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="SimpleBBOXOperatorType")
public class SimpleBBOXOperator extends BBOXOperator {
    @XmlAttribute(required = true)
    private SimpleBBOXMode bboxMode = SimpleBBOXMode.BBOX;

    public SimpleBBOXMode getBboxMode() {
        return bboxMode;
    }

    public void setBboxMode(SimpleBBOXMode bboxMode) {
        this.bboxMode = bboxMode;
    }
}

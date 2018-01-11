package org.citydb.config.project.query.filter.selection;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name="filter")
@XmlType(name="SelectionFilterType", propOrder={
		"predicate"
})
public class SelectionFilter {
	@XmlElementRef(required = true)
	private AbstractPredicate predicate;

	public AbstractPredicate getPredicate() {
		return predicate;
	}

	public boolean isSetPredicate() {
		return predicate != null;
	}
	
	public void setPredicate(AbstractPredicate predicate) {
		this.predicate = predicate;
	}
	
}

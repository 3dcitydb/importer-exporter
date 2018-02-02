package org.citydb.config.project.query.filter.selection.id;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.citydb.config.project.query.filter.selection.AbstractPredicate;
import org.citydb.config.project.query.filter.selection.PredicateName;

@XmlRootElement(name="resourceIds")
@XmlType(name="ResourceIdType", propOrder={
		"ids"
})
public class ResourceIdOperator extends AbstractPredicate {
	@XmlElement(name="id")
	private List<String> ids;
	
	public ResourceIdOperator() {
		ids = new ArrayList<>();
	}
	
	public boolean isSetResourceIds() {
		return !ids.isEmpty();
	}

	public List<String> getResourceIds() {
		return ids;
	}

	public void setResourceIds(List<String> ids) {
		if (ids != null && !ids.isEmpty())
			this.ids = ids;
	}
	
	@Override
	public void reset() {
		ids.clear();
	}
	
	@Override
	public PredicateName getPredicateName() {
		return PredicateName.ID_OPERATOR;
	}
}

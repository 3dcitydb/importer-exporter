package org.citydb.config.project.query.filter.projection;

import java.util.LinkedHashSet;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "PropertyProjectionFilterType")
public class ProjectionFilter {
	@XmlElement(name="context")
	private LinkedHashSet<ProjectionContext> projectionContexts;
	
	public ProjectionFilter() {
		projectionContexts = new LinkedHashSet<>();
	}
	
	public LinkedHashSet<ProjectionContext> getProjectionContexts() {
		return projectionContexts;
	}

	public void setProjectionContexts(LinkedHashSet<ProjectionContext> projectionContexts) {
		this.projectionContexts = projectionContexts;
	}
	
}

package org.citydb.config.project.query.filter.selection;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.citydb.config.project.query.filter.selection.comparison.LikeOperator;
import org.citydb.config.project.query.filter.selection.id.ResourceIdOperator;
import org.citydb.config.project.query.filter.selection.spatial.BBOXOperator;
import org.citydb.config.project.query.filter.selection.spatial.SimpleBBOXMode;

@XmlType(name="SimpleSelectionType", propOrder={
		"gmlIdFilter",
		"gmlNameFilter",
		"bboxFilter"
})
public class SimpleSelectionFilter {
	@XmlAttribute(required = true)
	private SimpleBBOXMode bboxMode = SimpleBBOXMode.BBOX;
	@XmlElement(name = "gmlIds", required = true)
	private ResourceIdOperator gmlIdFilter;
	@XmlElement(name = "gmlName", required = true)
	private LikeOperator gmlNameFilter;
	@XmlElement(name = "bbox", required = true)
	private BBOXOperator bboxFilter;

	public SimpleSelectionFilter() {
		gmlIdFilter = new ResourceIdOperator();
		gmlNameFilter = new LikeOperator();
		bboxFilter = new BBOXOperator();
	}
	
	public ResourceIdOperator getGmlIdFilter() {
		return gmlIdFilter;
	}
	
	public boolean isSetGmlIdFilter() {
		return gmlIdFilter != null;
	}

	public void setGmlIdFilter(ResourceIdOperator gmlIdFilter) {
		this.gmlIdFilter = gmlIdFilter;
	}

	public LikeOperator getGmlNameFilter() {
		return gmlNameFilter;
	}
	
	public boolean isSetGmlNameFilter() {
		return gmlNameFilter != null;
	}

	public void setGmlNameFilter(LikeOperator gmlNameFilter) {
		this.gmlNameFilter = gmlNameFilter;
	}

	public BBOXOperator getBboxFilter() {
		return bboxFilter;
	}
	
	public boolean isSetBboxFilter() {
		return bboxFilter != null;
	}

	public void setBboxFilter(BBOXOperator bboxFilter) {
		this.bboxFilter = bboxFilter;
	}

	public SimpleBBOXMode getBboxMode() {
		return bboxMode;
	}

	public void setBboxMode(SimpleBBOXMode bboxMode) {
		this.bboxMode = bboxMode;
	}
	
}

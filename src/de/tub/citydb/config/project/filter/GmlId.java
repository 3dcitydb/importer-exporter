package de.tub.citydb.config.project.filter;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="GmlIdType", propOrder={
		"gmlIds"
		})
public class GmlId {
	@XmlElement(name="gmlId")
	private List<String> gmlIds;
	
	public GmlId() {
		gmlIds = new ArrayList<String>();
	}

	public List<String> getGmlIds() {
		return gmlIds;
	}

	public void setGmlIds(List<String> gmlIds) {
		if (gmlIds != null)
			this.gmlIds = gmlIds;
	}

	public void addGmlId(String gmlId) {
		gmlIds.add(gmlId);
	}
	
}

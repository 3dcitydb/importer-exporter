package de.tub.citydb.config.project.database;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement()
public class ReferenceSystems {
	@XmlElement(name="referenceSystem")
	private List<ReferenceSystem> items;
	
	public ReferenceSystems() {
		items = new ArrayList<ReferenceSystem>();
	}

	public List<ReferenceSystem> getItems() {
		return items;
	}

	public void setItems(List<ReferenceSystem> items) {
		this.items = items;
	}
	
	public boolean addItem(ReferenceSystem item) {
		return items.add(item);
	}
	 
}

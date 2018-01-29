package org.citydb.database.schema.mapping;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "treeHierarchy")
public class TreeHierarchy {
	@XmlAttribute(required = true)
	protected String rootColumn;
	
	protected TreeHierarchy() {
	}
	
	public TreeHierarchy(String rootColumn) {
		this.rootColumn = rootColumn;
	}

	public String getRootColumn() {
		return rootColumn;
	}
	
	public boolean isSetRootColumn() {
		return  rootColumn != null && !rootColumn.isEmpty();
	}

	public void setRootColumn(String rootColumn) {
		this.rootColumn = rootColumn;
	}
	
}

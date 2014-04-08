package de.tub.citydb.config.project.exporter;

import javax.xml.bind.annotation.XmlType;

@XmlType(name="ExportCityObjectGroupType", propOrder={
		"exportMemberAsXLinks"
})
public class ExportCityObjectGroup {
	private Boolean exportMemberAsXLinks;

	public Boolean getExportMemberAsXLinks() {
		return exportMemberAsXLinks;
	}

	public boolean isExportMemberAsXLinks() {
		if (exportMemberAsXLinks != null)
			return exportMemberAsXLinks.booleanValue();
		
		return false;
	}

	public void setExportMemberAsXLinks(Boolean exportMemberAsXLinks) {
		if (exportMemberAsXLinks != null)
			this.exportMemberAsXLinks = exportMemberAsXLinks;
	}

}

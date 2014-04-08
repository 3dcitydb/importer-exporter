package de.tub.citydb.config.project.exporter;

import javax.xml.bind.annotation.XmlType;

@XmlType(name="ExportCityObjectGroupType", propOrder={
		"exportMemberAsXLinks"
})
public class ExportCityObjectGroup {
	private Boolean exportMemberAsXLinks = false;

	public Boolean isExportMemberAsXLinks() {
		return exportMemberAsXLinks;
	}

	public void setExportMemberAsXLinks(Boolean exportMemberAsXLinks) {
		if (exportMemberAsXLinks != null)
			this.exportMemberAsXLinks = exportMemberAsXLinks;
	}

}

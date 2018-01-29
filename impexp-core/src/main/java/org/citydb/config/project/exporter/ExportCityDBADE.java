package org.citydb.config.project.exporter;

import javax.xml.bind.annotation.XmlType;

@XmlType(name="ExportCityDBADEType", propOrder={
		"exportMetadata"
})
public class ExportCityDBADE {
	private boolean exportMetadata = false;
	
	public boolean isExportMetadata() {
		return exportMetadata;
	}
	
	public void setExportMetadata(boolean exportMetadata) {
		this.exportMetadata = exportMetadata;
	}
	
}

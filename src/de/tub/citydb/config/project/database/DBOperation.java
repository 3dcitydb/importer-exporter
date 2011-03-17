package de.tub.citydb.config.project.database;

import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlType;

import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.general.FeatureClassMode;

@XmlType(name="DBOperationType", propOrder={
		"execute",
		"boundingBoxFeatureClass",
		"boundingBoxSrs"
})
public class DBOperation {
	private DBOperationMode execute = DBOperationMode.REPORT;
	private FeatureClassMode boundingBoxFeatureClass = FeatureClassMode.CITYOBJECT;
	@XmlIDREF
	private ReferenceSystem boundingBoxSrs = Internal.DEFAULT_DB_REF_SYS;
	
	public DBOperation() {
	}

	public DBOperationMode getExecute() {
		return execute;
	}

	public void setExecute(DBOperationMode execute) {
		this.execute = execute;
	}

	public FeatureClassMode getBoundingBoxFeatureClass() {
		return boundingBoxFeatureClass;
	}

	public void setBoundingBoxFeatureClass(FeatureClassMode boundingBoxFeatureClass) {
		this.boundingBoxFeatureClass = boundingBoxFeatureClass;
	}

	public ReferenceSystem getBoundingBoxSRS() {
		return boundingBoxSrs;
	}

	public void setBoundingBoxSRS(ReferenceSystem boundingBoxSrs) {
		this.boundingBoxSrs = boundingBoxSrs;
	}
	
}

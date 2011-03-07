package de.tub.citydb.config.project.database;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import de.tub.citydb.config.internal.Internal;

@XmlType(name="UpdateBatchingType", propOrder={
		"featureBatchValue",
		"gmlIdLookupServerBatchValue",
		"tempBatchValue"
		})
public class UpdateBatching {
	@XmlElement(required=true, defaultValue="20")
	@XmlSchemaType(name="positiveInteger")
	private Integer featureBatchValue = 20;
	@XmlElement(required=true, defaultValue="10000")
	@XmlSchemaType(name="positiveInteger")
	private Integer gmlIdLookupServerBatchValue = 10000;
	@XmlElement(required=true, defaultValue="10000")
	@XmlSchemaType(name="positiveInteger")
	private Integer tempBatchValue = 10000;
	
	public UpdateBatching() {
	}

	public Integer getFeatureBatchValue() {
		return featureBatchValue;
	}

	public void setFeatureBatchValue(Integer featureBatchValue) {
		if (featureBatchValue != null && featureBatchValue > 0 &&
				featureBatchValue <= Internal.ORACLE_MAX_BATCH_SIZE)
			this.featureBatchValue = featureBatchValue;
	}

	public Integer getGmlIdLookupServerBatchValue() {
		return gmlIdLookupServerBatchValue;
	}

	public void setGmlIdLookupServerBatchValue(Integer gmlIdLookupServerBatchValue) {
		if (gmlIdLookupServerBatchValue != null && gmlIdLookupServerBatchValue > 0 &&
				gmlIdLookupServerBatchValue <= Internal.ORACLE_MAX_BATCH_SIZE)
			this.gmlIdLookupServerBatchValue = gmlIdLookupServerBatchValue;
	}

	public Integer getTempBatchValue() {
		return tempBatchValue;
	}

	public void setTempBatchValue(Integer tempBatchValue) {
		if (tempBatchValue != null && tempBatchValue > 0 && 
				tempBatchValue <= Internal.ORACLE_MAX_BATCH_SIZE)
			this.tempBatchValue = tempBatchValue;
	}
	
}

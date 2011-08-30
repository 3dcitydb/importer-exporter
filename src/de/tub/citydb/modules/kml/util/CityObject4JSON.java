package de.tub.citydb.modules.kml.util;

public class CityObject4JSON {

//	private String gmlId;

	private double envelopeXmin;
	private double envelopeXmax;
	private double envelopeYmin;
	private double envelopeYmax;

	private int tileRow = 0;
	private int tileColumn = 0;
/*	
	public CityObject4JSON (String gmlId) {
		this.gmlId = gmlId;
	}
*/
	@Override
	public String toString() {
/*
		return "\t{\"gmlId\": \"" + gmlId +
			   "\",\n\t\"envelope\": [" + envelopeXmin + ", " + envelopeYmin + ", " + envelopeXmax + ", " + envelopeYmax +
			   "],\n\t\"tile\": [" + tileRow + ", " + tileColumn + "]}\n";
*/
		return "\",\n\t\"envelope\": [" + envelopeXmin + ", " + envelopeYmin + ", " + envelopeXmax + ", " + envelopeYmax +
		   	   "],\n\t\"tile\": [" + tileRow + ", " + tileColumn + "]}\n";
	}

/*
	@Override
	public boolean equals(Object obj) {
		
		try {
			CityObject4JSON cityObject4Json = (CityObject4JSON) obj;
			return this.gmlId.equals(cityObject4Json.getGmlId());
		}
		catch (Exception e) {}
		return false;
	}
	
	@Override
	public int hashCode(){
		return this.gmlId.hashCode();
	}
*/

	public void setEnvelope (double[] ordinatesArray) {
		envelopeYmax = ordinatesArray[4];
		envelopeYmin = ordinatesArray[1];
		envelopeXmax = ordinatesArray[3];
		envelopeXmin = ordinatesArray[0];
	}
	
	public void setEnvelopeXmin(double envelopeXmin) {
		this.envelopeXmin = envelopeXmin;
	}

	public double getEnvelopeXmin() {
		return envelopeXmin;
	}

	public void setEnvelopeXmax(double envelopeXmax) {
		this.envelopeXmax = envelopeXmax;
	}

	public double getEnvelopeXmax() {
		return envelopeXmax;
	}

	public void setEnvelopeYmin(double envelopeYmin) {
		this.envelopeYmin = envelopeYmin;
	}

	public double getEnvelopeYmin() {
		return envelopeYmin;
	}

	public void setEnvelopeYmax(double envelopeYmax) {
		this.envelopeYmax = envelopeYmax;
	}

	public double getEnvelopeYmax() {
		return envelopeYmax;
	}

	public void setTileRow(int tileRow) {
		this.tileRow = tileRow;
	}

	public int getTileRow() {
		return tileRow;
	}

	public void setTileColumn(int tileColumn) {
		this.tileColumn = tileColumn;
	}

	public int getTileColumn() {
		return tileColumn;
	}
/*
	public String getGmlId() {
		return gmlId;
	}
*/
}

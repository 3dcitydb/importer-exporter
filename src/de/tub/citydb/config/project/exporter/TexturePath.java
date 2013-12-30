package de.tub.citydb.config.project.exporter;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="TexturePathType", propOrder={
		"relativePath",
		"absolutePath",
		"noOfBuckets"
})
public class TexturePath {
	@XmlAttribute
	private TexturePathMode mode = TexturePathMode.RELATIVE;
	@XmlAttribute
	private boolean useBuckets = false;
	private String relativePath = "appearance";
	private String absolutePath = "";
	private Integer noOfBuckets;

	public boolean isAbsolute() {
		return mode == TexturePathMode.ABSOLUTE;
	}

	public boolean isRelative() {
		return mode == TexturePathMode.RELATIVE;
	}

	public TexturePathMode getMode() {
		return mode;
	}

	public void setMode(TexturePathMode mode) {
		this.mode = mode;
	}

	public boolean isUseBuckets() {
		return useBuckets;
	}

	public void setUseBuckets(boolean useBuckets) {
		this.useBuckets = useBuckets;
	}

	public String getRelativePath() {
		return relativePath;
	}

	public void setRelativePath(String relativePath) {
		this.relativePath = relativePath;
	}

	public String getAbsolutePath() {
		return absolutePath;
	}

	public void setAbsolutePath(String absolutePath) {
		this.absolutePath = absolutePath;
	}

	public int getNoOfBuckets() {
		if (noOfBuckets == null)
			return 0;

		return noOfBuckets.intValue();
	}

	public void setNoOfBuckets(Integer noOfBuckets) {
		if (noOfBuckets != null)
			this.noOfBuckets = noOfBuckets;
	}

}

package de.tub.citydb.db.gmlId;

import java.util.concurrent.atomic.AtomicBoolean;

import org.citygml4j.model.citygml.CityGMLClass;

public class GmlIdEntry {
	private long id;
	private long rootId;
	private boolean reverse;
	private String mapping;
	private CityGMLClass type;
	private AtomicBoolean isRegistered = new AtomicBoolean(false);
	private AtomicBoolean isRequested = new AtomicBoolean(false);

	public GmlIdEntry(long id, long rootId, boolean reverse, String mapping, CityGMLClass type) {
		this.id = id;
		this.rootId = rootId;
		this.reverse = reverse;
		this.mapping = mapping;
		this.type = type;
	}

	public long getId() {
		return id;
	}

	public long getRootId() {
		return rootId;
	}

	public boolean isReverse() {
		return reverse;
	}

	public String getMapping() {
		return mapping;
	}

	protected boolean isRequested() {
		return isRequested.get();
	}
	
	protected boolean getAndSetRequested(boolean value) {
		return isRequested.getAndSet(value);
	}
	
	protected boolean isRegistered() {
		return isRegistered.get();
	}
	
	protected boolean getAndSetRegistered(boolean value) {
		return isRegistered.getAndSet(value);
	}
	
	public CityGMLClass getType() {
		return type;
	}

}

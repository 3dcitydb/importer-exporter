package org.citydb.query.filter.lod;

import java.util.NoSuchElementException;

public class LodIterator {
	private final LodFilter filter;
	private final int fromLod;
	private final int toLod;

	private final int limit;
	private final int update;
	private int next;
	private int current;

	LodIterator(LodFilter filter, int fromLod, int toLod, boolean reverse) {
		this.filter = filter;
		this.fromLod = fromLod;
		this.toLod = toLod;

		limit = reverse ? fromLod - 1 : toLod + 1;
		update = reverse ? -1 : 1;

		reset();
	}

	public void reset() {
		current = update == -1 ? toLod + 1 : fromLod - 1;
		next = -1;
	}

	public boolean hasNext() {
		if (next == -1) {
			try {
				next = next();
			} catch (NoSuchElementException e) {
				//
			}
		}

		return next != -1;
	}

	public int next() {
		if (next == -1) {
			boolean hasFound = false;

			for (int lod = current + update; lod != limit; lod += update) {
				if (filter.isEnabled(lod)) {
					current = lod;
					hasFound = true;
					break;
				}
			}

			if (!hasFound)
				throw new NoSuchElementException();

		} else {
			current = next;
			next = -1;
		}

		return current;
	}
	
}

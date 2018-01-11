package org.citydb.citygml.importer.util;

import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Function;

import org.citydb.config.internal.Internal;

public class AttributeValueJoiner {
	private StringJoiner[] joiner;

	@SafeVarargs
	public final <T> AttributeValueJoiner join(String delimiter, List<T> values, Function<T, String>... mapper) {
		joiner = new StringJoiner[mapper.length];
		Arrays.setAll(joiner, i -> new StringJoiner(delimiter));

		if (values != null) {
			for (T value : values) {
				if (value == null)
					continue;

				for (int i = 0; i < mapper.length; i++) {
					String item = mapper[i].apply(value);
					if (i == 0 && (item == null || item.length() == 0))
						break;

					joiner[i].add(item != null ? item.trim() : "");
				}
			}
		}

		return this;
	}

	@SafeVarargs
	public final <T> AttributeValueJoiner join(List<T> values, Function<T, String>... mapper) {
		return join(Internal.DEFAULT_DELIMITER, values, mapper);
	}

	public <T> String join(String delimiter, List<T> values) {
		return join(delimiter, values, Object::toString).result(0);
	}

	public <T> String join(List<T> values) {
		return join(Internal.DEFAULT_DELIMITER, values, Object::toString).result(0);
	}

	public String result(int i) {
		if (joiner == null)
			throw new IllegalStateException("No join results found");
		if (i < 0 || i >= joiner.length)
			throw new IndexOutOfBoundsException("No join result " + i);

		String result = joiner[i].length() != 0 ? joiner[i].toString() : null;
		joiner[i] = null;		
		return result;
	}

}

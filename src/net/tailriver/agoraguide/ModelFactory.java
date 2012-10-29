package net.tailriver.agoraguide;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ModelFactory<T extends AbstractModel<T>> extends HashMap<String, T> {
	private static final long serialVersionUID = 1L;
	private Collection<T> values;
	private List<T> sortedValues;

	public ModelFactory() {}

	@Override
	public void clear() {
		values = null;
		sortedValues = null;
		super.clear();
	}

	@Override
	public T get(Object key) {
		if (key == null) {
			throw new IllegalArgumentException();
		}
		return super.get(key);
	}

	@Override
	public T put(String key, T value) {
		if (key == null || value == null) {
			throw new IllegalArgumentException();
		}
		return super.put(key, value);
	}

	@Override
	public Collection<T> values() {
		if (values == null) {
			values = Collections.unmodifiableCollection(super.values());
		}
		return values;
	}

	public List<T> sortedValues() {
		if (sortedValues == null) {
			List<T> list = new ArrayList<T>(super.values());
			Collections.sort(list);
			sortedValues = Collections.unmodifiableList(list);
		}
		return sortedValues;
	}
}

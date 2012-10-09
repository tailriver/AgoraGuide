package net.tailriver.agoraguide;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ModelFactory<T extends AbstractModel<T>> extends HashMap<String, T> {
	private static final long serialVersionUID = 1L;
	private List<T> values;

	public ModelFactory() {}

	@Override
	public List<T> values() {
		if (values == null) {
			List<T> list = new ArrayList<T>(super.values());
			Collections.sort(list);
			values = Collections.unmodifiableList(list);
		}
		return values;
	}
}

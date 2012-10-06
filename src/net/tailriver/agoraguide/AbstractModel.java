package net.tailriver.agoraguide;

import android.util.Log;

abstract public class AbstractModel<T extends AbstractModel<T>> implements Comparable<T> {
	private String id;

	protected AbstractModel() {}

	protected AbstractModel(String id) {
		this.id = id;
	}

	protected final void init_base() {
		long start = System.currentTimeMillis();
		init_factory();

		long passed = System.currentTimeMillis() - start;
		Log.d("timer", getClass().getSimpleName() + "#init() took " + passed + "ms");
	}

	abstract protected void init_factory();

	public int compareTo(T another) {
		return id.compareTo(another.id);
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof AbstractModel<?>) {
			return id.equals(((AbstractModel<?>) o).id);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public final String toString() {
		return id;
	}
}

package net.tailriver.agoraguide;

import android.database.sqlite.SQLiteDatabase;

abstract public class AbstractModel<T extends AbstractModel<T>>
implements Comparable<T>
{
	private String id;

	protected AbstractModel() {
		init(AgoraInitializer.getDatabase());
	}

	protected AbstractModel(String id) {
		this.id = id;
	}

	abstract protected void init(SQLiteDatabase database);

	public final String getId() {
		return id;
	}

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
	abstract public String toString();
}

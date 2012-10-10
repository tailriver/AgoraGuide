package net.tailriver.agoraguide;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

abstract public class AbstractModel<T extends AbstractModel<T>>
implements Comparable<T>
{
	private String id;

	protected AbstractModel() {}

	protected AbstractModel(String id) {
		this.id = id;
	}

	protected void execute() {
		SQLiteDatabase database = AgoraInitializer.getDatabase();
		long start = System.currentTimeMillis();
		init_factory(database);

		long passed = System.currentTimeMillis() - start;
		Log.d("DB SELECT", getClass().getSimpleName() + "#init() took " + passed + "ms");
	}

	abstract protected void init_factory(SQLiteDatabase database);

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

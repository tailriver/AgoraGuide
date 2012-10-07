package net.tailriver.agoraguide;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

abstract public class AbstractModel<T extends AbstractModel<T>>
implements Comparable<T>
{
	protected static Context        context  = AgoraGuideActivity.getContext(); 
	private String id;

	protected AbstractModel() {}

	protected AbstractModel(String id) {
		this.id = id;
	}

	protected void execute() {
		SQLiteDatabase database = AgoraGuideActivity.getDatabase();
		long start = System.currentTimeMillis();
		init_factory(database);

		long passed = System.currentTimeMillis() - start;
		Log.d("DB fetch", getClass().getSimpleName() + "#init() took " + passed + "ms");
	}

	abstract protected void init_factory(SQLiteDatabase database);

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

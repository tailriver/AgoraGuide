package net.tailriver.agoraguide;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class EntryFilter {
	private Collection<EntrySummary> collection;
	private boolean removeMatched;

	public EntryFilter() {
		this.collection = new HashSet<EntrySummary>();
		this.removeMatched = false;
	}

	public EntryFilter(Collection<EntrySummary> collection) {
		this();
		addAll(collection);
	}

	public EntryFilter add(EntrySummary summary) {
		collection.add(summary);
		return this;
	}

	public EntryFilter addAll(Collection<EntrySummary> collection) {
		if (collection != null) {
			this.collection.addAll(collection);
		}
		return this;
	}

	public EntryFilter addAllEntry() {
		return addAll(getAllEntry());
	}

	public EntryFilter addFavoriteEntry() {
		return addAll(getFavoriteEntry());
	}

	public EntryFilter addTimeFrameEntry() {
		return addAll(getTimeFrameEntry());
	}

	/** remain matched */
	public EntryFilter toPositiveFilter() {
		removeMatched = false;
		return this;
	}

	/** remain NOT matched */
	public EntryFilter toNegativeFilter() {
		removeMatched = true;
		return this;
	}

	@SuppressWarnings("unchecked")
	public EntryFilter applyFilter(Collection<? extends AbstractModel<?>> filter) {
		if (filter == null || filter.size() == 0) {
			return this;
		}
		AbstractModel<?> sample = filter.iterator().next();

		if (sample instanceof Area) {
			return applyAreaFilter((Collection<Area>) filter);
		}
		if (sample instanceof Category) {
			return applyCategoryFilter((Collection<Category>) filter);
		}
		if (sample instanceof EntrySummary) {
			return applyEntryFilter((Collection<EntrySummary>) filter);
		}

		throw new UnsupportedOperationException("");
	}

	public EntryFilter applyFilter(AbstractModel<?> filter) {
		return applyFilter(Collections.singleton(filter));
	}

	/** id, title, sponsor search */
	public EntryFilter applyFilter(CharSequence keyword) {
		Iterator<EntrySummary> it = collection.iterator();
		while (it.hasNext()) {
			EntrySummary s = it.next();
			Collection<String> filter = new HashSet<String>();
			filter.add(s.getId());
			filter.add(s.toString());
			filter.add(s.getSponsor());
			// TODO add detail contents

			boolean matched = false;
			for (String cs : filter) {
				if (cs.contains(keyword)) {
					matched = true;
					break;
				}
			}
			if (matched == removeMatched) {
				it.remove();
			}
		}
		return this;
	}

	public List<EntrySummary> getResult() {
		List<EntrySummary> list = new ArrayList<EntrySummary>(collection);
		Collections.sort(list);
		return list;
	}

	public List<EntrySummary> getResultTimeOrder() {
		List<EntrySummary> list = new ArrayList<EntrySummary>(collection);
		Collections.sort(list, new TimeFrameComparator());
		return list;
	}

	private EntryFilter applyAreaFilter(Collection<Area> filter) {
		Iterator<EntrySummary> it = collection.iterator();
		while (it.hasNext()) {
			EntrySummary s = it.next();
			if (filter.contains(s.getArea()) == removeMatched) {
				it.remove();
			}
		}
		return this;
	}

	private EntryFilter applyCategoryFilter(Collection<Category> filter) {
		Iterator<EntrySummary> it = collection.iterator();
		while (it.hasNext()) {
			EntrySummary s = it.next();
			if (filter.contains(s.getCategory()) == removeMatched) {
				it.remove();
			}
		}
		return this;
	}

	private EntryFilter applyEntryFilter(Collection<EntrySummary> filter) {
		if (removeMatched) {
			collection.removeAll(filter);
		} else {
			collection.retainAll(filter);
		}
		return this;
	}

	private final Collection<EntrySummary> getAllEntry() {
		return EntrySummary.values();
	}

	private final Collection<EntrySummary> getFavoriteEntry() {
		return Favorite.values();
	}

	private final Collection<EntrySummary> getTimeFrameEntry() {
		Collection<EntrySummary> set = new HashSet<EntrySummary>();
		for (TimeFrame tf : TimeFrame.values()) {
			set.add(tf.getSummary());
		}
		return set;
	}

	class TimeFrameComparator implements Comparator<EntrySummary> {
		public int compare(EntrySummary lhs, EntrySummary rhs) {
			return TimeFrame.get(lhs).compareTo(TimeFrame.get(rhs));
		}
	}
}

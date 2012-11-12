package net.tailriver.agoraguide;

public class IntentExtra {
	private static final String PREFIX = IntentExtra.class.getPackage()
			.getName();

	/** String: Area.get() */
	public static final String AREA_ID = PREFIX + ".area";

	/** String: EntrySummary.get() */
	public static final String ENTRY_ID = PREFIX + ".entry";

	/** int */
	public static final String NOTIFICATION_ID = PREFIX + ".notify";

	/** String */
	public static final String NOTIFICATION_TEXT = PREFIX + ".notify.text";

	/** long */
	public static final String NOTIFICATION_WHEN = PREFIX + ".notify.when";

	/** SearchType */
	public static final String SEARCH_TYPE = PREFIX + ".search";
}

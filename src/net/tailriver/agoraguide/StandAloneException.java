package net.tailriver.agoraguide;

import java.io.IOException;

public class StandAloneException extends IOException {
	private static final long serialVersionUID = 1L;

	public StandAloneException() {
		super("not connected to the internet");
	}

	public StandAloneException(Throwable e) {
		super(e);
	}
}

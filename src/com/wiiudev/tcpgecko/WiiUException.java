package com.wiiudev.tcpgecko;

/**
 * An exception that is thrown when there is a non IO related protocol error<br>
 * If this gets thrown, it is probably a good idea to reboot the Wii U
 * 
 * @author gudenau
 * */
public class WiiUException extends Exception {

	private static final long serialVersionUID = 7181954877162249893L;

	public WiiUException(String message) {
		super(message);
	}

	public WiiUException(String message, Exception exception) {
		super(message, exception);
	}
}

package com.wiiudev.tcpgecko.enumeration;

/**
 * An enumeration of all supported consoles
 * 
 * @author gudenau
 * */
public enum Console {
	WIIU(0x82);
	
	public final byte value;
	Console(int value){
		this.value = (byte) value;
	}
	
	/**
	 * Returns the Console that matches the provided ID
	 * 
	 * @param console The console ID
	 * 
	 * @return The matching Console, or null if there is none
	 * */
	public static Console getConsole(byte console){
		for(Console c : values()){
			if(c.value == console){
				return c;
			}
		}
		
		return null;
	}
}

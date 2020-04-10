package com.wiiudev.tcpgecko.enumeration;

/**
 * An enumeration of all known status codes
 * 
 * @author gudenau
 * */
public enum Status {
	RUNNING (0x01),
	OK      (0xBD),
	OK_EMPTY(0xB0),
	GC_ACK  (0xAA);

	public final byte value;
	
	Status(int value){
		this.value = (byte)value;
	}

	/**
	 * Returns the status corresponding to the provided byte
	 * 
	 * @return The Status, null if unknown
	 * */
	public static Status getStatusFromByte(byte status) {
		for(Status s : values()){
			if(s.value == status){
				return s;
			}
		}
		
		return null;
	}
}

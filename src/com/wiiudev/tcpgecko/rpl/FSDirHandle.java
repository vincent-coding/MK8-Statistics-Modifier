package com.wiiudev.tcpgecko.rpl;

/**
 * @author gudenau
 */
public class FSDirHandle {
	private boolean initalized;
	private int buffer;
	private boolean hasBuffer;

	public boolean getInitalized() {
		return initalized;
	}
	
	public void setInitalized(boolean added){
		this.initalized = added;
	}

	public void setBuffer(int buffer) {
		hasBuffer = true;
		this.buffer = buffer;
	}

	public int getBuffer(){
		return buffer;
	}

	public boolean hasBuffer() {
		return hasBuffer;
	}
	
	public void removeBuffer(){
		hasBuffer = false;
	}
}

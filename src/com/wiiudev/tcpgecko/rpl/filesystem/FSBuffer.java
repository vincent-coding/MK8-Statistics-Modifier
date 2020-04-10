package com.wiiudev.tcpgecko.rpl.filesystem;

import java.io.IOException;

import com.wiiudev.tcpgecko.WiiUException;
import com.wiiudev.tcpgecko.memory.IAlocatedBuffer;
import com.wiiudev.tcpgecko.memory.IRemoteBuffer;

/**
 * A simple buffer abstraction for use by the {@link com.wiiudev.tcpgecko.rpl.filesystem.Filesystem Filesystem}
 * */
class FSBuffer {
	private boolean initalized;
	private IRemoteBuffer buffer = null;
	
	public boolean getInitalized() {
		return initalized;
	}
	
	public void setInitalized(boolean added){
		this.initalized = added;
	}

	public void setBuffer(IRemoteBuffer buffer) {
		this.buffer = buffer;
	}

	public IRemoteBuffer getBuffer(){
		return buffer;
	}

	public boolean hasBuffer() {
		return buffer != null;
	}
	
	public void removeBuffer() throws IOException, WiiUException{
		if(hasBuffer() && buffer instanceof IAlocatedBuffer){
			((IAlocatedBuffer)buffer).free();
		}
	}
}

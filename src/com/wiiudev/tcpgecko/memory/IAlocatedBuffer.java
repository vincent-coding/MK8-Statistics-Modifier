package com.wiiudev.tcpgecko.memory;

import java.io.IOException;

import com.wiiudev.tcpgecko.WiiUException;

/**
 * An interface to represent a block of memory we allocated on the Wii U, used to create an abstraction layer to hopefully reduce bandwidth requirements
 * 
 * @author gudenau
 * */
public interface IAlocatedBuffer extends IRemoteBuffer {
	/**
	 * Used to free this buffer
	 * 
	 * @throws WiiUException 
	 * @throws IOException 
	 * */
	public void free() throws IOException, WiiUException;
}

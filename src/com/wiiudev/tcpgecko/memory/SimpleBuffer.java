package com.wiiudev.tcpgecko.memory;

import java.io.IOException;

import com.wiiudev.tcpgecko.TcpGecko;
import com.wiiudev.tcpgecko.WiiUException;

/**
 * A simple and stupid implementation of a remote buffer, only tracks if it is dirty<br>
 * So it flushes the entire buffer, not just the changed parts
 * 
 * @author gudenau
 * */
public class SimpleBuffer implements IRemoteBuffer {

	private final TcpGecko gecko;
	private final int address;
	private final byte[] data;
	private boolean dirty = false;
	
	public SimpleBuffer(TcpGecko gecko, int address, byte[] data){
		this.gecko = gecko;
		this.address = address;
		this.data = data;
	}
	
	@Override
	public int getSize() {
		return data.length;
	}

	@Override
	public int getAddress() {
		return address;
	}

	@Override
	public void setData(int address, byte[] data, int offset, int length) {
		System.arraycopy(data, offset, this.data, address, length);
		dirty = true;
	}

	@Override
	public void getData(int address, byte[] data, int offset, int length) {
		System.arraycopy(this.data, address, this.data, offset, length);
	}

	@Override
	public boolean isDirty() {
		return dirty;
	}

	@Override
	public void flush() throws IOException, WiiUException {
		gecko.writeMemory(address, data, 0, data.length);
		dirty = false;
	}

	@Override
	public void update() throws IOException, WiiUException {
		update(0, data.length);
		dirty = false;
	}

	@Override
	public void update(int offset, int size) throws IOException, WiiUException {
		gecko.readMemory(address, data, offset, size);
	}

	@Override
	public void markDirty() {
		dirty = true;
	}

	@Override
	public void clearDirty() {
		dirty = false;
	}
}

package com.wiiudev.tcpgecko.memory;

import java.io.IOException;

import com.wiiudev.tcpgecko.TcpGecko;
import com.wiiudev.tcpgecko.WiiUException;
import com.wiiudev.tcpgecko.rpl.CoreInit;

public class SimpleAlocatedBuffer extends SimpleBuffer implements IAlocatedBuffer {

	private final CoreInit coreInit;
	private final boolean isHeap;
	private boolean isAlocated = true;
	
	public SimpleAlocatedBuffer(CoreInit coreInit, boolean isHeap, TcpGecko gecko, int address, byte[] data) {
		super(gecko, address, data);
		
		this.coreInit = coreInit;
		this.isHeap = isHeap;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void free() throws IOException, WiiUException {
		if(isAlocated){
			return;
		}
		isAlocated = false;
		
		if(isHeap){
			coreInit.freeHeap(getAddress());
		}else{
			coreInit.free(getAddress());
		}
	}

}

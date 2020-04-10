package com.wiiudev.tcpgecko;

import java.io.IOException;

/**
 * A class to represent a symbol obtained from the Wii U
 * 
 * @author gudenau
 * */
public class ExportedSymbol {
	private final int address;
	private final String rplName;
	private final String symbolName;
	private final TcpGecko gecko;
	
	protected ExportedSymbol(int address, String rplName, String symbolName, TcpGecko gecko){
		this.address = address;
		this.rplName = rplName;
		this.symbolName = symbolName;
		this.gecko = gecko;
	}
	
	/**
	 * Calls this symbol with the provided parameters
	 * 
	 * @param params The parameters to pass to the Wii U
	 * 
	 * @throws IOException If there was an error talking to the Wii U
	 * */
	public long call(int ... params) throws IOException{
		return gecko.callRemoteMethod(this, params);
	}

	/**
	 * Gets the address of the symbol
	 * */
	public int getAddress() {
		return address;
	}

	/**
	 * Gets the RPL that contains this symbol
	 * */
	public String getRplName() {
		return rplName;
	}

	/**
	 * Gets the name of this symbol
	 * */
	public String getSymbolName() {
		return symbolName;
	}
}

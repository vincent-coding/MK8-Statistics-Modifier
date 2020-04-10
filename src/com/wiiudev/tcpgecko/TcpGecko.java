package com.wiiudev.tcpgecko;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.wiiudev.tcpgecko.enumeration.Commands;
import com.wiiudev.tcpgecko.enumeration.Console;
import com.wiiudev.tcpgecko.enumeration.MemoryAccess;
import com.wiiudev.tcpgecko.enumeration.Status;
import com.wiiudev.tcpgecko.rpl.CoreInit;
import com.wiiudev.tcpgecko.rpl.filesystem.Filesystem;

/**
 * Java port of the tcpgecko.py script<br>
 * <br>
 * Warning:<br>
 * Symbol caching might not work well, it is not well tested
 * 
 * @author gudenau
 * */
public class TcpGecko implements Closeable {
	private Logger logger;
	
	private static final int DEFAULT_PORT = 7331;
	
	private Socket socket;
	private InputStream inputStream;
	private OutputStream outputStream;

	private boolean enableSymbolCache;
	private HashMap<String, HashMap<String, ExportedSymbol>> symbolCache;
	
	private CoreInit coreInitInstance;
	private Filesystem filesystemInstance;
	
	/**
	 * Connects to the provided host with the default port
	 * 
	 * @param host Hostname to use, most likely an IP
	 * 
	 * @throws UnknownHostException If there the host was bad
	 * @throws IOException If there was an error getting the streams or opening the socket
	 * */
	public TcpGecko(String host) throws UnknownHostException, IOException {
		this(host, DEFAULT_PORT);
	}
	
	/**
	 * Connects to the provided host with the provided port
	 * 
	 * @param host Hostname to use, most likely an IP
	 * @param port The port to use when connecting
	 * 
	 * @throws UnknownHostException If there the host was bad
	 * @throws IOException If there was an error getting the streams or opening the socket
	 * */
	public TcpGecko(String host, int port) throws UnknownHostException, IOException {
		this(new Socket(host, port));
	}

	/**
	 * Uses an existing {@link java.net.Socket socket} to communicate with
	 * 
	 * @param socket The socket to use
	 * 
	 * @throws IOException If there was an error getting the streams
	 * */
	public TcpGecko(Socket socket) throws IOException {
		logger = Logger.getLogger("TCPGecko");
		logger.setLevel(Level.WARNING);
		
		debug("Constructing...");
		
		this.socket = socket;
		
		inputStream = socket.getInputStream();
		outputStream = socket.getOutputStream();
		
		symbolCache = new HashMap<String, HashMap<String, ExportedSymbol>>();
		
		coreInitInstance = new CoreInit(this);
		filesystemInstance = new Filesystem(this);
		
		debug("Done!");
	}

	/**
	 * Pokes an address in memory with a byte
	 * 
	 * @param address The address to poke
	 * @param value The value to poke
	 * 
	 * @throws IOException If an exception occurs while talking to the Wii U or a buffer
	 * @throws IllegalArgumentException If length or address is invalid
	 * */
	public void pokeMemory(long address, byte value) throws IOException{
		debug("Poking address " + address + " with the byte " + value);
		
		// Validate params
		if(address < 0){
			throw new IllegalArgumentException("Address must be positive!");
		}
		
		// Make sure the Wii U will be happy
		if(!validateMemoryRange(address, 1)){
			throw new IllegalArgumentException("Address is not valid!");
		}
		if(!validateMemoryAccess(address, 1, MemoryAccess.WRITE)){
			throw new IllegalArgumentException("Cannot write to address!");
		}
		
		sendCommand(Commands.MEMORY_POKE_8);
		writeInteger((int)address);
		writeByte(value);
	}
	
	/**
	 * Pokes an address in memory with a short
	 * 
	 * @param address The address to poke
	 * @param value The value to poke
	 * 
	 * @throws IOException If an exception occurs while talking to the Wii U or a buffer
	 * @throws IllegalArgumentException If length or address is invalid
	 * */
	public void pokeMemory(long address, short value) throws IOException{
		debug("Poking address " + address + " with the short " + value);
		
		// Validate params
		if(address < 0){
			throw new IllegalArgumentException("Address must be positive!");
		}
		
		// Make sure the Wii U will be happy
		if(!validateMemoryRange(address, 2)){
			throw new IllegalArgumentException("Address is not valid!");
		}
		if(!validateMemoryAccess(address, 2, MemoryAccess.WRITE)){
			throw new IllegalArgumentException("Cannot write to address!");
		}
		
		sendCommand(Commands.MEMORY_POKE_16);
		writeInteger((int)address);
		writeShort(value);
	}
	
	/**
	 * Pokes an address in memory with an integer
	 * 
	 * @param address The address to poke
	 * @param value The value to poke
	 * 
	 * @throws IOException If an exception occurs while talking to the Wii U or a buffer
	 * @throws IllegalArgumentException If length or address is invalid
	 * */
	public void pokeMemory(long address, int value) throws IOException{
		debug("Poking address " + address + " with the int " + value);
		
		// Validate params
		if(address < 0){
			throw new IllegalArgumentException("Address must be positive!");
		}
		
		// Make sure the Wii U will be happy
		if(!validateMemoryRange(address, 4)){
			throw new IllegalArgumentException("Address is not valid!");
		}
		if(!validateMemoryAccess(address, 4, MemoryAccess.WRITE)){
			throw new IllegalArgumentException("Cannot write to address!");
		}
		
		sendCommand(Commands.MEMORY_POKE_32);
		writeInteger((int)address);
		writeInteger(value);
	}
	
	/**
	 * Pokes an address in memory with a long
	 * 
	 * @param address The address to poke
	 * @param value The value to poke
	 * 
	 * @throws IOException If an exception occurs while talking to the Wii U or a buffer
	 * @throws IllegalArgumentException If length or address is invalid
	 * */
	public void pokeMemory(long address, long value) throws IOException {
		debug("Poking address " + address + " with the long " + value);
		// Validate params
		if(address < 0){
			throw new IllegalArgumentException("Address must be positive!");
		}
				
		// Make sure the Wii U will be happy
		if(!validateMemoryRange(address, 8)){
			throw new IllegalArgumentException("Address is not valid!");
		}
		if(!validateMemoryAccess(address, 8, MemoryAccess.WRITE)){
			throw new IllegalArgumentException("Cannot write to address!");
		}
				
		sendCommand(Commands.MEMORY_POKE_32);
		writeInteger((int)address);
		writeInteger((int) ((value >> 32) & 0x00000000FFFFFFFF));
		sendCommand(Commands.MEMORY_POKE_32);
		writeInteger((int)address + 4);
		writeInteger((int) (value & 0x00000000FFFFFFFF));
	}
	
	/**
	 * Pokes an address in memory with a float
	 * 
	 * @param address The address to poke
	 * @param value The value to poke
	 * 
	 * @throws IOException If an exception occurs while talking to the Wii U or a buffer
	 * @throws IllegalArgumentException If address is invalid
	 * */
	public void pokeMemory(long address, float value) throws IOException {
		debug("Poking address " + address + " with the float " + value);
		
		// Convert float to integer
		ByteBuffer buffer = ByteBuffer.allocate(4);
		buffer.putFloat(value);
		buffer.position(0);
		int integer = buffer.getInt();
		
		// Do the poke
		pokeMemory(address, integer);
	}
	
	/**
	 * Pokes an address in memory with a double
	 * 
	 * @param address The address to poke
	 * @param value The value to poke
	 * 
	 * @throws IOException If an exception occurs while talking to the Wii U or a buffer
	 * @throws IllegalArgumentException If address is invalid
	 * */
	public void pokeMemory(long address, double value) throws IOException {
		debug("Poking address " + address + " with the double " + value);
		
		// Convert double to long
		ByteBuffer buffer = ByteBuffer.allocate(8);
		buffer.putDouble(value);
		buffer.position(0);
		long integer = buffer.getLong();
		
		// Do the poke
		pokeMemory(address, integer);
	}
	
	/**
	 * Pokes an address in memory with a UTF-8 string
	 * 
	 * @param address The address to poke
	 * @param value The value to poke
	 * @param nullTerminated Append a null byte to the end of the string?
	 * 
	 * @throws IOException If an exception occurs while talking to the Wii U or a buffer
	 * @throws WiiUException If something bad happens while communicating with the Wii U
	 * @throws IllegalArgumentException If address is invalid
	 * */
	public void pokeMemory(long address, String value, boolean nullTerminated) throws IOException, WiiUException {
		debug("Poking address " + address + " with the" + (nullTerminated ? " null terminated" : "") + " string " + value);
		
		if(nullTerminated){
			value = value + "\u0000";
		}
		
		// Convert the String to a byte array
		byte[] data = value.getBytes(StandardCharsets.UTF_8);
		
		// Write it
		writeMemory(address, data, 0, data.length);
	}
	
	/**
	 * Peeks a byte from the Wii U
	 * 
	 * @param address The address to peek
	 * 
	 * @return The peeked byte
	 * 
	 * @throws IOException If there was an error reading from the Wii U
	 * @throws WiiUException If something bad happens while communicating with the Wii U
	 * @throws IllegalArgumentException If the address is invalid
	 */
	public byte peekByte(long address) throws IOException, WiiUException{
		debug("Peaking the byte at " + address);
		byte[] buffer = readMemory(address, 1);
		return buffer[0];
	}
	
	/**
	 * Peeks a short from the Wii U
	 * 
	 * @param address The address to peek
	 * 
	 * @return The peeked short
	 * 
	 * @throws IOException If there was an error reading from the Wii U
	 * @throws WiiUException If something bad happens while communicating with the Wii U
	 * @throws IllegalArgumentException If the address is invalid
	 */
	public short peekShort(long address) throws IOException, WiiUException{
		debug("Peaking the short at " + address);
		
		// Shorts are two bytes
		byte[] buffer = readMemory(address, 2);
		
		// Need to create a short from the data
		// The Wii U is big endian
		return (short) (((buffer[0] << 8) & 0xFF00) |
				(buffer[1] & 0x00FF));
	}
	
	/**
	 * Peeks an integer from the Wii U
	 * 
	 * @param address The address to peek
	 * 
	 * @return The peeked integer
	 * 
	 * @throws IOException If there was an error reading from the Wii U
	 * @throws WiiUException If something bad happens while communicating with the Wii U
	 * @throws IllegalArgumentException If the address is invalid
	 */
	public int peekInteger(long address) throws IOException, WiiUException{
		debug("Peaking the int at " + address);
		
		// Integers are four bytes
		byte[] buffer = readMemory(address, 4);
		
		// Need to create a integer from the data
		// The Wii U is big endian
		return (int) (((buffer[0] << 24) & 0xFF000000) |
				((buffer[1] << 16) & 0x00FF0000) |
				((buffer[2] <<  8) & 0x0000FF00) |
				(buffer[3] & 0x000000FF));
	}
	
	/**
	 * Peeks a long from the Wii U
	 * 
	 * @param address The address to peek
	 * 
	 * @return The peeked long
	 * 
	 * @throws IOException If there was an error reading from the Wii U
	 * @throws WiiUException If something bad happens while communicating with the Wii U
	 * @throws IllegalArgumentException If the address is invalid
	 */
	public long peekLong(long address) throws IOException, WiiUException{
		debug("Peaking the long at " + address);
		
		// Longs are eight bytes
		byte[] buffer = readMemory(address, 8);
		
		readData(buffer);
		
		// Need to create a integer from the data
		// The Wii U is big endian
		return (long) (
				((((long)buffer[0]) << 56) & 0xFF00000000000000L) |
				((((long)buffer[1]) << 48) & 0x00FF000000000000L) |
				((((long)buffer[2]) << 40) & 0x0000FF0000000000L) |
				((((long)buffer[3]) << 32) & 0x000000FF00000000L) |
				((((long)buffer[4]) << 24) & 0x00000000FF000000L) |
				((((long)buffer[5]) << 16) & 0x0000000000FF0000L) |
				((((long)buffer[6]) <<  8) & 0x000000000000FF00L) |
				(( (long)buffer[7]       ) & 0x00000000000000FFL));
	}
	
	/**
	 * Peeks a float from the Wii U
	 * 
	 * @param address The address to peek
	 * 
	 * @return The peeked float
	 * 
	 * @throws IOException If there was an error reading from the Wii U
	 * @throws WiiUException If something bad happens while communicating with the Wii U
	 * @throws IllegalArgumentException If the address is invalid	
	 */
	public float peekFloat(long address) throws IOException, WiiUException{
		debug("Peaking the float at " + address);
		
		int integer = peekInteger(address);
		
		ByteBuffer buffer = ByteBuffer.allocate(4);
		buffer.putInt(integer);
		buffer.position(0);
		return buffer.getFloat();
	}
	
	/**
	 * Peeks a double from the Wii U
	 * 
	 * @param address The address to peek
	 * 
	 * @return The peeked double
	 * 
	 * @throws IOException If there was an error reading from the Wii U
	 * @throws WiiUException If something bad happens while communicating with the Wii U
	 * @throws IllegalArgumentException If the address is invalid	
	 */
	public double peekDouble(long address) throws IOException, WiiUException{
		debug("Peaking the double at " + address);
		
		long integer = peekLong(address);
		
		ByteBuffer buffer = ByteBuffer.allocate(8);
		buffer.putLong(integer);
		buffer.position(0);
		return buffer.getDouble();
	}
	
	/**
	 * Reads memory from a Wii U, if length is greater than 0x0400 it is read in blocks
	 * 
	 * @param address Address to read from
	 * @param length Length of the data to read
	 * 
	 * @return Data read
	 * 
	 * @throws IOException If an exception occurs while talking to the Wii U or a buffer
	 * @throws WiiUException If something bad happens while communicating with the Wii U
	 * @throws IllegalArgumentException If length or address is invalid
	 * */
	public byte[] readMemory(long address, int length) throws IOException, WiiUException{
		debug("Reading memory at " + address + " to " + (address + length));
		
		// Validate input params
		if(length <= 0){
			throw new IllegalArgumentException("Length must be greater than 0!");
		}
		if(length > 0x00000000FFFFFFFFL){
			throw new IllegalArgumentException("Length must be less than 0xFFFFFFFF!");
		}
		if(address < 0){
			throw new IllegalArgumentException("Address must be positive!");
		}
		
		// Make sure the Wii U will be happy
		if(!validateMemoryRange(address, length)){
			throw new IllegalArgumentException("Address range is not valid!");
		}
		if(!validateMemoryAccess(address, length, MemoryAccess.READ)){
			throw new IllegalArgumentException("Cannot read from address!");
		}
		
		ByteArrayOutputStream output = new ByteArrayOutputStream(length);
		
		int readLength;
		byte[] buffer = new byte[0x400];
		
		// Read the data
		while(length > 0){
			debug("Reading a block with a length of " + length);
			
			// Tell the Wii U to read memory
			sendCommand(Commands.MEMORY_READ);
			
			// Calculate the amount of data to read
			readLength = (length > 0x400 ? 0x400 : length);
			
			// Tell the Wii U what to read
			writeIntegers((int)address, (int)address + readLength);
			
			// Process returned data
			Status status = getStatus();
			switch(status){
			// The memory is not all 0, read the data
			case OK:
				readData(buffer, 0, readLength);
				output.write(buffer, 0, readLength);
				break;
			// The memory was all 0, no nead to read the data
			case OK_EMPTY:
				clearBuffer(buffer);
				output.write(buffer, 0, readLength);
				break;
			// Something went wrong
			default:
				throw new WiiUException("Got an unknown status while reading memory!");
			}
			
			// A little cheaty, but this works fine
			length  -= 0x400;
			address += 0x400;
		}
		
		debug("Done!");
		
		return output.toByteArray();
	}
	
	/**
	 * Reads memory from a Wii U, if length is greater than 0x0400 it is read in blocks
	 * 
	 * @param address Address to read from
	 * @param data Buffer to write
	 * @param offset Offset into the buffer
	 * @param length Length of the data to read
	 * 
	 * @throws IOException If an exception occurs while talking to the Wii U or a buffer
	 * @throws WiiUException If something bad happens while communicating with the Wii U
	 * @throws IllegalArgumentException If length or address is invalid
	 * */
	public void readMemory(long address, byte[] data, int offset, int length) throws IOException, WiiUException {
		byte[] memory = readMemory(address, length);
		System.arraycopy(memory, 0, data, offset, length);
	}
	
	/**
	 * Reads memory from a Wii U, if length is greater than 0x0400 it is read in blocks<br>
	 * This calls {@link com.wiiudev.tcpgecko.TcpGecko#readMemory(long, byte[], int, int) readMemory(address, data, 0, data.length)}
	 * 
	 * @param address Address to read from
	 * @param data Buffer to write
	 * 
	 * @throws IOException If an exception occurs while talking to the Wii U or a buffer
	 * @throws WiiUException If something bad happens while communicating with the Wii U
	 * @throws IllegalArgumentException If length or address is invalid
	 * */
	public void readMemory(long address, byte[] data) throws IOException, WiiUException {
		readMemory(address, data, 0, data.length);
	}
	
	/**
	 * Writes data into kernel memory, if the length is greater than 0x04 it is written in blocks
	 * 
	 * @param address Address to write to
	 * @param data The data to be written
	 * @param offset Offset into the data to start
	 * @param length Amount of data to write
	 * 
	 * @throws IOException If an exception occurs while talking to the Wii U
	 * @throws IllegalArgumentException If length or address is invalid
	 * */
	public void writeKernelMemory(long address, byte[] data, int offset, int length) throws IOException {
		debug("Writing memory at " + address + " to " + (address + length) + " as kernel");
		
		// Validate input params
		if(length <= 0){
			throw new IllegalArgumentException("Length must be greater than 0!");
		}
		if(length % 4 != 0){
			throw new IllegalArgumentException("Length must be divisable by 4!");
		}
		if(address < 0){
			throw new IllegalArgumentException("Address must be positive!");
		}
		
		// Make sure the Wii U will be happy
		if(!validateMemoryRange(address, length)){
			throw new IllegalArgumentException("Address range is not valid!");
		}
		if(!validateMemoryAccess(address, length, MemoryAccess.WRITE)){
			throw new IllegalArgumentException("Cannot write to address!");
		}
		
		// Wrap the data, make it easy to read an integer
		ByteBuffer buffer = ByteBuffer.wrap(data);
		buffer.order(ByteOrder.BIG_ENDIAN);
		buffer.position(offset);
		
		while(length > 0){
			debug("Writing a chunk");
			sendCommand(Commands.MEMORY_KERNEL_WRITE);
			writeIntegers((int)address, buffer.getInt());
			
			length -= 4;
			address += 4;
		}
		
		debug("Done!");
	}
	
	/**
	 * Reads kernel memory from a Wii U, if length is greater than 0x04 it is read in blocks
	 * 
	 * @param address Address to read from
	 * @param length Length of the data to read
	 * 
	 * @return Data read
	 * 
	 * @throws IOException If an exception occurs while talking to the Wii U or a buffer
	 * @throws IllegalArgumentException If length or address is invalid
	 * */
	public byte[] readKernelMemory(long address, int length) throws IOException{
		debug("Reading memory at " + address + " to " + (address + length) + " as kernel");
		
		// Validate input params
		if(length <= 0){
			throw new IllegalArgumentException("Length must be greater than 0!");
		}
		if(address < 0){
			throw new IllegalArgumentException("Address must be positive!");
		}
		
		// Make sure the Wii U will be happy
		if(!validateMemoryRange(address, length)){
			throw new IllegalArgumentException("Address range is not valid!");
		}
		if(!validateMemoryAccess(address, length, MemoryAccess.WRITE)){
			throw new IllegalArgumentException("Cannot read from address!");
		}
		
		ByteArrayOutputStream output = new ByteArrayOutputStream(length);
		
		// Read the data
		while(length > 0){
			debug("Reading chunk");
			byte[] buffer = readKernelMemory(address);
			output.write(buffer, 0, length > 4 ? 4 : length);
			
			// A little cheaty, but this works fine
			length -= 4;
			address += 4;
		}
		
		debug("Done!");
		
		return output.toByteArray();
	}
	
	/**
	 * Reads four bytes from the kernel memory from the Wii U
	 * 
	 * @param address Address to read from
	 * 
	 * @return Data read
	 * 
	 * @throws IOException If an exception occurs while talking to the Wii U or a buffer
	 * @throws IllegalArgumentException If length or address is invalid
	 * */
	public byte[] readKernelMemory(long address) throws IOException{
		debug("Reading memory at " + address + " as kernel");
		
		// Validate input params
		if(address < 0){
			throw new IllegalArgumentException("Address must be positive!");
		}
		
		// Make sure the Wii U will be happy
		if(!validateMemoryRange(address, 4)){
			throw new IllegalArgumentException("Address range is not valid!");
		}
		if(!validateMemoryAccess(address, 4, MemoryAccess.WRITE)){
			throw new IllegalArgumentException("Cannot read from address!");
		}
		
		sendCommand(Commands.MEMORY_KERNEL_READ);
		writeInteger((int) address);
		
		byte[] data = new byte[4];
		readData(data);
		return data;
	}
	
	/**
	 * Writes data into memory, if the length is greater than 0x0400 it is written in blocks
	 * 
	 * @param address Address to write to
	 * @param data The data to be written
	 * @param offset Offset into the data to start
	 * @param length Amount of data to write
	 * 
	 * @throws IOException If an exception occurs while talking to the Wii U
	 * @throws WiiUException If something bad happens while communicating with the Wii U
	 * @throws IllegalArgumentException If length or address is invalid
	 * */
	public void writeMemory(long address, byte[] data, int offset, int length) throws IOException, WiiUException {
		debug("Writing memory at " + address + " to " + (address + length));
		
		// Validate input params
		if(length <= 0){
			throw new IllegalArgumentException("Length must be greater than 0!");
		}
		if(address < 0){
			throw new IllegalArgumentException("Address must be positive!");
		}
		
		// Make sure the Wii U will be happy
		if(!validateMemoryRange(address, length)){
			throw new IllegalArgumentException("Address range is not valid!");
		}
		if(!validateMemoryAccess(address, length, MemoryAccess.WRITE)){
			throw new IllegalArgumentException("Cannot write to address!");
		}
		
		int writeLength;
		
		// Write the data
		while(length > 0){
			debug("Writing chunk");
			sendCommand(Commands.MEMORY_WRITE);
			
			writeLength = length > 0x0400 ? 0x0400 : length;
			writeIntegers((int)address, (int)address + writeLength);
			writeData(data, offset, writeLength);
			
			if(getStatus() != Status.GC_ACK){
				// This is bad...
				throw new WiiUException("Got unexpected status while writing memory!");
			}
			
			length -= writeLength;
			address += writeLength;
		}
		
		debug("Done!");
	}
	
	/**
	 * Calls a method on the Wii U
	 * 
	 * @param symbol The symbol that defines the method
	 * @param params The parameters for the method
	 * 
	 * @throws IOException If there was an error talking to the Wii U
	 * @throws IllegalArgumentException If there are to many parameters
	 * */
	public long callRemoteMethod(ExportedSymbol symbol, int ... params) throws IOException{
		debug("Calling " + symbol.getSymbolName() + " from " + symbol.getRplName());
		
		if(params.length > 8 && params.length <= 16){
			int[] realParams = new int[16];
			System.arraycopy(params, 0, realParams, 0, params.length);
			sendCommand(Commands.RPC_BIG);
			writeInteger(symbol.getAddress());
			writeIntegers(realParams);
			
			return readLong();
		}else if(params.length <= 8){
			int[] realParams = new int[8];
			System.arraycopy(params, 0, realParams, 0, params.length);
			sendCommand(Commands.RPC_BIG);
			writeInteger(symbol.getAddress());
			writeIntegers(realParams);
			
			return readLong();
		}
		
		throw new IllegalArgumentException("Too many paramaters!");
	}
	
	/**
	 * Gets a symbol from the Wii U
	 * 
	 * @param rplName The PRL the symbol refers to
	 * @param symbolName The name of the symbol
	 * @param isPointer Is the returned symbol is a pointer?
	 * @param isData Is the symbol data?
	 * 
	 * @return The requested symbol
	 * 
	 * @throws IOException If there was an error taking to the Wii U
	 * @throws WiiUException If something bad happens while communicating with the Wii U
	 * */
	public ExportedSymbol getSymbol(String rplName, String symbolName, boolean isPointer, boolean isData) throws IOException, WiiUException{
		debug("Getting symbol " + symbolName + " from " + rplName);
		
		// Check if we are caching symbols
		if(enableSymbolCache){
			HashMap<String, ExportedSymbol> rplHashMap = symbolCache.get(rplName);
			
			// Check if we have the requested symbol cached
			if(rplHashMap != null){
				ExportedSymbol symbol = rplHashMap.get(symbolName);
				if(symbol != null){
					debug("Hit the cache!");
					// We do, so we are done
					return symbol;
				}
			}
		}
		
		// Ask the Wii U for a symbol
		sendCommand(Commands.GET_SYMBOL);
		
		// We need to buffer the strings to get the length
		ByteArrayOutputStream request = new ByteArrayOutputStream();
		
		// This length is static at least
		request.write(new byte[]{ 0x00, 0x00, 0x00, 0x08 });
		
		// This one is easy to get as well
		int length = rplName.length() + 9;
		request.write(new byte[]{ (byte) ((length >>> 24) & 0xFF), (byte) ((length >>> 16) & 0xFF), (byte) ((length >>> 8) & 0xFF), (byte) (length & 0xFF) });
		
		// Write the UTF-8 encoded null terminated names
		request.write(rplName.getBytes(StandardCharsets.UTF_8));
		request.write(0);
		request.write(symbolName.getBytes(StandardCharsets.UTF_8));
		request.write(0);
		
		// Write the size of our request
		writeByte((byte) request.size());
		
		// Write our request
		writeData(request.toByteArray());
		
		// Specify if this is a data symbol
		writeByte((byte) (isData ? 1 : 0));
		
		// Get returned address
		int address = readInteger();
		
		// Dereference pointer if we need to
		if(isPointer){
			address = peekInteger(address);
		}
		
		ExportedSymbol symbol = new ExportedSymbol(address, rplName, symbolName, this);
		
		// Cache the symbol if we need to
		if(enableSymbolCache){
			// This is slightly slower, but more compact
			// Speed should not be a problem, as it will get cached anyway
			if(!symbolCache.containsKey(rplName)){
				symbolCache.put(rplName, new HashMap<String, ExportedSymbol>());
			}
			
			// Since we know the symbol does not exist, we do not have to check for it
			symbolCache.get(rplName).put(symbolName, symbol);
		}
		
		return symbol;
	}
	


	/**
	 * Gets a symbol from the Wii U<br>
	 * Does the same thing as calling {@link com.wiiudev.tcpgecko.TcpGecko#getSymbol(String, String, boolean, boolean) getSymbol(rplName, symbolName, false, false)}
	 * 
	 * @param rplName The PRL the symbol refers to
	 * @param symbolName The name of the symbol
	 * 
	 * @return The requested symbol
	 * 
	 * @throws IOException If there was an error taking to the Wii U
	 * @throws WiiUException If something bad happens while communicating with the Wii U
	 * */
	public ExportedSymbol getSymbol(String rplName, String symbolName) throws IOException, WiiUException{
		return getSymbol(rplName, symbolName, false, false);
	}
	
	/**
	 * Gets the status of the Gecko handler installed on the Wii U
	 * 
	 * @return The current status
	 * 
	 * @throws IOException If there was an error talking to the Wii U
	 * */
	public Status getGeckoStatus() throws IOException{
		debug("Getting gecko status");
		sendCommand(Commands.GET_STATUS);
		return getStatus();
	}
	
	/**
	 * Finds an integer in the Wii U's memory
	 * 
	 * @param address The address to start the search at
	 * @param value The value to search for
	 * @param length The amount of memory to search
	 * 
	 * @throws IOException
	 * */
	public long memorySearch(long address, int value, int length) throws IOException{
		debug("Searching " + address + " to " + (address + length) + " for " + value);
		
		sendCommand(Commands.MEMORY_SEARCH_32);
		writeIntegers((int)address, value, length);
		int result = readInteger();
		return result & 0x00000000FFFFFFFFL;
	}
	
	/**
	 * Gets the console that is running the handler
	 * 
	 * @return Current console
	 * 
	 * @throws IOException If there was an error talking to the Wii U
	 * */
	public Console getVersion() throws IOException{
		debug("Getting version");
		sendCommand(Commands.GET_VERSION);
		return Console.getConsole(readByte());
	}
	
	/**
	 * Gets the OS version of the console
	 * 
	 * @return OS version
	 * 
	 * @throws IOException If there was an error talking to the Wii U
	 * */
	public int getOsVersion() throws IOException{
		debug("Getting OS version");
		sendCommand(Commands.GET_OS_VERSION);
		return readInteger();
	}
	
	/**
	 * Does nothing, put this here for sake of completeness
	 * 
	 * @throws IOException If there was an error talking to the Wii U
	 * */
	public void gcFail() throws IOException{
		debug("Sending GSFAIL");
		sendCommand(Commands.GCFAIL);
	}

	/**
	 * Clears a buffer<br>
	 * This seems slow to me...
	 * 
	 * @param buffer The buffer to clear
	 * */
	public void clearBuffer(byte[] buffer) {
		for(int i = 0; i < buffer.length; i++){
			buffer[i] = 0;
		}
	}

	/**
	 * Reads data from the Wii U
	 * 
	 * @param buffer Buffer to store the read data
	 * @param offset Offset into the buffer
	 * @param length Amount of data to read
	 * 
	 * @throws IOException If there was an error reading from the Wii U
	 * */
	public void readData(byte[] buffer, int offset, int length) throws IOException {
		debug("Reading data from the inputStream");
		
		int read;
		while(length > 0){
			// Since this only read the available data we need to make sure to read it all
			read = inputStream.read(buffer, offset, length);
			
			length -= read;
			offset += read;
		}
	}
	
	/**
	 * Reads data from the Wii U<br>
	 * Equivalent to calling readData(buffer, 0, buffer.length)
	 * 
	 * @param buffer Buffer to store the read data
	 * 
	 * @throws IOException If there was an error reading from the Wii U
	 * */
	public void readData(byte[] buffer) throws IOException{
		readData(buffer, 0, buffer.length);
	}
	
	/**
	 * Reads a byte from the Wii U
	 * 
	 * @return The read byte
	 * 
	 * @throws IOException If there was an error reading from the Wii U
	 */
	public byte readByte() throws IOException{
		byte[] buffer = new byte[1];
		readData(buffer);
		return buffer[0];
	}
	
	/**
	 * Reads a short from the Wii U
	 * 
	 * @return The read short
	 * 
	 * @throws IOException If there was an error reading from the Wii U
	 */
	public short readShort() throws IOException{
		// Shorts are two bytes
		byte[] buffer = new byte[2];
		
		readData(buffer);
		
		// Need to create a short from the data
		// The Wii U is big endian
		return (short) (((buffer[0] << 8) & 0xFF00) |
				(buffer[1] & 0x00FF));
	}
	
	/**
	 * Reads an integer from the Wii U
	 * 
	 * @return The read integer
	 * 
	 * @throws IOException If there was an error reading from the Wii U
	 */
	public int readInteger() throws IOException{
		// Integers are four bytes
		byte[] buffer = new byte[4];
		
		readData(buffer);
		
		// Need to create a integer from the data
		// The Wii U is big endian
		return (int) (((buffer[0] << 24) & 0xFF000000) |
				((buffer[1] << 16) & 0x00FF0000) |
				((buffer[2] <<  8) & 0x0000FF00) |
				(buffer[3] & 0x000000FF));
	}
	
	/**
	 * Reads a long from the Wii U
	 * 
	 * @return The read long
	 * 
	 * @throws IOException If there was an error reading from the Wii U
	 */
	public long readLong() throws IOException{
		// Longs are eight bytes
		byte[] buffer = new byte[8];
		
		readData(buffer);
		
		// Need to create a integer from the data
		// The Wii U is big endian
		return (long) (
				((((long)buffer[0]) << 56) & 0xFF00000000000000L) |
				((((long)buffer[1]) << 48) & 0x00FF000000000000L) |
				((((long)buffer[2]) << 40) & 0x0000FF0000000000L) |
				((((long)buffer[3]) << 32) & 0x000000FF00000000L) |
				((((long)buffer[4]) << 24) & 0x00000000FF000000L) |
				((((long)buffer[5]) << 16) & 0x0000000000FF0000L) |
				((((long)buffer[6]) <<  8) & 0x000000000000FF00L) |
				(( (long)buffer[7]       ) & 0x00000000000000FFL));
	}

	/**
	 * Gets the Status of the last command sent to the Wii U
	 * 
	 * @return The status, null if unknown
	 * 
	 * @throws IOException If there was an error reading from the Wii U
	 * */
	public Status getStatus() throws IOException {
		byte statusByte = readByte();
		return Status.getStatusFromByte(statusByte);
	}

	/**
	 * Writes data to the Wii U
	 * 
	 * @param buffer Data to write
	 * @param offset Offset into the buffer
	 * @param length Amount of data to write
	 * 
	 * @throws IOException If there was an error writing to the Wii U
	 * */
	public void writeData(byte[] buffer, int offset, int length) throws IOException{
		debug("Writing data to the outputStream");
		
		outputStream.write(buffer, offset, length);
	}
	
	/**
	 * Writes data to the Wii U<br>
	 * Equivalent to calling writeData(buffer, 0, buffer.length)
	 * 
	 * @param buffer Data to write
	 * 
	 * @throws IOException If there was an error writing to the Wii U
	 * */
	public void writeData(byte[] buffer) throws IOException{
		writeData(buffer, 0, buffer.length);
	}
	
	/**
	 * Writes a byte to the Wii U
	 * 
	 * @param value The byte to write
	 * 
	 * @throws IOException If there was an error writing to the Wii U
	 * */
	public void writeByte(byte value) throws IOException{
		writeData(new byte[]{ value });
	}
	
	/**
	 * Writes a short to the Wii U
	 * 
	 * @param value The short to write
	 * 
	 * @throws IOException If there was an error writing to the Wii U
	 * */
	public void writeShort(short value) throws IOException{
		byte[] buffer = new byte[4];
		buffer[0] = (byte)((value >>>  8) & 0x00FF);
		buffer[1] = (byte)( value         & 0x00FF);
		
		writeData(buffer);
	}
	
	/**
	 * Writes an integer to the Wii U
	 * 
	 * @param value The integer to write
	 * 
	 * @throws IOException If there was an error writing to the Wii U
	 * */
	public void writeInteger(int value) throws IOException{
		byte[] buffer = new byte[4];
		buffer[0] = (byte)((value >>> 24) & 0x000000FF);
		buffer[1] = (byte)((value >>> 16) & 0x000000FF);
		buffer[2] = (byte)((value >>>  8) & 0x000000FF);
		buffer[3] = (byte)( value         & 0x000000FF);
		
		writeData(buffer);
	}
	
	/**
	 * Writes a series of shorts to the Wii U
	 * 
	 * @param shorts The shorts to write
	 * 
	 * @throws IOException If there was an error writing to the Wii U
	 * */
	public void writeShorts(short ... shorts) throws IOException {
		for(short s : shorts){
			writeShort(s);
		}
	}
	
	/**
	 * Writes a series of integers to the Wii U
	 * 
	 * @param integers The integers to write
	 * 
	 * @throws IOException If there was an error writing to the Wii U
	 * */
	public void writeIntegers(int ... integers) throws IOException {
		for(int integer : integers){
			writeInteger(integer);
		}
	}

	/**
	 * Sends a command to the Wii U
	 * 
	 * @param command The command to send
	 * 
	 * @throws IOException If there was an error writing to the Wii U
	 * */
	public void sendCommand(Commands command) throws IOException {
		writeByte(command.value);
	}

	/**
	 * Checks if we can access a range of memory
	 * 
	 * @param address Starting address
	 * @param length Size of memory range
	 * @param access The access we wish to have
	 * 
	 * @return True if we can access the memory
	 * */
	public boolean validateMemoryAccess(long address, int length, MemoryAccess access) {
		long addressEnd =  address + length;
		
		if(0x01000000L <= address && addressEnd <= 0x01800000){
			return access == MemoryAccess.READ;
		}else if(0x0E000000L <= address && addressEnd <= 0x10000000L){
			return access == MemoryAccess.READ;
		}else if(0x10000000L <= address && addressEnd <= 0x50000000L){
			return true;
		}else if(0xE0000000L <= address && addressEnd <= 0xE4000000L){
			return access == MemoryAccess.READ;
		}else if(0xE8000000L <= address && addressEnd <= 0xEA000000L){
			return access == MemoryAccess.READ;
		}else if(0xF4000000L <= address && addressEnd <= 0xF6000000L){
			return access == MemoryAccess.READ;
		}else if(0xF6000000L <= address && addressEnd <= 0xF6800000L){
			return access == MemoryAccess.READ;
		}else if(0xF8000000L <= address && addressEnd <= 0xFB000000L){
			return access == MemoryAccess.READ;
		}else if(0xFB000000L <= address && addressEnd <= 0xFB800000L){
			return access == MemoryAccess.READ;
		}else if(0xFFFE0000L <= address && addressEnd <= 0xFFFFFFFFL){
			return true;
		}
		
		return false;
	}

	/**
	 * Checks if a memory range is valid
	 * 
	 * @param address Starting address
	 * @param length Size of memory range
	 * 
	 * @return True if the memory range is valid
	 * */
	public boolean validateMemoryRange(long address, int length) {
		long addressEnd =  address + length;
		
		if(0x01000000L <= address && addressEnd <= 0x01800000){
			return true;
		}else if(0x0E000000L <= address && addressEnd <= 0x10000000L){
			return true;
		}else if(0x10000000L <= address && addressEnd <= 0x50000000L){
			return true;
		}else if(0xE0000000L <= address && addressEnd <= 0xE4000000L){
			return true;
		}else if(0xE8000000L <= address && addressEnd <= 0xEA000000L){
			return true;
		}else if(0xF4000000L <= address && addressEnd <= 0xF6000000L){
			return true;
		}else if(0xF6000000L <= address && addressEnd <= 0xF6800000L){
			return true;
		}else if(0xF8000000L <= address && addressEnd <= 0xFB000000L){
			return true;
		}else if(0xFB000000L <= address && addressEnd <= 0xFB800000L){
			return true;
		}else if(0xFFFE0000L <= address && addressEnd <= 0xFFFFFFFFL){
			return true;
		}
		
		return false;
	}

	/**
	 * Checks if we are caching RPC symbols
	 * 
	 * @return Are we caching symbols?
	 * */
	public boolean isSymbolCacheEnabled(){
		return enableSymbolCache;
	}
	
	/**
	 * Sets weather or not we should cache symbols<br>
	 * If false the cache is cleared automatically
	 * */
	public void setSymbolCacheEnabled(boolean enabled){
		enableSymbolCache = enabled;
		
		if(!enabled){
			clearSymbolCache();
		}
	}
	
	/**
	 * Clears the symbol cache
	 * */
	public void clearSymbolCache(){
		for(String key : symbolCache.keySet()){
			symbolCache.get(key).clear();
		}
		symbolCache.clear();
	}
	
	@Override
	public void close() throws IOException {
		debug("Cleaning up, goodbye!");
		
		filesystemInstance.close();
		coreInitInstance.close();
		
		inputStream.close();
		outputStream.close();
		socket.close();
	}

	public CoreInit getCoreInit() {
		return coreInitInstance;
	}
	
	public Filesystem getFilesystem(){
		return filesystemInstance;
	}

	/**
	 * Helper method<br>
	 * calls {@link com.wiiudev.tcpgecko.TcpGecko#getSymbol(String, String) getSymbol(rplName, symbolName)} then {@link com.wiiudev.tcpgecko.ExportedSymbol#call(int...) call(params)}
	 * 
	 * @throws WiiUException 
	 * @throws IOException 
	 * */
	public long getAndCallSymbol(String rplName, String symbolName, int ... params) throws IOException, WiiUException {
		ExportedSymbol symbol = getSymbol(rplName, symbolName);
		return symbol.call(params);
	}
	
	/**
	 * Helper method<br>
	 * calls {@link com.wiiudev.tcpgecko.TcpGecko#getSymbol(String, String) getSymbol("coreinit.rpl", symbolName)} then {@link com.wiiudev.tcpgecko.ExportedSymbol#call(int...) call(params)}
	 * 
	 * @throws WiiUException 
	 * @throws IOException 
	 * */
	public long getAndCallSymbol(String symbolName, int ... params) throws IOException, WiiUException {
		ExportedSymbol symbol = getSymbol("coreinit.rpl", symbolName);
		return symbol.call(params);
	}
	
	private void debug(String string) {
		logger.log(Level.INFO, string);
	}
	
	/**
	 * Sets the logging {@link java.util.logging.Level level} of this instance<br>
	 * Default level is {@link java.util.logging.Level#WARNING warning}
	 * 
	 * @param level The {@link java.util.logging.Level level} to log
	 * */
	public void setLoggingLevel(Level level){
		logger.setLevel(level);
	}

	/**
	 * Adds a new {@link java.util.logging.Handler handler} to the logger
	 * 
	 * @param handler The {@link java.util.logging.Handler handler} to add
	 * */
	public void addLoggerHandler(Handler handler) {
		logger.addHandler(handler);
	}
}

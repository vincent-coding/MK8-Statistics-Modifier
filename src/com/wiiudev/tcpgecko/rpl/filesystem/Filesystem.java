package com.wiiudev.tcpgecko.rpl.filesystem;

import java.io.Closeable;
import java.io.IOException;

import com.wiiudev.tcpgecko.TcpGecko;
import com.wiiudev.tcpgecko.WiiUException;
import com.wiiudev.tcpgecko.enumeration.FSErrorHandling;
import com.wiiudev.tcpgecko.enumeration.FSStatus;
import com.wiiudev.tcpgecko.memory.IRemoteBuffer;
import com.wiiudev.tcpgecko.rpl.CoreInit;
import com.wiiudev.tcpgecko.rpl.FSDirHandle;

/**
 * Methods relevant to file IO on the Wii U<br>
 * This includes save data<br>
 * <br>
 * This must be created after {@link com.wiiudev.tcpgecko.rpl.CoreInit CoreInit}, as it uses {@link com.wiiudev.tcpgecko.rpl.CoreInit CoreInit}
 * 
 * @author gudenau
 * */
public class Filesystem implements Closeable {
	private final TcpGecko gecko;
	private final CoreInit coreInit;
	
	public Filesystem(TcpGecko gecko){
		this.gecko = gecko;
		this.coreInit = gecko.getCoreInit();
	}

	/**
	 * Initialize FS library
	 * */
	public void FSInit() throws IOException, WiiUException{
		gecko.getAndCallSymbol("FSInit");
	}
	
	/**
	 * Shutdown FS library
	 * */
	public void FSShutdown() throws IOException, WiiUException{
		gecko.getAndCallSymbol("FSShutdown");
	}
	
	/**
	 * Registers a new client
	 * 
	 * @param client Client to register
	 * @param errors Error handling info
	 * 
	 * @return Result code
	 * */
	public FSStatus FSAddClient(FSClient client, FSErrorHandling ... errors) throws IOException, WiiUException{
		if(client.getInitalized()){
			return FSStatus.OK;
		}
		
		int errorMask = getErrorMask(errors);
		
		IRemoteBuffer buffer = client.getBuffer();
		
		if(buffer == null){
			buffer = coreInit.mallocHeap(0x1700, 0x20, false);
		}
		
		long result = gecko.getAndCallSymbol("FSAddClient", buffer.getAddress(), errorMask);
		client.setInitalized(true);
		
		return FSStatus.getStatus((int) result);
	}
	
	/**
	 * Unregister a client
	 * 
	 * @param client Client to unregister
	 * @param errors Error handling info
	 * 
	 * @return Result code
	 * */
	public FSStatus FSDelClient(FSClient client, FSErrorHandling ... errors) throws IOException, WiiUException{
		if(!client.getInitalized()){
			return FSStatus.OK;
		}
		
		IRemoteBuffer buffer = client.getBuffer();
		
		if(buffer == null){
			throw new IllegalArgumentException("FSClient did not have a buffer?");
		}
		
		int errorMask = getErrorMask(errors);

		long result = gecko.getAndCallSymbol("FSDelClient", buffer.getAddress(), errorMask);
		client.setInitalized(false);
		return FSStatus.getStatus((int) result);
	}
	
	/**
	 * Gets number of registered clients
	 * 
	 * @return Number of registered clients
	 * */
	public int FSGetClientNum() throws IOException, WiiUException{
		return (int) gecko.getAndCallSymbol("FSGetClientNum");
	}
	
	/**
	 * Initializes the command block.
	 * 
	 * @param block Command block
	 * */
	public void FSInitCmdBlock(FSCmdBlock block) throws IOException, WiiUException{
		if(block.getInitalized()){
			return;
		}
		
		IRemoteBuffer buffer = block.getBuffer();

		if(buffer == null){
			buffer = coreInit.mallocHeap(0x800, 0x20, false);
			block.setBuffer(buffer);
		}
		
		gecko.getAndCallSymbol("FSInitCmdBlock", buffer.getAddress());
		
		block.setInitalized(true);
	}
	
	/**
	 * Cancels specified waiting command block
	 * 
	 * @param client Client
	 * @param block Command block
	 * */
	public void FSCancelCommand(FSClient client, FSCmdBlock block) throws IOException, WiiUException{
		if(!client.getInitalized()){
			throw new IllegalArgumentException("Client was not initzlized!");
		}
		if(!block.getInitalized()){
			throw new IllegalArgumentException("Command block was not initzlized!");
		}
		
		gecko.getAndCallSymbol("FSCancelCommand", client.getBuffer().getAddress(), block.getBuffer().getAddress());
	}
	
	/**
	 * Cancels all waiting commands for a client
	 * 
	 * @param client
	 * */
	public void FSCancelAllCommands(FSClient client) throws IOException, WiiUException{
		if(!client.getInitalized()){
			throw new IllegalArgumentException("Client was not initzlized!");
		}
		
		gecko.getAndCallSymbol("FSCancelCommand", client.getBuffer().getAddress());
	}
	
	public void FSSetUserData(FSCmdBlock block, int userData) throws IOException, WiiUException{
		//gecko.getAndCallSymbol("FSSetUserData", block.getBuffer(), userData);
	}
	
	public int FSGetUserData(FSCmdBlock block) throws IOException, WiiUException{
		//return (int)gecko.getAndCallSymbol("FSGetUserData", block.getBuffer());
		return 0;
	}

	public FSStatus FSOpenDir(FSClient client, FSCmdBlock block, String path, FSDirHandle handle, FSErrorHandling ... errors) throws IOException, WiiUException{
		//int pathBuffer = coreInit.createString(path);
		int errorMask = getErrorMask(errors);
		
		int buffer;
		if(handle.hasBuffer()){
			buffer = handle.getBuffer();
		}else{
			//buffer = coreInit.malloc(4, 4);
			//handle.setBuffer(buffer);
		}
		
		//long returnStatus = gecko.getAndCallSymbol("FSOpenDir", client.getBuffer(), block.getBuffer(), pathBuffer, handle.getBuffer(), errorMask);
		
		//coreInit.free(pathBuffer);
		
		//return FSStatus.getStatus((int) returnStatus);
		return FSStatus.ACCESS_ERROR;
	}
	
	public FSStatus FSCloseDir(FSClient client, FSCmdBlock block, FSDirHandle handle, FSErrorHandling ... errors) throws IOException, WiiUException{
		int errorMask = getErrorMask(errors);
		
		int buffer;
		if(handle.hasBuffer()){
			buffer = handle.getBuffer();
		}else{
			//buffer = coreInit.malloc(4, 4);
			//handle.setBuffer(buffer);
		}
		
		//long returnStatus = gecko.getAndCallSymbol("FSCloseDir", client.getBuffer(), block.getBuffer(), handle.getBuffer(), errorMask);
		
		//return FSStatus.getStatus((int) returnStatus);
		return FSStatus.ACCESS_ERROR;
	}
	
	/**
	 * Simple helper method to get error mask
	 * */
	private int getErrorMask(FSErrorHandling ... errors){
		int errorMask = 0;
		for(FSErrorHandling error : errors){
			errorMask |= error.value;
		}
		return errorMask;
	}
	
	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		
	}
}

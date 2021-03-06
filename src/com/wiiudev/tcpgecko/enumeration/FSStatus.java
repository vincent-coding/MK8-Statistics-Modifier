package com.wiiudev.tcpgecko.enumeration;

public enum FSStatus {
	OK              (    0),
	CANCELED        (   -1),
	END             (   -2),
	MAX             (   -3),
	ALREADY_OPEN    (   -4),
	EXISTS          (   -5),
	NOT_FOUND       (   -6),
	NOT_FILE        (   -7),
	NOT_DIR         (   -8),
	ACCESS_ERROR    (   -9),
	PERMISSION_ERROR(  -10),
	FILE_TOO_BIG    (  -11),
	STRORAGE_FULL   (  -12),
	JOURNAL_FULL    (  -13),
	UNSUPPORTED_CMD (  -14),
	MEDIA_NOT_READY (  -15),
	INVALID_MEDIA   (  -16),
	MEDIA_ERROR     (  -17),
	DATA_CORRUPTED  (  -18),
	WRITE_PROTECTED (  -19),
	FATAL_ERROR     (-1024);
	
	public final int value;
	FSStatus(int value){
		this.value = value;
	}
	
	public static FSStatus getStatus(int value){
		for(FSStatus status : values()){
			if(status.value == value){
				return status;
			}
		}
		
		return null;
	}
}

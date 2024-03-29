package com.server.main;

import java.util.UUID;

public class SessionID {

	private String ID = "";
	private byte[] byteID;
	
	public SessionID(byte[] sessionID) {
		this(new String(sessionID));
		this.byteID = sessionID;
	}
	
	public SessionID(int sessionID) {
		this(String.valueOf(sessionID));
	}
	
	public SessionID(String sessionID) {
		this.ID = sessionID;
		this.byteID = sessionID.getBytes();
	}
	
	public SessionID(SessionID sessionID) {
		this(sessionID.getByteID());
	}
	
	public SessionID(UUID sessionID) {
		this(sessionID.toString());
	}
	
	public static SessionID getSessionIDAsUUID() {
		return new SessionID(UUID.randomUUID());
	}

	public String getID() {
		return ID;
	}
	
	public byte[] getByteID() {
		return this.byteID;
	}
	
	public SessionID clone() {
		return new SessionID(this);
	}
	
	@Override
	public String toString() {
		return this.ID;
	}
	
	@Override
	public boolean equals(Object other) {
		if(other instanceof SessionID) {
			return ((SessionID)other).getID().equals(this.getID());
		}else if(other instanceof String) {
			return ((String)other).equals(this.getID());
		}
		return false;
	}
	
}

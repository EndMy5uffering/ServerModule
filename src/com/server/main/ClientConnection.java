package com.server.main;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

import com.logger.Level;
import com.server.packageing.DataPackage;
import com.server.packageing.PackageInfo;
import com.server.packageing.PackageManager;

enum State{
	Active,
	Idle,
	Suspended,
	Dead
}

public class ClientConnection{

	private Socket socket;
	
	private Thread clientThread;

	private State state;
	
	private OutputStream out;
	private InputStream reader;
	
	private ClientCallBack callback = null;
	
	public ClientConnection(Socket socket, int timeout, ClientCallBack callback) {
		if(socket == null) {
			this.state = State.Dead;
		}
		this.callback = callback;
		this.socket = socket;
		
		try {
			out = socket.getOutputStream();
			this.reader = socket.getInputStream();
		} catch (IOException e) {
			out = null;
			this.reader = null;
			Server.logger.log(Level.ERROR, e, e.getClass());
		}
		
		if(timeout >= 0) {
			try {
				this.socket.setSoTimeout(timeout);
			} catch (SocketException e) {
				Server.logger.log(Level.ERROR, e, e.getClass());
			}
		}
		
		this.state = State.Active;
	}
	
	public ClientConnection(Socket socket, int timeout){
		this(socket, timeout, null);
	}
	
	public ClientConnection(Socket socket){
		this(socket, -1, null);
	}
	
	public void enable() {
		this.clientThread = new Thread(() -> {
			if(this.state != State.Active) return;
			try {
				if(reader == null) return;
				while(this.state == State.Active) {
					DataPackage dataOut = null;
					byte[] data = new byte[DataPackage.IDLENGTH];
					
					reader.read(data);
					PackageInfo info = PackageManager.getPackageInfo(data);
					
					if(info == null) {
						Server.logger.log(Level.ERROR, "Unknown package recived by: " + socket.getInetAddress().toString());
						Server.logger.log(Level.ERROR, "Unknown package id: " + data[0] + data[1]);
						disable();
					}
					if(!info.isDynamicLength()) {
						
						byte[] rawData = new byte[info.getLength()];
						reader.read(rawData);
						dataOut = info.getConstruct().build(info.getLength(), info.isDynamicLength(), rawData);
					
					}else {
						
						byte[] rawData = new byte[info.getLength()];
						reader.read(rawData);
						int length = DataPackage.getIntFromByte(rawData);
						if(length >= 0 && length < Server.getMaxPackageSize()) {
							rawData = new byte[length];
							reader.read(rawData);
						}else if(length > Server.getMaxPackageSize() || length < 0) {
							Server.logger.log(Level.ERROR, "Size missmatch packages have to be larger or equal to 0 and smaller to max package length!");
							break;
						}
					
						dataOut = info.getConstruct().build(info.getLength(), info.isDynamicLength(), rawData);
					
					}
					
					if(callback != null && dataOut != null) callback.call(dataOut);
					if(dataOut != null && info.getCallback() != null) info.getCallback().call(dataOut);
				}
			} catch (IOException e) {
				Server.logger.log(Level.ERROR, e, e.getClass());
			}
			disable();
		});
		
		this.clientThread.start();
	}
	
	public void send(DataPackage data) {
		if(out == null) return;
		if(this.state == State.Active) {
			try {
				out.write(data.pack());
				out.flush();
			} catch (IOException e) {
				Server.logger.log(Level.ERROR, e, e.getClass());
				disable();
			}
		}
	}
	
	public void disable(Level level, String log) {
		Server.logger.log(level, log);
		disable();
	}
	
	public void disable() {
		Server.logger.log(Level.INFO, "Disableing connection for: " + socket.getInetAddress().toString());
		this.state = State.Dead;
		try {
			if(socket != null) this.socket.close();
			if(reader != null) this.reader.close();
			if(out != null) this.out.close();
		} catch (IOException e) {
			Server.logger.log(Level.ERROR, e, e.getClass());
		}
		ClientManager.removeClient(this);
	}

	public ClientCallBack getCallback() {
		return callback;
	}

	public void setCallback(ClientCallBack callback) {
		this.callback = callback;
	}
}

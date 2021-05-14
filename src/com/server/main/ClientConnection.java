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
			//TODO: logger
			Server.logger.log(Level.ERROR, e, e.getClass());
		}
		
		if(timeout >= 0) {
			try {
				this.socket.setSoTimeout(timeout);
			} catch (SocketException e) {
				//TODO: logger
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
					if(info != null && !info.isDynamicLength()) {
						byte[] rawData = new byte[info.getLength()];
						reader.read(rawData);
						dataOut = info.getConstruct().build(info.getLength(), info.isDynamicLength(), rawData);
					}else if(info != null && info.isDynamicLength()){
						byte[] rawData = new byte[info.getLength()];
						reader.read(rawData);
						int length = DataPackage.getLengthFromByte(rawData);
						rawData = new byte[length];
						reader.read(rawData);
						dataOut = info.getConstruct().build(info.getLength(), info.isDynamicLength(), rawData);
					}
					
					if(callback != null && dataOut != null) callback.call(dataOut);
				}
			} catch (IOException e) {
				//TODO: logger
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
				//TODO: logger
				Server.logger.log(Level.ERROR, e, e.getClass());
			}
		}
	}
	
	public void disable() {
		this.state = State.Dead;
		try {
			if(socket != null) this.socket.close();
			if(reader != null) this.reader.close();
			if(out != null) this.out.close();
		} catch (IOException e) {
			//TODO: logger
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

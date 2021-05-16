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
	Dead
}

public class ClientConnection{

	private Socket socket;
	private Server server;
	private PackageManager packageManager;
	
	private Thread clientThread;

	private State state;
	
	private OutputStream out;
	private InputStream reader;
	
	private ClientCallBack callback = null;
	
	public ClientConnection(Socket socket, Server server, PackageManager packageManager, int timeout, ClientCallBack callback) {
		if(socket == null) {
			this.state = State.Dead;
		}
		this.packageManager = packageManager;
		this.callback = callback;
		this.socket = socket;
		this.server = server;
		
		try {
			out = socket.getOutputStream();
			this.reader = socket.getInputStream();
		} catch (IOException e) {
			out = null;
			this.reader = null;
			Server.logger.log(Level.ERROR, e, e.getClass());
		}
		
		if(timeout > 0) {
			try {
				this.socket.setSoTimeout(timeout);
			} catch (SocketException e) {
				Server.logger.log(Level.ERROR, e, e.getClass());
			}
		}
		
		this.state = State.Active;
	}
	
	public ClientConnection(Socket socket, Server server, PackageManager packageManager, int timeout){
		this(socket, server, packageManager, timeout, null);
	}
	
	public ClientConnection(Socket socket, Server server, PackageManager packageManager){
		this(socket, server, packageManager, -1, null);
	}
	
	/**
	 * Enables the client connection and starts the client thread.<br>
	 * When enabled the client connection will listen to incoming packages.<br>
	 * All received packages are relayed back to the server or the API via the callback functions.<br>
	 * All packages can define there on callback function or the default server callback can be used.<br>
	 * <br>
	 * 
	 * <b>The client will be automatically disconnected when a faulty package is received.</b>
	 * 
	 * */
	public void enable() {
		this.clientThread = new Thread(() -> {
			if(this.state != State.Active) return;
			try {
				if(reader == null) return;
				while(this.state == State.Active) {
					DataPackage dataOut = null;
					byte[] data = new byte[DataPackage.IDLENGTH];
					
					reader.read(data);
					PackageInfo info = this.packageManager.getPackageInfo(data);
					
					if(info == null) {
						Server.logger.log(Level.ERROR, "Unknown package recived by: " + socket.getInetAddress().toString());
						Server.logger.log(Level.ERROR, "Unknown package id: " + data[0] + data[1]);
						disable();
						return;
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
					
					if(callback != null && dataOut != null) callback.call(dataOut, this);
					if(dataOut != null && info.getCallback() != null) info.getCallback().call(dataOut, this);
				}
			} catch (IOException e) {
				Server.logger.log(Level.ERROR, e, e.getClass());
			}
			disable();
		});
		
		this.clientThread.start();
	}
	
	/**
	 * Will send a data package to the client that is connected.<br>
	 * 
	 * @param data
	 * */
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
	
	/**
	 * Disable function that can log an error if the connection had to be disabled in a catch block.
	 * */
	public void disable(Level level, String log) {
		Server.logger.log(level, log);
		disable();
	}
	
	/**
	 * Disables the client connection and stops all the in and output streams and removes the connection form the manager.
	 * */
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
		this.server.getClientManager().removeClient(this);
	}

	/**
	 * Returns the default callback set by the server when the object was created.
	 * */
	public ClientCallBack getCallback() {
		return callback;
	}

	/**
	 * Sets the default callback for the connection.
	 * */
	public void setCallback(ClientCallBack callback) {
		this.callback = callback;
	}

	public PackageManager getPackageManager() {
		return packageManager;
	}

	public void setPackageManager(PackageManager packageManager) {
		this.packageManager = packageManager;
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	public Socket getSocket() {
		return socket;
	}

	public Server getServer() {
		return server;
	}
}

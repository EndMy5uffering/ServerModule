package com.server.main;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketException;

enum State{
	Active,
	Idle,
	Dead
}

public class ClientConnection{

	private Socket socket;
	
	private Thread clientThread;

	private State state;
	
	private OutputStreamWriter writer;
	
	private OutputStream out;
	
	private ClientCallBack callback = null;
	
	public ClientConnection(Socket socket, int timeout, ClientCallBack callback) {
		if(socket == null) {
			this.state = State.Dead;
		}
		this.callback = callback;
		this.socket = socket;
		
		try {
			out = socket.getOutputStream();
			writer = new OutputStreamWriter(out);
		} catch (IOException e1) {
			out = null;
			writer = null;
			//TODO: logger
		}
		
		if(timeout >= 0) {
			try {
				this.socket.setSoTimeout(timeout);
			} catch (SocketException e) {
				//TODO: logger
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
				//TODO: readInput
				while(this.state == State.Active) {
					byte[] data = new byte[1024];
					
					socket.getInputStream().read(data);
					
					if(callback != null) callback.call(new DataPackage(data));
				}
			} catch (IOException e) {
				//TODO: logger
			}
		});
		
		this.clientThread.start();
	}
	
	public void send(DataPackage data) {
		if(out == null) throw new NullPointerException("Output stream of socket was null!");
		try {
			out.write(data.getByteData());
			out.flush();
		} catch (IOException e) {
			//TODO: logger
		}
	}
	
	public void disable() {
		this.state = State.Dead;
		try {
			this.socket.close();
		} catch (IOException e) {
			//TODO: logger
		}
	}

	public ClientCallBack getCallback() {
		return callback;
	}

	public void setCallback(ClientCallBack callback) {
		this.callback = callback;
	}
}

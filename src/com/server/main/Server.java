package com.server.main;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.logger.Level;
import com.logger.Logger;
import com.logger.PrintMode;
import com.logger.PrintingType;

public class Server {

	public static Logger logger = new Logger(PrintingType.Console, PrintMode.Event);
	
	private final int port;
	
	private ServerSocket serverSocket;
	
	private Thread serverThread;
	
	private ClientCallBack callback = null;
	
	private int clientTimeOut = -1;
	private int defaultErrorOut = 500;
	
	private boolean IsAlive = true;
	
	private static int maxPackageSize = 2048;
	
	public Server(int port) {
		this.port = port;
	}
	
	public void start() throws NullPointerException {
		
		logger.log(Level.INFO, "Starting server");
		
		try {
			logger.log(Level.INFO, "Binding server to port");
			serverSocket = new ServerSocket(this.port);
		} catch (IOException e) {
			logger.log(Level.ERROR, "Could not bind server to port!", e.getClass());
			return;
		}
		if(serverSocket == null)
			throw new NullPointerException("Server was null user serverInit() first!");
		
		this.serverThread = new Thread(() -> {
			int ErrorOut = defaultErrorOut;
			while(IsAlive) {
				ClientConnection newConnection;
				try {
					logger.log(Level.INFO, "Waiting for connection");
					Socket s = serverSocket.accept();
					logger.log(Level.INFO, "Client connecting " + s.getInetAddress().getHostAddress());
					if(s != null) {
						newConnection = new ClientConnection(s, this.clientTimeOut, callback);
						ClientManager.submit(newConnection);
					}
					ErrorOut = defaultErrorOut;
				} catch (IOException e) {
					if(IsAlive) {
						logger.log(Level.ERROR, e, e.getClass());
						--ErrorOut;
						if(ErrorOut <= 0) stopServer();
					}
				}
			}
		});
		
		this.serverThread.start();
		
	}
	
	public void stopServer() {
		try {
			if(serverSocket != null) serverSocket.close();
			this.IsAlive = false;
			logger.log(Level.INFO, "Stopping server");
		} catch (IOException e) {
			logger.log(Level.ERROR, e, e.getClass());
		}
	}
	
	public void setDefaultClientCallBack(ClientCallBack callback) {
		this.callback = callback;
	}
	
	public void setDefualtClientTimeOut(int timeOut) {
		this.clientTimeOut = timeOut;
	}

	public Logger getLogger() {
		return logger;
	}

	public void setPrintMode(PrintMode mode) {
		logger.setMode(mode);
	}

	public static int getMaxPackageSize() {
		return Server.maxPackageSize;
	}

	public static void setMaxPackageSize(int maxPackageSize) {
		Server.maxPackageSize = maxPackageSize;
	}
	
}

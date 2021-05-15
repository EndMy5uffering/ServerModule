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
	
	
	/**
	 * Starts the server thread.
	 * */
	public void start() {
		
		logger.log(Level.INFO, "Starting server");
		
		try {
			logger.log(Level.INFO, "Binding server to port");
			serverSocket = new ServerSocket(this.port);
		} catch (IOException e) {
			logger.log(Level.ERROR, "Could not bind server to port!", e.getClass());
			return;
		}
		
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
	
	/**
	 * Disables the server and all the client connections that are currently active.
	 * */
	public void stopServer() {
		try {
			ClientManager.stopClients();
			if(serverSocket != null) serverSocket.close();
			this.IsAlive = false;
			logger.log(Level.INFO, "Stopping server");
		} catch (IOException e) {
			logger.log(Level.ERROR, e, e.getClass());
		}
	}
	
	/**
	 * Sets the default callback function that is executed when the server receives a package. <br>
	 * This callback is executed for every package that is received.
	 * 
	 * @param callback The callback function that will be executed.
	 * */
	public void setDefaultClientCallBack(ClientCallBack callback) {
		this.callback = callback;
	}
	
	/**
	 * Timeout value for the client connection that is set by the server when a connection is made.<br>
	 * <br><u>No timeout is set for values <b>smaller</b> or <b>equal</b> to <b>0</b></u>
	 * 
	 * @param timeOut
	 * */
	public void setDefualtClientTimeOut(int timeOut) {
		this.clientTimeOut = timeOut;
	}

	/**
	 * Gets the logger object.
	 * The logger is a subsystem that logs everything to the console or a log file.
	 * */
	public Logger getLogger() {
		return logger;
	}

	/**
	 * Sets the print mode for the logging system.
	 * Possible print modes:
	 * 
	 * <pre>	Debug		Logs all events with debug info</pre>
	 * <pre>	DebugDetaild	Logs all events with debug info with colors</pre>
	 * <pre>	Event		Logs only necessary info</pre>
	 * <pre>	EventDetaild	Logs only necessary info with colors</pre>
	 * 
	 * Currently only <b>Debug</b> and <b>Event</b> are working.<br>
	 * <br>
	 * @param mode
	 * */
	public void setPrintMode(PrintMode mode) {
		logger.setMode(mode);
	}

	/**
	 * Returns the maximum size of a package.<br>
	 * <b>This dose not include the package header only the body.</b>
	 * */
	public static int getMaxPackageSize() {
		return Server.maxPackageSize;
	}

	/**
	 * Sets the maximum size of a package.<br>
	 * <b>This only refers to the package body.</b>
	 * */
	public static void setMaxPackageSize(int maxPackageSize) {
		Server.maxPackageSize = maxPackageSize;
	}
	
}

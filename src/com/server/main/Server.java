package com.server.main;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import com.logger.Level;
import com.logger.Logger;
import com.logger.PrintMode;
import com.logger.PrintingType;
import com.server.packageing.ClientConnectCallback;
import com.server.packageing.DataPackage;
import com.server.packageing.DefaultPackageManager;
import com.server.packageing.PackageManager;
import com.server.packageing.PackageRegistrationManager;

public class Server {

	public static Logger logger = new Logger(PrintingType.Console, PrintMode.Event);
	
	private final ClientManager clientManager;
	private PackageManager defaultPackageManager;
	private final PackageRegistrationManager packageRegistrationManager;
	
	private final int port;
	
	private ServerSocket serverSocket;
	
	private Thread serverThread;
	
	private List<ClientPackageReceiveCallback> callback = new ArrayList<ClientPackageReceiveCallback>();
	private ClientConnectCallback clientConnectCallback= null;
	
	private int clientTimeOut = -1;
	private int defaultErrorOut = 500;
	
	private boolean IsAlive = true;
	
	private static int maxPackageSize = 2048;
	
	public Server(int port) {
		this(port, null);
	}
	
	public Server(int port, PackageRegistrationManager packageRegistrationManager) {
		this.port = port;
		this.clientManager = new ClientManager();
		if(packageRegistrationManager != null) {
			this.packageRegistrationManager = packageRegistrationManager;
		}else {
			this.packageRegistrationManager = new PackageRegistrationManager();
		}
		this.defaultPackageManager = new DefaultPackageManager(this);
	}
	
	
	/**
	 * Starts the server thread.<br>
	 * All changes to the server settings have to happen before this function call.
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
					Socket s = serverSocket.accept();
					logger.log(Level.INFO, "Client connecting " + s.getInetAddress().getHostAddress());
					if(s != null) {
						newConnection = new ClientConnection(s, this, this.defaultPackageManager, this.clientTimeOut);
						newConnection.setClientPackageReceiveCallback(callback);
						this.clientManager.submit(newConnection);
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
			this.clientManager.stopClients();
			if(serverSocket != null) serverSocket.close();
			this.IsAlive = false;
			logger.log(Level.INFO, "Stopping server");
		} catch (IOException e) {
			logger.log(Level.ERROR, e, e.getClass());
		}
	}
	
	/**
	 * Sends a data package to all connected clients.
	 * 
	 * */
	public void sendToAllClients(DataPackage data) {
		this.clientManager.sendToClient(data);
	}
	
	/**
	 * Sets the default callback function that is executed when the server receives a package. <br>
	 * This callback is executed for every package that is received.
	 * 
	 * @param callback The callback function that will be executed.
	 * */
	public void addDefaultClientPackageReceiveCallback(ClientPackageReceiveCallback... callback) {
		if(callback != null)
			for(ClientPackageReceiveCallback e : callback)
				this.callback.add(e);
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

	public ClientManager getClientManager() {
		return clientManager;
	}

	public PackageRegistrationManager getPackageRegistrationManager() {
		return packageRegistrationManager;
	}

	public PackageManager getDefaultPackageManager() {
		return defaultPackageManager;
	}

	public int getPort() {
		return port;
	}

	public void setDefaultPackageManager(PackageManager defaultPackageManager) {
		this.defaultPackageManager = defaultPackageManager;
	}

	public ClientConnectCallback getClientConnectCallback() {
		return clientConnectCallback;
	}

	public void setClientConnectCallback(ClientConnectCallback clientConnectCallback) {
		this.clientConnectCallback = clientConnectCallback;
	}
	
}

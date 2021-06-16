package com.server.asyncworker;

import java.util.ArrayList;

import com.logger.Level;
import com.server.main.Server;

public class Timer implements Runnable{

	private boolean running = true;
	private AsyncTask task = null;
	private long period = 0;
	private Thread workerThread;
	private ArrayList<Tickable> listenderObjects = new ArrayList<>();
	
	/**
	 * Creates a new time that runs every <b>period</b> milliseconds.<br>
	 * The timer is not running by default. The start() method must be called to start the timer.<br>
	 * <br>
	 * Timings: 1000ms = 1s<br>
	 * <br>
	 * Execution:<br>
	 * 1. Timer task<br>
	 * 2. Listening objects<br>
	 * 
	 * @param period Sets the interval for the timer to tick in milliseconds.
	 * */
	public Timer(long period) {
		this(period, null, false);
	}
	
	/**
	 * Creates a new time that runs every <b>period</b> milliseconds.<br>
	 * The timer can be set to run when created by specifying <b>true</b> as the second parameter.<br>
	 * <br>
	 * Timings: 1000ms = 1s<br>
	 * <br>
	 * Execution:<br>
	 * 1. Timer task<br>
	 * 2. Listening objects<br>
	 * 
	 * @param period Sets the interval for the timer to tick in milliseconds.
	 * @param isRunning Sets the default running state of the timer when created. If set to true the timer will start running when created.
	 * */
	public Timer(long period, boolean isRunning) {
		this(period, null, isRunning);
	}
	
	/**
	 * Creates a new time that runs every <b>period</b> milliseconds.<br>
	 * The given task can be run by the timer every tick.<br>
	 * <br>
	 * Timings: 1000ms = 1s<br>
	 * <br>
	 * Execution:<br>
	 * 1. Timer task<br>
	 * 2. Listening objects<br>
	 * 
	 * @param period Sets the interval for the timer to tick in milliseconds.
	 * @param task Sets a task for the timer to execute asynchronous.
	 * */
	public Timer(long period, AsyncTask task) {
		this(period, task, false);
	}
	
	/**
	 * Creates a new time that runs every <b>period</b> milliseconds.<br>
	 * The timer can be set to run when created by specifying <b>true</b> as the third parameter.<br>
	 * The given task can be run by the timer every tick.<br>
	 * <br>
	 * Timings: 1000ms = 1s<br>
	 * 
	 * @param period Sets the interval for the timer to tick in milliseconds.
	 * @param task Sets a task for the timer to execute asynchronous.
	 * @param isRunning Sets the default running state of the timer when created. If set to true the timer will start running when created.
	 * */
	public Timer(long period, AsyncTask task, boolean isRunning) {
		this.period = (period < 0 ? 0 : period);
		this.task = task;
		this.running = isRunning;
		this.workerThread = new Thread(this);
		if(isRunning) this.start();
	}

	@Override
	public void run() {
		while(this.running) {
			try {
				Thread.sleep(period);
			} catch (InterruptedException e) {
				Server.getLogger().log(Level.WARNING, "Timer was interuped!");
			}
			
			if(this.task != null)
				this.task.call();
			
			listenderObjects.forEach(x -> x.tick());
		}
	}
	
	/**
	 * This will stop the timer after the last execution has finished.<br>
	 * */
	public void stop() {
		this.running = false;
		try {
			this.workerThread.join();
		} catch (InterruptedException e) {
			Server.getLogger().log(Level.WARNING, "Timer was interuped while stopping!");
		}
	}
	
	/**
	 * This will start the timers internal thread.<br>
	 * The timer can be stopped with the <b>stop()</b> function.
	 * */
	public void start() {
		this.running = true;
		this.workerThread.start();
	}
	
	/**
	 * This will return a new thread that sets <b>this</b> timer as the runnable.<br>
	 * The thread can then be managed external.<br>
	 * <br>
	 * The <b>start()</b> and <b>stop()</b> functions will no longer work when using an external thread.
	 * */
	public Thread toNewThread() {
		return new Thread(this);
	}

	/**
	 * Will return the current state of the Timer.
	 * */
	public boolean isRunning() {
		return running;
	}
	
	/**
	 * Sets a task for the timer to execute.
	 * */
	public void setTask(AsyncTask task) {
		this.task = task;
	}

	/**
	 * Returns the tick interval in milliseconds of the timer.
	 * */
	public long getPeriod() {
		return period;
	}

	/**
	 * Sets the ticking interval for the timer in milliseconds.
	 * */
	public void setPeriod(long perioud) {
		this.period = perioud;
	}
	
	/**
	 * Adds a listener object to the timer that will be called after the timer task was executed.
	 * */
	public void subscribe(Tickable t) {
		this.listenderObjects.add(t);
	}

}

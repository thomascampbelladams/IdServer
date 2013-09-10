package server;
import java.io.Serializable;
import java.util.Timer;
import java.util.TimerTask;

public class Timeout implements Serializable{
		
		/**
	 * 
	 */
	private static final long serialVersionUID = -5411487241057471921L;

		Bully bu;

		private Timer timer = new Timer();   
		private int seconds;   
		public Timeout(int sec, Bully bu)    
		{    
			this.seconds = sec;   
			this.bu = bu;
		}   
		public void start() {    
			timer.schedule(new TimerTask() {    
				public void run()    
				{    
					bu.Coordinator();    
					timer.cancel(); 
					timer=null;
				}       
			}, seconds* 1000);   
		}   
		public void cancel()   
		{   
			timer.cancel();   
		}   

	}

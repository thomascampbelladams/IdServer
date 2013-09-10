package server;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import client.ServerMessage;

public class Bully {

	InetSocketAddress[] ips;
	InetSocketAddress[] deadips;
	InetSocketAddress me;
	int id;   // process id
	int port; // local port
	
	Timeout timeout=null; // if a timeout occurs it's because I'm the biggest  
	Timer dycbTimer;
	int dycbDelay=3*1000;

	IdInterServerMessage conduit;
	public IdServer server;

	void ParseNeighborsFile(String filename){
		try{			  
			BufferedReader br = new BufferedReader(new FileReader(filename) );
			String strLine;
			while ((strLine = br.readLine()) != null)   {
				if (strLine.charAt(0)=='#')
					continue;
				String [] address = strLine.split(":");

				if (Integer.parseInt( address[0] ) == id)
				{
					me = new InetSocketAddress(address[1], Integer.parseInt(address[2]));
					continue;
				}

				InetSocketAddress isa = new InetSocketAddress(address[1], Integer.parseInt(address[2]) );
				ips[Integer.parseInt( address[0])]= isa;
			}
			br.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	Bully(final int id, int port, IdServer server) throws IOException   
	{   
		this.id = id; 
		this.port = port;
		this.server = server;
		conduit = new IdInterServerMessage(this);
		ips = new InetSocketAddress[9];
		deadips = new InetSocketAddress[9];
		dycbTimer = new Timer();
	}   

	public void start()
	{
		checkServer(); // check the servers right away to see if they are alive
		Election();

		dycbTimer.schedule(new TimerTask(){
			public void run(){
				checkServer();
				didYouComeBack();
			}
		}, 0, dycbDelay);

		//start multicast listener
		new Thread(new Runnable(){

			@Override
			public void run() {
				try
				{
					System.out.println("Starting multicast server...");
					InetAddress group = InetAddress.getByName("224.0.0.1");
					MulticastSocket s = new MulticastSocket(6789);
					s.setSoTimeout(0); //10 seconds
					NetworkInterface net = NetworkInterface.getByName("eth0");
					s.setNetworkInterface(net);
					s.joinGroup(group);
		
					
					while (true){
						byte[] buf = new byte[256];
						DatagramPacket recv = new DatagramPacket(buf, buf.length);
						s.receive(recv);
						if ( new String(buf).trim().contains("AREYOUTHERE") )
						{
							System.out.println("Got smiley!");
							String caddr = me.getAddress().getHostAddress()+":"+me.getPort();
							buf = caddr.getBytes();
							DatagramPacket send = new DatagramPacket(buf, buf.length, group, 6789);
							s.send(send);
						}
					}
				}catch (Exception e)
				{
					e.printStackTrace();
				}



			}

		}).start();
	}

	// sends a message to coordinator
	private void Message(String msg, InterServerMessage ism){
		try {   
			ism.Message(conduit, msg); 
		} catch (IOException e)   
		{   
			System.out.println(server.coordinator + " connection failed, starting election.");   
			Election();
			// Attempts to send the message again
			Message(msg, ism);
		}    
	}

	private void checkServer(){
		InetSocketAddress currIp = ips[server.coordinator];
		if( (!server.isCooridnator) && currIp == null){
			Election();
			return;
		}
		for(int i=0; i<ips.length; i++){
			int otherid = i;
			InetSocketAddress aliveIp = ips[i];
			if(otherid != this.id && aliveIp != null){
				System.out.println("checking "+otherid +"... ");
				try {
					InterServerMessage ism = (InterServerMessage) tryConnect(aliveIp, "Messaging");
					if(ism == null)
						throw new Exception(aliveIp + " didn't connect.");
					if(ism.Message(conduit, "AREYOUALIVE") == 1){
						int currCoor = ism.Message(conduit, "WHOISCURRENTCOORDINATOR");
						if(otherid > currCoor) Election();
						InetSocketAddress corrIp = ips[otherid];
						ism = (InterServerMessage) tryConnect(corrIp, "Messaging");
						int othertime = ism.Message(conduit, "PROGRAMTIME");
						System.out.println("Got time "+ othertime+ " from server "+otherid + " my time is "+server.time);
						if(othertime > server.time){
							ArrayList<SqlAction> clock = ism.sync(false, null);
							System.out.println(clock);
							server.syncDown(clock);
						}
					}
				} catch(Exception e){
					InetSocketAddress otherip = aliveIp;
					if(otherip != null){
						deadips[otherid] = otherip;
						ips[otherid] = null;
					}				
					System.err.println(e.getMessage());
					//e.printStackTrace();

				}
			}
		} //end for
	}

	private void didYouComeBack(){

		for(int i=0; i<ips.length; i++){
			int otherid=i;
			InetSocketAddress deadip = deadips[otherid];
			if(otherid != id && deadip != null){
				InterServerMessage ism = null;
				try {
					ism = (InterServerMessage) tryConnect(deadip, "Messaging");
					if(ism != null && ism.Message(conduit, "AREYOUALIVE") == 1){
						ips[otherid] = deadip;
						deadips[otherid] = null;
						if(server.coordinator < otherid) // someone bigger came back
							Election();
					}
				}
				catch (Exception e) {
				} 
			}
		} 
	}

	public void makeAlive(int i)
	{
		if (deadips[i] != null)
			ips[i] = deadips[i];
	}
	//Holds election, to see if anyone bigger is out there
	private void Election(){   
		System.out.println("Holding Election!");
		timeout=new Timeout(3, this);   
		timeout.start();     
		int max = 0;
		for(int i=0; i<ips.length; i++){
			int otherid = i;
			InetSocketAddress aliveIp = ips[i];
			if(otherid>this.id && aliveIp != null){   
				max = otherid;                                
			}
		}
		InetSocketAddress aliveIp = ips[max];
		if(max != 0 && max != this.id && aliveIp != null){
			try {   
				InterServerMessage ism = (InterServerMessage) tryConnect(aliveIp, "Messaging");
				if(ism != null) ism.Message(conduit, "ELECT="+id);
			} catch (Exception e)   
			{   
				e.printStackTrace();
				System.out.println(max + " election connection failed.");   
			}  
		}
	}   

	private Object tryConnect(InetSocketAddress ia, String rminame) throws MalformedURLException, RemoteException, NotBoundException
	{
		//try{
		String connect = "rmi://"+ia.getHostName()+":"+ia.getPort()+"/"+rminame;
		return Naming.lookup(connect);
		//} catch(Exception e){
		//	return null;
		//}
	}

	//Send OK if I'm bigger   
	public void Reply(InterServerMessage ism) {   
		try {   
			ism.Message(conduit, "OK="+id);
		} catch (IOException e)   
		{   
			System.out.println("Reply: process not responding.");   
		}     
		// Hold election	
		Election();   
	}   

	// Becomes the coordinator cause I'm biggest
	public void Coordinator() { 
		int currCor = this.id;
		//bugfix, make sure the timer is stopped
		if (timeout != null) {
			timeout.cancel();
			timeout = null;
		}

		for(int i = 0; i < ips.length; i++){
			Integer otherid=i;
			InetSocketAddress aliveIp = ips[i];

			if(aliveIp != null && i != this.id){
				try{
					InterServerMessage ism = (InterServerMessage) tryConnect(aliveIp, "Messaging");
					currCor = ism.Message(conduit, "WHOISCURRENTCOORDINATOR");
					break;
				} catch(Exception e){}
			}
		}

		if(currCor != this.id){
			try{
				InterServerMessage ism = (InterServerMessage) tryConnect(ips[currCor], "Messaging");
				ArrayList<SqlAction> list = ism.sync(false, (new ArrayList<SqlAction>()));
				server.syncDown(list);
			} catch(Exception e){
				System.out.println("Issue with getting list from current coordinator!");
				e.printStackTrace();
			}
		}

		// sends a message to everyone else   
		for(int i=0; i<ips.length; i++){
			Integer otherid=i;
			InetSocketAddress aliveIp = ips[i];
			if(aliveIp != null && i != this.id){
				try {   
					InterServerMessage ism = (InterServerMessage) tryConnect(aliveIp, "Messaging");
					ism.Message(conduit, "IMCOOR="+id);

				} catch (Exception e)    
				{   
					System.out.println(otherid+" not avaliable.");
				}   
			}

		}   
		server.isCooridnator=true; 
		server.coordinator = id;
		System.out.println("I'm coordinating!");

		InetSocketAddress newcoord;
		if( (newcoord = ips[server.coordinator])!= null)
		{

			for( ServerMessage sm : server.client_callbacks)
				try {
					sm.Message("CHGSRV="+newcoord.getHostName()+":"+newcoord.getPort());
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NotBoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}

	public void syncUp(ArrayList<SqlAction> vectorClock) {
		// TODO Auto-generated method stub
		for(int i = 0; i < ips.length; i++){
			Integer otherid = i;
			InetSocketAddress aliveIp = ips[i];
			if(aliveIp != null && i != this.id){
				try {
					InterServerMessage ism = (InterServerMessage) tryConnect(aliveIp, "Messaging");
					ism.sync(true, vectorClock);
				} catch (Exception e ) {
					System.err.println("Failed to syncup: "+e.getMessage());
				}

			}
		}
	}   	
}

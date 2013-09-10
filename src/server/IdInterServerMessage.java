package server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RMISocketFactory;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;
import java.util.ArrayList;

import client.ServerMessage;

public class IdInterServerMessage extends UnicastRemoteObject implements InterServerMessage{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8412590108930866690L;
	private Bully bully;

	IdInterServerMessage(Bully bully) throws IOException
	{
		this.bully = bully;
	
		Registry registry = LocateRegistry.getRegistry(bully.port );
		registry.rebind("Messaging", this);
	}

	@Override
	public synchronized int Message( InterServerMessage ism, String msg) throws RemoteException {

		if(msg.contains("IMCOOR"))
		{
			String [] message = msg.split("=");
			System.out.println(message[1] + " is the new coordinator.");
			bully.server.coordinator = (Integer.parseInt(message[1]));
			bully.server.isCooridnator = false;

			// Tell any client who's boss
			InetSocketAddress newcoord;
			if( (newcoord = bully.ips[bully.server.coordinator])!= null)
			{

				try {

					for (ServerMessage sm : bully.server.client_callbacks)
						sm.Message("CHGSRV="+newcoord.getHostName()+":"+newcoord.getPort());
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NotBoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		else if(msg.contains("MSG"))
		{
			// do server messages here
		}
		else if(msg.contains("OK"))
		{

			bully.timeout.cancel();
			bully.timeout= null;

			bully.server.isCooridnator = false;
		}
		else if(msg.contains("AREYOUALIVE"))
		{

		}
		else if(msg.contains("WHOISCURRENTCOORDINATOR"))
		{

			return bully.server.coordinator;
		}
		else if(msg.contains("ELECT"))
		{
			String [] message = msg.split("=");
			if (Integer.parseInt(message[1]) < bully.id )
				bully.Reply(ism);
			bully.makeAlive(Integer.parseInt(message[1]));
		}
		else if(msg.contains("PROGRAMTIME")){
			return bully.server.time;
		}
		else{
			System.err.println("Invalid message: "+msg);
		}
		return 1;
	}

	public synchronized ArrayList<SqlAction> sync(boolean dir, ArrayList<SqlAction> clock) throws RemoteException, SQLException{
		if(dir) bully.server.syncDown(clock);
		else return bully.server.vectorClock;
		return null;
	}

}

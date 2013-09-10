package client;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import server.Server;

public class IdServerMessage extends UnicastRemoteObject implements ServerMessage{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1050785327297815458L;
	IdClient idclient;
	
	IdServerMessage(IdClient idclient) throws RemoteException
	{
		this.idclient = idclient;
	}
	
	@Override
	public void Message(String msg) throws RemoteException, MalformedURLException, NotBoundException {
		
		if(msg.contains("CHGSRV"))
		{
			String [] message = msg.split("=");
			Server server = (Server)Naming.lookup("rmi://"+message[1]+"/DougThomas");
			idclient.setServer(server);
			System.out.println("Changing coordinator to: "+message[1]);
		}
	}

}

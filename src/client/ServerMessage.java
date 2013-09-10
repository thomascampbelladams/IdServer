package client;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerMessage extends Remote{

	void Message( String msg ) throws RemoteException, MalformedURLException, NotBoundException;
}

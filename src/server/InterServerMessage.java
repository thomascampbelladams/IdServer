package server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;

public interface InterServerMessage extends Remote{

	int Message( InterServerMessage ism, String msg) throws RemoteException;
	ArrayList<SqlAction> sync(boolean dir, ArrayList<SqlAction> clock) throws RemoteException, SQLException;
}

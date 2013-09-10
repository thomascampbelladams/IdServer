package server;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.MulticastSocket;
import java.rmi.NotBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;

import client.ServerMessage;

public class IdServer implements Server 
{   
	AuthController authcontroller;
	EncryptPassword ec;
	boolean verbose = false;
	int serverNumber;
	int time = 0;
	ArrayList<SqlAction> vectorClock = new ArrayList<SqlAction>();
	ArrayList<IdServer> servers = new ArrayList<IdServer>();
	ArrayList<ServerMessage> client_callbacks = new ArrayList<ServerMessage>();
	public int coordinator;
	public boolean isCooridnator;
	Timeout timeout; 
	Bully process;

	private boolean printstacktrace = true;

	public IdServer(boolean verbose, int serverNumber) throws NoSuchAlgorithmException, IOException, SQLException 
	{
		this.verbose = verbose;
		this.serverNumber = serverNumber;
		coordinator = this.serverNumber;
		isCooridnator = true;

		System.setSecurityManager(new RMISecurityManager());
		try {
			System.out.println("Loading auth.db...");
			authcontroller = new AuthController("jdbc:sqlite:auth"+serverNumber+".db");
			time = (int) authcontroller.getTime();
			ec = new EncryptPassword();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			PrintError(e);
		}

	}

	public void bind (String name, int registryPort)
	{
		RMIClientSocketFactory rmiClientSocketFactory = new SslRMIClientSocketFactory();
		RMIServerSocketFactory rmiServerSocketFactory = new SslRMIServerSocketFactory();

		try {
			System.out.println("Loading RMI registry...");
			Server server = (Server) UnicastRemoteObject.exportObject(this, 0, 
					rmiClientSocketFactory, rmiServerSocketFactory);
			Registry registry = LocateRegistry.createRegistry(registryPort);
			registry.rebind(name, server);
			System.out.println("RMI registry bound on port " +registryPort +"!");
		} catch (RemoteException e) {
			PrintError(e);
		}
	}

	public static void main(String args[]) throws NoSuchAlgorithmException, IOException, SQLException 
	{
		final Thread mainThread = Thread.currentThread();
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				System.out.println("Server is shutting down...");
				try {
					mainThread.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});

		if (args.length > 1 && args[0].contains("-h"))
		{
			System.out.println("Usage: idServer [--port <port no>] [--verbose]");
			System.exit(0);
		}

		int idx;
		int port = 1099; //default port
		Boolean verbose = false; //default verbosity
		int serverNumber = 1;
		if ( (idx = Arrays.asList(args).indexOf("--port") ) != -1)
			port = Integer.parseInt(args[idx+1]);
		if ( (idx = Arrays.asList(args).indexOf("--verbose") )  != -1)
			verbose = true;
		if ( (idx = Arrays.asList(args).indexOf("--number") ) != -1)
			serverNumber = Integer.parseInt(args[idx+1]);
		System.out.println("Loading system properties... ");
		System.setProperty("java.security.policy", "mysecurity.policy");
		System.setProperty("javax.net.ssl.keyStore","server.keystore");
		System.setProperty("javax.net.ssl.keyStorePassword","password");

		try {
			System.out.println("Starting server...");
			IdServer server = new IdServer(verbose, serverNumber);

			server.bind("DougThomas", port);

			server.process = new Bully(server.serverNumber, port, server);
			server.process.ParseNeighborsFile("hosts.txt");
			server.process.start();
		} catch (RemoteException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public User CreateUser(String username, String name, String password)
			throws java.rmi.RemoteException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException 
			{
		System.out.println("Creating user!");
		Long clock = System.currentTimeMillis();
		password = ec.encryptPassword(password);
		System.out.println("encrypt: "+(System.currentTimeMillis()-clock));
		
		PrintConnection("CreateUser");
		try {
			QueryResults qr = authcontroller.newUser(username, password, UnicastRemoteObject.getClientHost(), System.currentTimeMillis(), name);
			time++;
			vectorClock.add(new SqlAction(qr.getQuery(), time));
			syncUp();
			return qr.getUser();
		} catch (Exception e)
		{
			PrintError(e);
		}		

		return null;
			}

	@Override
	public User Lookup(String username) throws RemoteException {
		PrintConnection("Lookup");
		try {
			return authcontroller.getUser(username);
		} catch (SQLException e) {
			PrintError(e);
		}
		return null;
	}

	@Override
	public User RLookup(String uuid) throws RemoteException {
		PrintConnection("RLookup");
		try {
			return authcontroller.getUserWithUUID(uuid);
		} catch (SQLException e) {
			PrintError(e);
		}
		return null;
	}

	@Override
	public int ModifyUser(String oldloginname, String newloginname,
			String password) throws RemoteException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException 
			{
		password = ec.encryptPassword(password);
		PrintConnection("ModifyUser");
		int rval = 0;
		try {
			if( authcontroller.login(oldloginname, password) ){
				QueryResults qr = authcontroller.updateUser(oldloginname, newloginname, password);
				time++;
				vectorClock.add(new SqlAction(qr.getQuery(), time));
				rval = qr.getChangedRows();
				syncUp();
			}
		} catch (SQLException e) {
			PrintError(e);
		}

		return rval;

			}

	@Override
	public int DeleteUser(String username, String password) throws RemoteException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException {
		password = ec.encryptPassword(password);
		PrintConnection("DeleteUser");
		int rval = 0;
		try {
			if (authcontroller.login(username, password))
			{
				QueryResults qr = authcontroller.removeUser(username, password);
				time++;
				vectorClock.add(new SqlAction(qr.getQuery(), time));
				rval = qr.getChangedRows();
				syncUp();
			}
		} catch (SQLException e) {
			PrintError(e);
		}
		return rval;
	}

	@Override
	public Users GetUsers() throws RemoteException {
		PrintConnection("GetUsers");
		try {
			return authcontroller.getAllUsers();
		} catch (SQLException e) {
			PrintError(e);
		}
		return null;
	}

	public int modifyPassword (String username, String oldpassword, String newpassword) throws RemoteException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException{
		oldpassword = ec.encryptPassword(oldpassword);
		newpassword = ec.encryptPassword(newpassword);
		PrintConnection("modifyPassword");
		int rval = 0;
		try {
			QueryResults qr = authcontroller.updateUserPassword(username, oldpassword, newpassword);
			time++;
			vectorClock.add(new SqlAction(qr.getQuery(), time));
			rval = qr.getChangedRows();
			syncUp();
		} catch (SQLException e) {
			PrintError(e);
		}
		
		return rval;
	}

	public void syncUp() throws SQLException{
		authcontroller.recordTime(time);
		process.syncUp(vectorClock);
	}

	public void syncDown(ArrayList<SqlAction> clock) throws SQLException{
		for(SqlAction act : clock){
			if(act.getTime() > time){
				System.out.println("Running query: "+act.getSql());
				authcontroller.runQuery(act.getSql());
				time = act.getTime();
			}
		}
		vectorClock = clock;
		authcontroller.recordTime(time);
	}

	private void PrintError(Exception e){
		if (printstacktrace)
			e.printStackTrace();
		else
			System.err.println(e.getMessage());
	}

	private void PrintConnection(String methodName)
	{
		if (verbose == true)
		{
			try {
				System.out.println("Connection from: " +UnicastRemoteObject.getClientHost() + " calling " +methodName);
			} catch (ServerNotActiveException e) {
				// TODO Auto-generated catch block
				System.out.println("Client called: "+methodName);
			}
		}
	}

	@Override
	public void setCallback(ServerMessage sm) throws RemoteException {
		client_callbacks.add(sm);
	}

	@Override
	public void getCoordinator(ServerMessage sm) throws RemoteException {
		InetSocketAddress newcoord;
		if( (newcoord = process.ips[coordinator])!= null)
		{

			try {
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

}
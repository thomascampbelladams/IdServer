package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import server.Server;
import server.User;
import server.Users;

/**
 * Presented by Doug Applegate and Thomas Campbell-Adams
 * 
 * MyClient is observed be be a sophisticated mechanism for creating users with passwords
 * in a sort of identity server.  All users are identified uniquely using a UUID. Communication
 * is done over the network via SSL RMI, leaving no rock unturned and no corner unexplored when 
 * it comes to security
 */
public class IdClient extends java.rmi.server.UnicastRemoteObject
{
	private static final long serialVersionUID = -6314695118464643327L;
	private Server server;
	public Error error;
	private IdServerMessage serverCallback;

	public static void main(String [] args)  
	{
		if (args.length < 2 )
		{
			System.err.println("Usage: IdClient <serverhost> <query>\nFor help: IdClient <serverhost> --help ");
			System.exit(1);
		}

		IdClient client = null;
		boolean rval = false;
		try {
			client = new IdClient( args[0] );


			if (client.error != null)
			{
				System.err.println(client.error.getMessage());
				System.exit(1);
			}


		} catch (Exception e)
		{
			InetAddress group = null;
			MulticastSocket s = null;
			NetworkInterface net = null;
			// If initial connection fails, go ahead and give multicast a go
			try {
				System.out.println("WARNING: client connect failed: "+e.getMessage()+"Starting multicast...");
				group = InetAddress.getByName("224.0.0.1");
				s = new MulticastSocket(6789); // Non-documented hard coded value
				s.setSoTimeout(3000); // 3 seconds
				//Enumeration<NetworkInterface> ne = NetworkInterface.getNetworkInterfaces();
				//while(ne.hasMoreElements())
				//	if((net = ne.nextElement()).isUp() && net.supportsMulticast())
				//		break;
				net = NetworkInterface.getByName("eth0");
				if(net == null)
					throw new Exception("No multicast interface avaliable!");
				System.out.println("Multicast interface: "+net.getName());
				s.setNetworkInterface(net);
				s.joinGroup(group);
				StringWriter str = new StringWriter();
				str.write("AREYOUTHERE");
				byte[] buf = new byte[256];
				while(true)
				{
					
					DatagramPacket areyouthere = new DatagramPacket(str.toString().getBytes(),
							str.toString().length(), group, 6789);
					s.send(areyouthere);

					DatagramPacket recv = new DatagramPacket(buf,buf.length );
					s.receive(recv);
					String msg = new String(buf).trim();
					if (! ( msg.contains("AREYOUTHERE") ) )
						break;
				}
				
				try {
					client = new IdClient( new String(buf).trim() );


					if (client.error != null)
					{
						System.err.println(client.error.getMessage());
						System.exit(1);
					}


				} catch (Exception e2)
				{
					e2.printStackTrace();
					System.exit(1);
				}
				
				

			} catch (Exception e1) {
				System.err.println(e1.getMessage());
				e1.printStackTrace();
				System.exit(1);
			} 

		}
		try {
			Long clock = System.currentTimeMillis();
			rval = client.executeCommand(Arrays.copyOfRange(args, 1, args.length));
			System.out.print("Execute command: "+(System.currentTimeMillis() - clock));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		if (! rval)
		{
			System.err.println(client.error.getMessage());
			System.exit(1);
		}
		System.exit(0);
	}

	/**
	 * Validates the input from console
	 * @param q Array of strings inputted from the console
	 * @return True if the command is valid.
	 */
	private boolean VerifyQuery(String [] q) 
	{
		boolean rval = false;
		if( q[0] != null )
		{
			String command = q[0];
			if (command.equals("-l") || command.equals("--lookup") 
					|| command.equals("-r") || command.equals ("--reverse-lookup")
					|| command.equals("-g") || command.equals ("--get") )
				rval = ( q.length == 2 );
			else if( command.equals("-m") || command.equals("--modify")) 
				if ( q.length == 5 )
					rval = ( q[3].equals("-p") || q[3].equals( "--password" ) );
				else
					rval = ( q.length == 3);
			else if( command.equals("-d") || command.equals("--delete") )
				if ( q.length == 4 )
					rval = ( q[2].equals("-p") || q[2].equals( "--password") );
				else
					rval = ( q.length == 2);
			else if( command.equals ("-c") || command.equals("--create") )
				if (q.length == 5)
					rval = ( q[3].equals("-p") || q[3].equals( "--password") );
				else if (q.length == 4 )
					rval = ( q[2].equals("-p") || q[2].equals( "--password") );
				else
					rval = (q.length == 3 || q.length == 2);
		}
		return rval;
	}

	/**
	 * Constructs a client and sets the host variable 
	 * @param host Address of the host to connect to.
	 * @throws RemoteException
	 * @throws NotBoundException 
	 * @throws MalformedURLException 
	 * @throws AlreadyBoundException 
	 */
	public IdClient(String host) throws RemoteException, MalformedURLException, NotBoundException, AlreadyBoundException
	{
		Long clock = System.currentTimeMillis();
		System.setProperty("java.security.policy", "mysecurity.policy");
		System.setProperty("javax.net.ssl.trustStore", "client.keystore");
		System.setProperty("javax.net.ssl.keyStore", "client.keystore");
		System.setProperty("javax.net.ssl.keyStorePassword", "password");
		System.out.println("Set properties: "+(System.currentTimeMillis()-clock)+" millis");
		System.setSecurityManager(new RMISecurityManager());
		this.server = (Server)Naming.lookup("rmi://"+host+"/DougThomas");
		//Naming.rebind("rmi://"+host+"/DougThomas", this.server);
		serverCallback = new IdServerMessage(this);
		//getCoordinator();
	}

	/**
	 * Takes in an array of command arguments and runs the appropriate functions.
	 * @param args Array of command arguments
	 * @return True if the command has executed succesfully
	 * @throws IOException
	 * @throws BadPaddingException 
	 * @throws IllegalBlockSizeException 
	 * @throws NoSuchPaddingException 
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 */
	boolean executeCommand(String [] args) throws IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException
	{
		Long clock = System.currentTimeMillis();
		if  ( VerifyQuery(args) == false )
		{
			String errormsg = "Usage: \n" +
					"--create <loginname> [<real name>] [--password <password>]\n" +
					"--lookup <loginname>\n" +
					"--reverse-lookup <UUID>\n" +
					"--modify <oldloginname> <newloginname> [--password <password>]\n" +
					" --delete <loginname> [--password <password>]\n" +
					" --get name|uuids|all\n" +
					"The above options can be abbreviated as-c, -l, -r, -m, -d, -p (for password) and -g.";
			error = new Error(errormsg);
			return false;
		}
		System.out.println("VerifyQuery: "+(System.currentTimeMillis()-clock));

		String query = args[0];
		if (query.contains("-c"))
		{

			String loginname = args[1];
			String realname = "";
			String password = "";

			if (args.length == 2)
			{
				realname = System.getProperty("user.name");
				password = getPasswordFromUser(true);
			}
			else if (args.length == 3)
			{
				realname = args[2];
				password = getPasswordFromUser(true);
			}
			else if (args.length == 4)
			{
				realname = System.getProperty("user.name");
				password = args[3];
			}
			else if (args.length == 5)
			{
				password = args[4];
				realname = args[2];
			}

			User ruser;
			try {
				ruser = server.CreateUser(loginname, realname, password);
				if (ruser == null)
					throw new Exception("Create User "+loginname +" failed.");
				else
					System.out.println(loginname +" succesfully created.");
			} catch (Exception e) {
				//e.printStackTrace();
				error = new Error(e);
				return false;
			}

		}

		else if ( query.contains("-l") )
		{	
			String lookupname = args[1];
			User lookup = server.Lookup(lookupname);
			if (lookup == null){
				System.err.println("No results for: "+lookupname);
				return false;
			}
			else
				System.out.println(lookup);
		}

		if ( query.contains("-r") )
		{
			String lookupuuid = args[1];
			User lookup = server.RLookup( lookupuuid );
			if (lookup == null)
			{
				System.err.println("No results for: "+lookupuuid);
				return false;
			}
			else
				System.out.println(lookup);
		}

		else if ( query.contains("-m") )
		{
			String oldloginname = args[1];
			String newloginname = args[2];
			String password = "";
			if ( args.length >= 5 )
				password = args[4];
			else
				password = getPasswordFromUser(false);

			int rval = server.ModifyUser( oldloginname, newloginname, password );
			if (rval >= 1)
				System.out.println("Changed " +oldloginname + " to "+ newloginname);
			else
			{
				System.err.println("Nothing modified.");
				return false;
			}
		}

		else if ( query.contains("-d") )
		{
			String loginname = "";
			String password = "";

			loginname = args[1];

			if (args.length == 4)
				password = args[3];

			if( password.isEmpty() )
			{
				System.out.println( "Password required for \"" + loginname +"\"") ;
				password = getPasswordFromUser(false) ;
			}

			int rval = server.DeleteUser(loginname, password);
			if (rval == 1)
				System.out.println("Deleted "+loginname);
			else if (rval > 1)
				System.out.println("Deleted all "+rval +" instances of " + loginname);
			else
			{
				System.err.println("Nothing modified.");
				return false;
			}
		}

		else if ( query.contains("-g") )
		{
			Users users = server.GetUsers();
			ArrayList<User> userlist = users.getUserList();
			Iterator<User> itr = userlist.iterator();


			if (args[1].equals("uuid") )
				while (itr.hasNext())
					System.out.println(itr.next().getUuid());
			else if (args[1].equals("name"))
				while (itr.hasNext())
					System.out.println(itr.next().getName());
			else if (args[1].equals("all"))
				while (itr.hasNext())
					System.out.println(itr.next()+"\n");

		}
		return true;
	}

	/**
	 * Gets the server object
	 * @return Server Object
	 */
	public Server getServer()
	{
		return server;
	}

	public void setServer(Server server)
	{
		this.server = server;
	}

	/**
	 * Gets the password from the user
	 * @param verify True if you wish to prompt the user to insert the new password twice
	 * @return The inputted password
	 * @throws IOException
	 */
	private String getPasswordFromUser(boolean verify) throws IOException
	{	
		String password = "";	
		while (password.isEmpty())
		{
			System.out.print("Password: ");
			BufferedReader br = new BufferedReader (new InputStreamReader(System.in) );
			password = br.readLine();
			if(verify)
			{
				System.out.print("Verify: ");
				if(! password.equals( br.readLine() ) )
				{
					System.err.println("Didn't match.");
					password = "";
				}
			}

		}
		return password;
	}

	public IdServerMessage getServerCallback() {
		return serverCallback;
	}

	public void setServerCallback(IdServerMessage serverCallback) throws RemoteException {
		this.serverCallback = serverCallback;
		server.setCallback(this.serverCallback);
	}

	public void getCoordinator() throws RemoteException
	{
		server.getCoordinator(serverCallback);
	}
}

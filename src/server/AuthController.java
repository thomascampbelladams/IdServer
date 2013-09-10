package server;

import java.sql.*;
import java.util.UUID;

public class AuthController {
	String connectionString;
	
	/**
	 * Constructs the AuthController and sets the SQL connection string.
	 * @param cs ConnectionString
	 * @throws ClassNotFoundException
	 */
	public AuthController(String cs) throws ClassNotFoundException{
		connectionString = cs;
		Class.forName("org.sqlite.JDBC");
	}
	
	/**
	 * Creates the tables required for the server if they don't exist.
	 * @throws SQLException
	 */
	private synchronized void  createTableIfNotExist() throws SQLException{
		Connection conn = DriverManager.getConnection(connectionString);
		Statement s = conn.createStatement();
		s.executeUpdate("CREATE TABLE IF NOT EXISTS users (uuid text, username text, password text, ip text, logintime integer, name text);");
		s.executeUpdate("CREATE TABLE IF NOT EXISTS programtime (time integer);");
		conn.close();
	}
	
	public synchronized void recordTime(int time) throws SQLException{
		this.createTableIfNotExist();
		
		String sqliteTruncate = "DELETE FROM programtime;";
		Connection conn = DriverManager.getConnection(connectionString);
		Statement s = conn.createStatement();
		s.execute(sqliteTruncate);
		String sqliteInsert = String.format("INSERT INTO programtime (time) VALUES (%1$d);", time);
		s.execute(sqliteInsert);
		conn.close();
	}
	
	public synchronized int getTime() throws SQLException{
		this.createTableIfNotExist();
		
		String sqliteSelect = "SELECT * FROM programtime;";
		String sqliteCount = "SELECT COUNT(*) AS length FROM programtime";
		Connection c = DriverManager.getConnection(connectionString);
		Statement s = c.createStatement();
		ResultSet r = s.executeQuery(sqliteCount);
		if(r.getInt("length") > 0){
			r = s.executeQuery(sqliteSelect);
			int ret = r.getInt("time");
			c.close();
			System.out.println("Recorded time:" + ret);
			return ret;
		}
		System.out.println("No time!");
		return 0;
	}
	
	/**
	 * Creates a new user
	 * @param username Username for the new user
	 * @param password Password for the new user
	 * @param ip IP of the client creating the new user
	 * @param loginTimestamp Timestamp representing the time of the new user creation request
	 * @param name Real name of the user
	 * @return The User object representing the newly created user in the system.
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public synchronized QueryResults newUser(String username, String password, String ip, long loginTimestamp, String name) throws ClassNotFoundException, SQLException{
		Long clock = System.currentTimeMillis();
		this.createTableIfNotExist();
		System.out.println("createtableifnotexist: "+(System.currentTimeMillis()-clock));
		
		UUID id = UUID.randomUUID();
		
		clock = System.currentTimeMillis();
		String sqliteInsert = String.format("INSERT INTO users (uuid, username, password, ip, logintime, name) VALUES ('%1$s', '%2$s', '%3$s', '%4$s', %5$d, '%6$s');", id, username, password.replace("'", "''"), ip, loginTimestamp, name);
		Connection conn = DriverManager.getConnection(connectionString);
		Statement s = conn.createStatement();
		s.executeUpdate(sqliteInsert);
		conn.close();
		System.out.println("sqlinsert: "+(System.currentTimeMillis()-clock));
		
		
		
		return new QueryResults(sqliteInsert, 
				new User(id.toString(), username, password, ip, loginTimestamp, name));
	}
	
	/**
	 * Updates the user's username
	 * @param oldusername User's previous username
	 * @param newusername User's new username
	 * @param password User's password
	 * @return 
	 * @throws SQLException
	 */
	public synchronized QueryResults updateUser(String oldusername, String newusername, String password) throws SQLException
	{
		this.createTableIfNotExist();
		String sqliteUpdate = String.format("UPDATE users SET username = '%1$s' WHERE username = '%2$s' AND password = '%3$s';", newusername, oldusername, password);
		System.out.println(sqliteUpdate);
		
		Connection conn = DriverManager.getConnection(connectionString);
		Statement s = conn.createStatement();
		int rval = s.executeUpdate(sqliteUpdate);
		conn.close();
		return new QueryResults(sqliteUpdate, rval);
	}
	
	/**
	 * Updates the user's password
	 * @param username User's username
	 * @param oldpassword User's old password
	 * @param newpassword User's new password
	 * @return 
	 * @throws SQLException
	 */
	public synchronized QueryResults updateUserPassword(String username, String oldpassword, String newpassword) throws SQLException
	{
		this.createTableIfNotExist();
		String sqliteUpdate = String.format("UPDATE users SET password = '%1$s' WHERE username = '%2$s' AND password = '%3$s';", newpassword, username, oldpassword);
		
		Connection conn = DriverManager.getConnection(connectionString);
		Statement s = conn.createStatement();
		int rval = s.executeUpdate(sqliteUpdate);
		conn.close();
		return new QueryResults(sqliteUpdate, rval);
	}
	
	/**
	 * Removes a user from the system
	 * @param username User's username
	 * @param password User's password
	 * @return
	 * @throws SQLException
	 */
	public synchronized QueryResults removeUser(String username, String password) throws SQLException
	{
		this.createTableIfNotExist();
		String sqliteDelete = String.format("DELETE FROM users WHERE username = '%1$s' and password = '%2$s';", username, password);
		
		Connection conn = DriverManager.getConnection(connectionString);
		Statement s = conn.createStatement();
		int rval = s.executeUpdate(sqliteDelete);
		conn.close();
		return new QueryResults(sqliteDelete, rval);
	}
	
	/**
	 * Get's a user record from the database
	 * @param username User's username
	 * @return The user's record
	 * @throws SQLException
	 */
	public User getUser(String username) throws SQLException{
		this.createTableIfNotExist();
		
		String sqliteSelect = String.format("SELECT * FROM users WHERE username = '%1$s';", username);
		Connection conn = DriverManager.getConnection(connectionString);
		Statement s = conn.createStatement();
		ResultSet rs = s.executeQuery(sqliteSelect);
		
		User u = new User(rs);
		rs.close();
		conn.close();
		return u;
	}
	
	/**
	 * Gets a user record from the database
	 * @param id User's UUID
	 * @return The user's record
	 * @throws SQLException
	 */
	public User getUserWithUUID(String id) throws SQLException{
		this.createTableIfNotExist();
		
		String sqliteSelect = String.format("SELECT * FROM users WHERE uuid = '%1$s';", id);
		Connection conn = DriverManager.getConnection(connectionString);
		Statement s = conn.createStatement();
		ResultSet rs = s.executeQuery(sqliteSelect);
		
		User u = new User(rs);
		rs.close();
		conn.close();
		return u;
	}
	
	/**
	 * Gets all user records from the database
	 * @return All user records contained in the database
	 * @throws SQLException
	 */
	public Users getAllUsers() throws SQLException{
		this.createTableIfNotExist();
		
		String sqliteSelect = "SELECT * FROM users;";
		Connection conn = DriverManager.getConnection(connectionString);
		Statement s = conn.createStatement();
		ResultSet rs = s.executeQuery(sqliteSelect);
		
		Users u = new Users(rs);
		rs.close();
		conn.close();
		return u;
	}
	
	/**
	 * Gets a user record from the database
	 * @param id User's UUID
	 * @return The user's record
	 * @throws SQLException
	 */
	public User getUser(UUID id) throws SQLException{
		this.createTableIfNotExist();
		
		String sqliteSelect = String.format("SELECT * FROM users WHERE id = '%1$s';", id);
		Connection conn = DriverManager.getConnection(connectionString);
		Statement s = conn.createStatement();
		ResultSet rs = s.executeQuery(sqliteSelect);
		
		User u = new User(rs);
		rs.close();
		conn.close();
		return u;
	}
	
	/**
	 * Authorizes a user in the system
	 * @param username User's username
	 * @param password User's password
	 * @return True if the authorization succeeds.
	 * @throws SQLException
	 */
	public Boolean login(String username, String password) throws SQLException{
		int i = 0;
		this.createTableIfNotExist();
		
		String sqliteSelect = String.format("SELECT * FROM users WHERE username = '%1$s' AND password = '%2$s'", username, password);
		Connection conn = DriverManager.getConnection(connectionString);
		Statement s = conn.createStatement();
		ResultSet rs = s.executeQuery(sqliteSelect);
		
		while(rs.next())  i++;
		rs.close();
		conn.close();
		
		return i == 1;
	}

	public synchronized void runQuery(String sql) throws SQLException {
		this.createTableIfNotExist();
		Connection conn = DriverManager.getConnection(connectionString);
		Statement s = conn.createStatement();
		s.executeUpdate(sql);
		conn.close();
	}
}

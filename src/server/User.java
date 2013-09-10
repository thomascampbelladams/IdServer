package server;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

public class User implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2561498673899190864L;
	private String uuid;
	private String username;
	private String password;
	private String ip;
	private long logintime;
	private String name;
	
	/**
	 * Creates a new user record
	 * @param u User's UUID
	 * @param un User's Username
	 * @param pw User's Password
	 * @param ip User's IP
	 * @param lt the last time the user logged in
	 * @param n User's real name
	 */
	public User(String u, String un, String pw, String ip, long lt, String n){
		setUuid(u);
		setUsername(un);
		setPassword(pw);
		setIp(ip);
		setLogintime(lt);
		setName(n);
	}
	
	/**
	 * Creates a new user record
	 * @param rs ResultSet object from the database
	 * @throws SQLException
	 */
	public User(ResultSet rs) throws SQLException{
		setUuid(rs.getString("uuid"));
		setUsername(rs.getString("username"));
		setPassword(rs.getString("password"));
		setIp(rs.getString("ip"));
		setLogintime(rs.getLong("logintime"));
		setName(rs.getString("name"));
	}

	/**
	 * @return User's UUID
	 */
	public String getUuid() {
		return uuid;
	}

	/**
	 * 
	 * @param uuid Set's User's UUID
	 */
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	/**
	 * 
	 * @return User's username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * 
	 * @param username Sets User's username
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * 
	 * @return Gets User's password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * 
	 * @param password Sets User's password
	 */
	public void setPassword(String password) {
		this.password = password;
	}
	
	/**
	 * 
	 * @return Gets User's IP
	 */
	public String getIp() {
		return ip;
	}

	/**
	 * 
	 * @param ip Sets User's IP
	 */
	public void setIp(String ip) {
		this.ip = ip;
	}

	/**
	 * 
	 * @return Gets User's last login time
	 */
	public long getLogintime() {
		return logintime;
	}

	/**
	 * 
	 * @param logintime Sets User's last login time
	 */
	public void setLogintime(long logintime) {
		this.logintime = logintime;
	}

	/**
	 * 
	 * @return Gets user's real name
	 */
	public String getName() {
		return name;
	}

	/**
	 * 
	 * @param name Sets User's real name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public String toString()
	{
		return ("UUID: " + uuid + " \n" +
				"Username: " + username + " \n" +
				"IP: " + ip + " \n" +
				"Login time: " + logintime + " \n" +
				"Login name: " + name);
				
	}
}

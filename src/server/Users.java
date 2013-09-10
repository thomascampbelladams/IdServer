package server;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Users implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7010169898392437543L;
	private ArrayList<User> UserList;
	
	/**
	 * Constructs a User Record List
	 * @param rs ResultSet Object returned from a SQL Select call.
	 * @throws SQLException
	 */
	public Users(ResultSet rs) throws SQLException{
		setUserList(new ArrayList<User>());
		while(rs.next()){
			getUserList().add(new User(rs));
		}
	}

	/**
	 * 
	 * @return Gets User list
	 */
	public ArrayList<User> getUserList() {
		return UserList;
	}

	/**
	 * 
	 * @param userList Sets User list
	 */
	public void setUserList(ArrayList<User> userList) {
		UserList = userList;
	}
}

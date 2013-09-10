package server;

import java.io.Serializable;

public class SqlAction implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3475232658603598524L;
	private String sql;
	private int time;
	String getSql() {
		return sql;
	}
	int getTime() {
		return time;
	}
	public SqlAction(String sql, int time){
		this.sql = sql;
		this.time = time;
	}
}

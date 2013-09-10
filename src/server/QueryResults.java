package server;

import java.io.Serializable;

public class QueryResults implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -435052354205993597L;
	private String query;
	private boolean successful;
	private User user;
	private int changedRows;
	public String getQuery() {
		return query;
	}
	public boolean isSuccessful() {
		return successful;
	}
	public User getUser() {
		return user;
	}
	public int getChangedRows() {
		return changedRows;
	}
	public QueryResults(String query, User user){
		this.query = query;
		this.user = user;
	}
	public QueryResults(String query, boolean successful){
		this.query = query;
		this.successful = successful;
	}
	public QueryResults(String query, int changedRows){
		this.query = query;
		this.changedRows = changedRows;
	}
}

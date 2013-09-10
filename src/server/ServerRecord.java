package server;

public class ServerRecord {
	private String ip;
	private int serverNumber;
	private IdInterServerMessage ism;
	
	public String getIp() {
		return ip;
	}
	public ServerRecord(String ip, int serverNumber){
		this.ip = ip;
		this.serverNumber = serverNumber;
	}
	public int getServerNumber() {
		return serverNumber;
	}
}

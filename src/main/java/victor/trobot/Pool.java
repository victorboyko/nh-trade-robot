package victor.trobot;

public class Pool {
	
	private String host;
	private int port;
	private String user;
	private String pass;

	public Pool(String host, int port, String user, String pass) {
		this.host = host;
		this.port = port;
		this.user = user;
		this.pass = pass;
	}
	
	public String getHost() {
		return host;
	}
	
	public int getPort() {
		return port;
	}
	
	public String getUser() {
		return user;
	}
	
	public String getPass() {
		return pass;
	}
}

package victor.trobot.util;

public enum Algo {

	EQUIHASH (24),
	LBRY (23);
	
	public final int code;
	
	private Algo(int code) {
		this.code = code;
	}
	
}

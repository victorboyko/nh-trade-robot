package victor.trobot.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.io.IOUtils;

public class HttpUtil {
	
	public static String getStringByURL(URL jsonURL) throws IOException {
		HttpURLConnection conn;
		String jsonText = null;
		InputStream httpIn = null;
		
		try {
			conn = (HttpURLConnection) jsonURL.openConnection();
			
			conn.setRequestMethod("GET");
	        conn.setRequestProperty("Content-Type", 
	                   "application/x-www-form-urlencoded");
	        conn.setRequestProperty("Content-Language", "en-US"); 
	        conn.setRequestProperty("User-Agent",
	                "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17.0.963.56 Safari/535.11");
	        conn.setUseCaches(false);
	        conn.setDoInput(true);
	        conn.setDoOutput(true);
			
			httpIn = new BufferedInputStream(conn.getInputStream());     
			jsonText = IOUtils.toString(httpIn);
		} finally {
			IOUtils.closeQuietly(httpIn);
		}
		
		return jsonText;
	}

}

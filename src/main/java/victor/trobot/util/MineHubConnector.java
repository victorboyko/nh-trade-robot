package victor.trobot.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class MineHubConnector {
	
	public static class Coin {
		private String name;
		private double price;
		
		public Coin(String name, double price) {
			this.name = name;
			this.price = price;
		}
		
		@Override
		public String toString() {
			return String.format(name +":" + price);
		}
		
		public String getName() {
			return name;
		}
		
		public double getPrice() {
			return price;
		}
	}
	
	public static Map<String, Coin> getBPCReducedPrices(double reductionPercent, Set<String> limit) throws IOException, ParseException {
		Map<String, Coin> result = new HashMap<>();
		
		String currData = HttpUtil.getStringByURL(new URL("http://proxypool.info:8018/minehub?lbry=1000000&key=123123123&equihash=1000000"));
		JSONObject topJson = (JSONObject)new JSONParser().parse(currData);
		for(Object node : topJson.entrySet()) {
			Map.Entry entry = (Map.Entry)node;
			String name = entry.getKey().toString();
			if (limit != null && !limit.contains(name)) {
				continue;
			}
			JSONArray vals = (JSONArray)entry.getValue();
			Double btcPrice = Double.valueOf(vals.get(1).toString()) * (1 - reductionPercent/100);
			result.put(name, new Coin(name, btcPrice));
		}
		
//		Collections.sort(result, (a,b)->{
//			return Double.valueOf(b.getPrice()).compareTo(Double.valueOf(a.getPrice()));
//		});
		
		return result;
	}

}

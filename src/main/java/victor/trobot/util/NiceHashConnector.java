package victor.trobot.util;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import victor.trobot.Pool;

public class NiceHashConnector {
	
	public static class Order {
		public int id, workers;
		//Algo algo;
		public double price, acceptedSpeed, btc_avail, limit_speed;
		
		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(Order.this);
			//return String.format("{price: %5.4f, speed: %5.4f\n}", price, acceptedSpeed);
		}
		
	}
	
	public static List<Order> getMyOrders(Location location, Algo algo, String id, String key) throws IOException, ParseException {
		return getOrders(location, algo, id, key);
	}

	public static List<Order> getAllOrders(Location location, Algo algo) throws IOException, ParseException {
		return getOrders(location, algo, null, null);
	}
	
	private static List<Order> getOrders(Location location, Algo algo, String id, String key) throws IOException, ParseException {
		List<Order> result = new ArrayList<>();
		String jsonStr = HttpUtil.getStringByURL(new URL(
				(id == null) ? String.format("https://api.nicehash.com/api?method=orders.get&location=%d&algo=%d", location.code, algo.code)
							 : String.format("https://api.nicehash.com/api?method=orders.get&location=%d&algo=%d&my&id=%s&key=%s", location.code, algo.code, id, key)
			    )
		);
		JSONObject topJson = (JSONObject)new JSONParser().parse(jsonStr);
		JSONArray ordersJson = (JSONArray)((JSONObject)topJson.get("result")).get("orders");
		
		
		for(Object node : ordersJson) {
			JSONObject itemJson = (JSONObject)node;
			Order order = new Order();
			int type = Integer.valueOf(itemJson.get("type").toString());
			if (type == 1) {
				continue; // ignoring fixed orders
			}
			order.id	 			= Integer.valueOf(itemJson.get("id").toString());
			order.price 			= Double.valueOf(itemJson.get("price").toString());
			order.acceptedSpeed 	= 1000 * Double.valueOf(itemJson.get("accepted_speed").toString());
			if (itemJson.get("btc_avail") != null) {
				order.btc_avail = Double.valueOf(itemJson.get("btc_avail").toString());
			}
			if (itemJson.get("limit_speed") != null) {
				order.limit_speed = Double.valueOf(itemJson.get("limit_speed").toString());
			}
			if (itemJson.get("workers") != null) {
				order.workers = Integer.valueOf(itemJson.get("workers").toString());
			}

			result.add(order);
		}
		Collections.sort(result, (a, b) -> {return Double.compare(b.price, a.price);} );
		return result;
	}
	
	/**
	 * @return id of created order
	 * @throws IOException
	 * @throws ParseException
	 * @throws IllegalStateException if NH didn't return 'success'
	 */
	public static int createOrder(String apiId, String apiKey, Location location, Algo algo, double amount, double price, double limit, Pool pool) 
			throws IOException, ParseException {
		String jsonStr = HttpUtil.getStringByURL(new URL(String.format(
				"https://api.nicehash.com/api?method=orders.create&id=%s&key=%s&location=%d&algo=%d&amount=%2.5f&price=%2.4f&limit=%2.4f"
				+ "&pool_host=%s&pool_port=%d&pool_user=%s&pool_pass=%s", 
					apiId, apiKey, location.code, algo.code, amount, price, limit, pool.getHost(), pool.getPort(), pool.getUser(), pool.getPass())));
		JSONObject topJson = (JSONObject)new JSONParser().parse(jsonStr);
		Object o = topJson.get("result");
		if (o == null || !o.toString().contains("success")) {
			throw new IllegalStateException(jsonStr);
		}
		return extractOrderId(o.toString());
	}
	
	public static void removeOrder(String apiId, String apiKey, Location location, Algo algo, int orderId) throws IOException, ParseException {
		String jsonStr = HttpUtil.getStringByURL(new URL(String.format(
				"https://api.nicehash.com/api?method=orders.remove&id=%s&key=%s&location=%d&algo=%d&order=%d", 
					apiId, apiKey, location.code, algo.code, orderId)));
		JSONObject topJson = (JSONObject)new JSONParser().parse(jsonStr);
		Object o = topJson.get("result");
		if (o == null || !o.toString().contains("success") || !o.toString().contains("Order removed.")) {
			throw new IllegalStateException(jsonStr);
		}
	}
	
	static int extractOrderId(String nhResultMessage) {
		int begin = nhResultMessage.indexOf('#');
		return Integer.valueOf(nhResultMessage.substring(begin+1, nhResultMessage.indexOf(' ', begin)));
	}
	
	public static void updateOrderPrice(String apiId, String apiKey, Location location, Algo algo, int orderId, double price) throws IOException, ParseException {
		String jsonStr = HttpUtil.getStringByURL(new URL(String.format(
				"https://api.nicehash.com/api?method=orders.set.price&id=%s&key=%s&location=%d&algo=%d&order=%d&price=%2.4f", 
					apiId, apiKey, location.code, algo.code, orderId, price)));
		JSONObject topJson = (JSONObject)new JSONParser().parse(jsonStr);
		Object o = topJson.get("result");
		if (o == null || !o.toString().contains("success")) {
			throw new IllegalStateException(jsonStr);
		}
	}

	public static void main(String[] args) throws Exception {
		//System.out.println(getAllOrders(Location.US, Algo.LBRY));
		System.out.println(getMyOrders(Location.EU, Algo.LBRY, "503949", "3335d78f-3fe6-6e6c-36e2-101a68938117"));
	}

}

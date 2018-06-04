package victor.trobot;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.simple.parser.ParseException;

import victor.trobot.util.Algo;
import victor.trobot.util.Location;
import victor.trobot.util.MineHubConnector;
import victor.trobot.util.MineHubConnector.Coin;
import victor.trobot.util.NiceHashConnector;
import victor.trobot.util.NiceHashConnector.Order;

public class Starter {

	private static Logger logger = Logger.getLogger(Starter.class);
	
	private static String apiId = "503949";
	private static String apiKey = "3335d78f-3fe6-6e6c-36e2-101a68938117";
	
	public static void main(String[] args) {
		
		
		
		String[] coinAbrs = new String[] {"LBC"};
		
		Map<String,Pool> pools = new HashMap<>();
		pools.put("LBC", new Pool("lbry.suprnova.cc", 6257, "log121.nh1", "d=2222"));
		
		//int algoIds[] = new int[] {24, 24, 24, 24};
		
		
		final double minBelowTH = 5.0d;
		
		Order currentOrder = null;
		
		mainLoop:
		while (true) {
			

			List<Order> orders = null;
			List<Order> myOrders = null;
			
			try {
				orders = NiceHashConnector.getAllOrders(Location.US, Algo.LBRY);
				myOrders = NiceHashConnector.getMyOrders(Location.US, Algo.LBRY, apiId, apiKey);
				if (myOrders.size() > 1) {
					for(Order o : myOrders) {
						try {
							NiceHashConnector.removeOrder(apiId, apiKey, Location.US, Algo.LBRY, o.id);
						} catch (Exception e) {
							logger.fatal("Failed to remove order #" + o.id); 
						}
					}
					// close all until investigated
					logger.error("More then 1 order opened");
					//TODO - close orders
					System.exit(-121);
				}
				currentOrder = (myOrders.isEmpty()) ? null : myOrders.get(0);
				
			} catch (IOException | ParseException e) {
				logger.error(e);
				waitForSomeTime();
				continue;
			}
			
			Map<String, Coin> minehubPrices = null;
			
			try {
				minehubPrices = MineHubConnector.getBPCReducedPrices(26.1d, new HashSet<>(Arrays.asList(coinAbrs)));
				if (minehubPrices.size() < coinAbrs.length) {
					logger.error("Can't get all requested prices from MineHub");
					try {
						if (currentOrder != null) {
							NiceHashConnector.removeOrder(apiId, apiKey, Location.US, Algo.LBRY, currentOrder.id);
						}
					} catch (Exception e) {
						logger.fatal("Failed to remove order #" + currentOrder.id); 
					}
					System.exit(-121);
				}
					
			} catch (IOException | ParseException e) {
				logger.error("Error getting mining info " + e);
				waitForSomeTime();
				continue mainLoop;
			}
			
			
			Coin lbcCoin = minehubPrices.get("LBC");
			
			if (currentOrder != null) {
				
				if (currentOrder.price > lbcCoin.getPrice()*1.03d) {
					logger.info(String.format("closing order #%d as it's now overpriced", currentOrder.id)); // TODO - add details
					try {
						NiceHashConnector.removeOrder(apiId, apiKey, Location.US, Algo.LBRY, currentOrder.id);
					} catch (Exception e) {
						logger.error("Failed to remove order #" + currentOrder.id); 
						// if the price situation doesn't change after some time - program will continiously try to remove the open order
					}
					waitForSomeTime();
					continue mainLoop;
				} else {
					// TODO - check if more money needs to be added					
				}				
				
			} 

			double belowTH = 0;
			int gotConnections = 0;
			for(int i = orders.size()-1; i >=0; i--) {
				Order order = orders.get(i);
				if (currentOrder != null && currentOrder.id == order.id) {
					continue; //skip own order
				}
				gotConnections += order.workers;
				belowTH += order.acceptedSpeed;
				if (belowTH >= minBelowTH && gotConnections > 650) {
					double price = order.price+0.0002d;
					if (price <= lbcCoin.getPrice()) {
						
						if (currentOrder == null) {						
							try {
								int orderId = NiceHashConnector.createOrder(apiId, apiKey, Location.US, Algo.LBRY, 0.01d, price, 20.0, pools.get("LBC"));
								logger.info(String.format("Order #%d created", orderId));
							} catch (Exception e) {
								logger.error("Failed to create new order: " + e);
							}							
						} else {
							if ((currentOrder.workers == 0 || currentOrder.acceptedSpeed < 1.0d) && Math.abs(currentOrder.price - price) >= 0.0001d) {
								try {
									NiceHashConnector.updateOrderPrice(apiId, apiKey, Location.US, Algo.LBRY, currentOrder.id, price);
									logger.info(String.format("Order #%d price updated to %2.4f", currentOrder.id, price));
								} catch (Exception e) {
									logger.error(String.format("Failed to update price to %2.4f, exception : %s ", price , e.toString()));
								}
							}
							
						}
						
					} else {							
						// else do nothing and wait for a spike
						logger.debug(String.format("Min power treshold achieved, but the price is bad: need %2.4f, got %2.4f",
								lbcCoin.getPrice(), price));
					}
					
					waitForSomeTime();
					continue mainLoop;
				}
				
			}
		
			
			waitForSomeTime();
			
		}

	}
	
	private static void waitForSomeTime() {
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}

}

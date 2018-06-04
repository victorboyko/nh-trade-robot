package victor.trobot.util;

import static org.junit.Assert.*;

import org.junit.Test;

import victor.trobot.util.NiceHashConnector;

public class NiceHashConnectorTest {

	@Test
	public void testExtractOrderId() {
		assertTrue(NiceHashConnector.extractOrderId("\"success\":\"Order #5569 created.\"") == 5569 );
	}

}

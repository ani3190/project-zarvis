package zarvis.bakery.models;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.junit.Test;

import com.google.gson.Gson;
import zarvis.bakery.models.Order;

public class OrderTest {

	final String FILENAME = "src/main/test/Order.json";

	@Test
	public void test() throws UnsupportedEncodingException, IOException {
		
		BufferedReader reader = new BufferedReader(new FileReader(FILENAME));
		Order c = new Gson().fromJson(reader, Order.class);
		System.out.println(c);
		assertNotNull(FILENAME +" doesn't exist!!",reader);
		assertEquals(c.getGuid(),"order-001");
		assertEquals(c.getOrder_date().getDay(),1);
		assertEquals(c.getOrder_date().getHour(),9);
		assertEquals(c.getDelivery_date().getDay(),2);
		assertEquals(c.getDelivery_date().getHour(),10);
		assertEquals(c.getCustomer_id(),"customer-001");
		assertEquals(c.getProducts().get("Berliner").intValue(),9);

	}

}

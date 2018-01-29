package zarvis.bakery.models;

import static org.junit.Assert.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import org.junit.Test;
import com.google.gson.Gson;
import zarvis.bakery.models.Customer;


public class CustomerTest {
	final String FILENAME = "src/main/test/Customer.json";

	@Test
	public void test() throws UnsupportedEncodingException, IOException {
		
		BufferedReader reader = new BufferedReader(new FileReader(FILENAME));
		Customer c = new Gson().fromJson(reader, Customer.class);
		System.out.println(c);
		assertNotNull(FILENAME +" doesn't exist!!",reader);
		assertEquals(c.getGuid(),"customer-001");
		assertEquals(c.getName(),"King's Landing Shop");
		assertEquals(c.getLocation().getX(),-1,0);
		assertEquals(c.getLocation().getY(),-2,0);

		
		
	}

}

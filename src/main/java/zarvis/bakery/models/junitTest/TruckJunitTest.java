package zarvis.bakery.models.junitTest;

import static org.junit.Assert.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import org.junit.Test;
import com.google.gson.Gson;
import zarvis.bakery.models.Truck;

public class TruckJunitTest {

	final String FILENAME = "src/main/test/Truck.json";

	@Test
	public void test() throws UnsupportedEncodingException, IOException {
		
		BufferedReader reader = new BufferedReader(new FileReader(FILENAME));
		Truck c = new Gson().fromJson(reader, Truck.class);
		System.out.println(c);
		assertNotNull(FILENAME +" doesn't exist!!",reader);
		assertEquals(c.getGuid(),"truck-001");
		assertEquals(c.getLoad_capacity(),26);
		assertEquals(c.getLocation().getY(), -1,0);
		assertEquals(c.getLocation().getX(), 3,0);
	}

}

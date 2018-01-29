package zarvis.bakery.models.junitTest;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.junit.Test;
import com.google.gson.Gson;
import zarvis.bakery.models.Node;

public class NodeJunitTest {

	final String FILENAME = "src/main/test/Node.json";

	@Test
	public void test() throws UnsupportedEncodingException, IOException {
		
		BufferedReader reader = new BufferedReader(new FileReader(FILENAME));
		Node c = new Gson().fromJson(reader, Node.class);
		System.out.println(c);
		assertNotNull(FILENAME +" doesn't exist!!",reader);
		assertEquals(c.getGuid(),"node-032");
		assertEquals(c.getType(),"customer");
		assertEquals(c.getLocation().getY(), -1,0);
		assertEquals(c.getLocation().getX(), 3,0);
		assertEquals(c.getCompany(), "customer-030");
		assertEquals(c.getName(), "Wildfire Alchemist Society");
	}

}

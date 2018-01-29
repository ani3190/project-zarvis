package zarvis.bakery.models;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import org.junit.Test;
import com.google.gson.Gson;
import zarvis.bakery.models.StreetNetwork;

public class StreetNetworkTest {
	final String FILENAME = "src/main/test/StreetNetwork.json";

			@Test
			public void test() throws UnsupportedEncodingException, IOException {
				
				BufferedReader reader = new BufferedReader(new FileReader(FILENAME));
				StreetNetwork c = new Gson().fromJson(reader, StreetNetwork.class);
				System.out.println(c);
				assertNotNull(FILENAME +" doesn't exist!!",reader);
				assertEquals(c.getLinks().get(1).getGuid(),"edge-002");
				assertEquals(c.getLinks().get(1).getSource(),"node-032");
				assertEquals(c.getLinks().get(1).getTarget(),"node-001");
				assertEquals(c.getLinks().get(1).getDist(),1,0);
				assertEquals(c.getNodes().get(0).getGuid(),"node-032");
				assertEquals(c.getNodes().get(0).getType(),"customer");
				assertEquals(c.getNodes().get(0).getLocation().getY(), -1,0);
				assertEquals(c.getNodes().get(0).getLocation().getX(), 1,0);
				assertEquals(c.getNodes().get(0).getCompany(), "customer-030");
				assertEquals(c.getNodes().get(0).getName(), "Wildfire Alchemist Society");
				
	}

}

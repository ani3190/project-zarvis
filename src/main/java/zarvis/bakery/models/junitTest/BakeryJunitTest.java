package zarvis.bakery.models.junitTest;

import static org.junit.Assert.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import org.junit.Test;
import com.google.gson.Gson;
import zarvis.bakery.models.Bakery;


public class BakeryJunitTest {
	final String FILENAME = "src/main/test/Bakery.json";
	
	@Test
	public void test() throws UnsupportedEncodingException, IOException {
		BufferedReader reader = new BufferedReader(new FileReader(FILENAME));
		Bakery b = new Gson().fromJson(reader, Bakery.class);
		System.out.println(b);
		assertEquals(b.getGuid(),"bakery-001");
		assertEquals(b.getName(),"Sunspear Bakery");
		assertEquals(b.getOvens().get(0).getGuid(),"oven-001");
		assertEquals(b.getOvens().get(0).getCooling_rate(),3);
		assertEquals(b.getProducts().get(6).getGuid(),"Cookie");
		assertEquals(b.getProducts().get(6).getBaking_time(),12);
		assertEquals(b.getDough_prep_tables().get(1).getGuid(),"prep-table-002");
		assertEquals(b.getKneading_machines().get(1).getGuid(),"kneading-machine-002");
		assertEquals(b.getLocation().getX(),3,0);
		assertEquals(b.getLocation().getY(),1,0);
		
		
	}
}

package zarvis.bakery.models.junitTest;

import static org.junit.Assert.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import org.junit.Test;
import com.google.gson.Gson;
import zarvis.bakery.models.Oven;

public class OvenJunitTest {

	//final String FILENAME = "src/main/test/Oven.json";
	final String FILENAME = "/home/yassine/WS17_yboukn2s/project-zarvis/src/main/test/Oven.json";

		@Test
		public void test() throws UnsupportedEncodingException, IOException {
			
			BufferedReader reader = new BufferedReader(new FileReader(FILENAME));
			Oven c = new Gson().fromJson(reader, Oven.class);
			System.out.println(c);
			assertNotNull(FILENAME +" doesn't exist!!",reader);
			assertEquals(c.getGuid(),"oven-001");
			assertEquals(c.getCooling_rate(),3);
			assertEquals(c.getHeating_rate(),3);
			
	}

}

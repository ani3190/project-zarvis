package zarvis.bakery.models;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.junit.Test;

import com.google.gson.Gson;

import zarvis.bakery.models.Link;

public class LinkTest {

	final String FILENAME = "src/main/test/Link.json";

				@Test
				public void test() throws UnsupportedEncodingException, IOException {
					
					BufferedReader reader = new BufferedReader(new FileReader(FILENAME));
					Link c = new Gson().fromJson(reader, Link.class);
					System.out.println(c);
					assertNotNull(FILENAME +" doesn't exist!!",reader);
					assertEquals(c.getGuid(),"edge-002");
					assertEquals(c.getSource(),"node-032");
					assertEquals(c.getTarget(),"node-001");
					assertEquals(c.getDist(),1,0);
					
		}

	}

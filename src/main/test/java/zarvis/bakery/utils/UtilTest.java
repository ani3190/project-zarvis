package zarvis.bakery.utils;

import static org.junit.Assert.*;

import java.io.File;
import java.util.HashMap;
import java.util.TreeMap;
import org.junit.Test;
import zarvis.bakery.utils.Util;

public class UtilTest {

	@Test
	//for testing sortMapByvalue() method
	public void sortMapByValueJunitTest() {
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		map.put("str3", 3);
		map.put("str2", 2);
		map.put("str1", 1);
		TreeMap<String, Integer> result = new TreeMap<String, Integer>();
		result.put("str1", 1);
		result.put("str2", 2);
		result.put("str3", 3);
		
		
		assertEquals(result,Util.sortMapByValue(map));
	}
	//for testing getWrapper() method
	@Test
	public void getWrapperJunitTest(){
		File file = new File("src/main/config/random-scenario.json");
		assertNotNull(file);
		
		
	}

}

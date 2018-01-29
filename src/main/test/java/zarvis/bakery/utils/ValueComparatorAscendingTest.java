package zarvis.bakery.utils;

import static org.junit.Assert.*;

import java.util.HashMap;

import org.junit.Test;
import zarvis.bakery.utils.ValueComparatorAscending;

public class ValueComparatorAscendingTest {

	@Test
	public void compareTest() {
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		map.put("str1", 1);
		map.put("str2", 2);
		map.put("str3", 3);
		ValueComparatorAscending tester = new ValueComparatorAscending(map); //ValueComparatorAscending
																			// is tested
        // assert statements
        assertEquals(0, tester.compare("str1","str1"));
    }
}

package culmat.photo;

import static culmat.photo.Compare.equal;
import static java.nio.file.Files.list;
import static java.nio.file.Paths.get;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Stream;

import org.junit.Test;

import common.FactoryTreeMap;

public class CompareTest {

	
	private TreeMap<Path, SortedSet<Path>> group(Stream<Path> paths) {
		TreeMap<Path, SortedSet<Path>> ret = FactoryTreeMap.create((p1,p2)-> {
			try {
				return equal(p1, p2) ? 0: p1.compareTo(p2);
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
		}, (p)-> new TreeSet<Path>((p1, p2)->{
			int sizes = Long.compare(p2.toFile().length(), p1.toFile().length());
			return sizes == 0 ? p1.compareTo(p2) : sizes;
		}));
	
		paths.forEach((p)-> {
			SortedSet<Path> grouped = ret.get(p);
			grouped.add(p);
		});
		return ret;
	}
	
	
	@Test
	public void testConflict1() throws Exception {
		Iterator<SortedSet<Path>> grouped = group(list(get("src/test/resources/conflict1"))).values().iterator();
		SortedSet<Path> paths = grouped.next();
		assertEquals(3, paths.size());
		paths.forEach((p)->assertTrue(p.getFileName().toString().contains("2015-07-03_22-41-14_250")));
		assertTrue(paths.first().toString().endsWith("nef"));
		paths = grouped.next();
		assertEquals(3, paths.size());
		assertTrue(paths.first().toString().endsWith("nef"));
		paths.forEach((p)->assertTrue(p.getFileName().toString().contains("2015-07-04_16-58-16_900")));
	}
	
	@Test
	public void testConflict2() throws Exception {
		TreeMap<Path, SortedSet<Path>> keys = group(list(get("src/test/resources/conflict2")));
		assertEquals(8, keys.size());
		
	}
	
	@Test
	public void testConflict3() throws Exception {
		TreeMap<Path, SortedSet<Path>> keys = group(list(get("src/test/resources/conflict3")));
		assertEquals(2, keys.size());
	}
	
	@Test
	public void testConflict4() throws Exception {
		TreeMap<Path, SortedSet<Path>> keys = group(list(get("src/test/resources/conflict4")));
		assertEquals(1, keys.size());
	}

}

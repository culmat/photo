package culmat.photo;

import static java.nio.file.Paths.get;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class DateHelperTest {

	@Test
	public void testGetPath() throws Exception {
		assertEquals("2016/03/2016-03-27_13-06-45_640", DateHelper.getPath(Meta.getDate(get("src/test/resources/_MM03592.NEF"))));
	}

	
	@Test
	public void testGetDate() throws Exception {
		assertEquals("1978/10/1978-10-09_23-24-25_000", DateHelper.getPath(Meta.com_drew_metadata_Directory.getDate("1977:22:09 23:24:25", "0000")));
		assertEquals("1978/10/1978-10-09_23-24-25_007", DateHelper.getPath(Meta.com_drew_metadata_Directory.getDate("1977:22:09 23:24:25", "0007")));
		assertEquals("1978/10/1978-10-09_23-24-25_070", DateHelper.getPath(Meta.com_drew_metadata_Directory.getDate("1977:22:09 23:24:25", "0070")));
		assertEquals("1978/10/1978-10-09_23-24-25_530", DateHelper.getPath(Meta.com_drew_metadata_Directory.getDate("1977:22:09 23:24:25", "0530")));
		assertEquals("1978/10/1978-10-09_23-24-25_640", DateHelper.getPath(Meta.com_drew_metadata_Directory.getDate("1977:22:09 23:24:25", "64")));
	}
}

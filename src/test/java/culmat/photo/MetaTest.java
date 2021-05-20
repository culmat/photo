package culmat.photo;

import static culmat.photo.Meta.getDate;
import static culmat.photo.Meta.getDateFromFileName;
import static java.nio.file.Paths.get;
import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.Test;

public class MetaTest {

	
	private void assertDate(String expected, Date actual) {
		assertEquals(expected,Meta.DATE_FORMAT.format(actual));
	}
	
	@Test
	public void testGetDateFromFileName() throws Exception {
		assertDate("20131223133454", getDateFromFileName("2013-12-23_13-34-54.jpg"));
		assertDate("20131223143454", getDateFromFileName("20131223.14:34:54.nef"));
		assertDate("20181123173152", getDateFromFileName("20181123_173152(0).jpg"));
	}
	
	@Test
	public void meta() throws Exception {
		assertEquals("27 Mar 2016 13:06:45 GMT",getDate(get("src/test/resources/_MM03592.jpg")).toGMTString());
		assertEquals("27 Mar 2016 13:06:45 GMT",getDate(get("src/test/resources/_MM03592.NEF")).toGMTString());
		assertEquals("24 Aug 2019 19:19:24 GMT",getDate(get("src/test/resources/20190824_191924.dng")).toGMTString());
	}

}

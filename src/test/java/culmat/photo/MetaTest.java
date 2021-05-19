package culmat.photo;

import static culmat.photo.Meta.getDate;
import static culmat.photo.Meta.getDateFromFileName;
import static java.nio.file.Paths.get;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class MetaTest {

	
	@Test
	public void testGetDateFromFileName() throws Exception {
		assertEquals("23 Dec 2013 12:34:54 GMT", getDateFromFileName("2013-12-23_13-34-54.jpg").toGMTString());
		assertEquals("23 Dec 2013 13:34:54 GMT", getDateFromFileName("20131223.14:34:54.nef").toGMTString());
		assertEquals("23 Nov 2018 16:31:52 GMT", getDateFromFileName("20181123_173152(0).jpg").toGMTString());
	}
	
	@Test
	public void meta() throws Exception {
		assertEquals("27 Mar 2016 13:06:45 GMT",getDate(get("src/test/resources/_MM03592.jpg")).toGMTString());
		assertEquals("27 Mar 2016 13:06:45 GMT",getDate(get("src/test/resources/_MM03592.NEF")).toGMTString());
		assertEquals("24 Aug 2019 19:19:24 GMT",getDate(get("src/test/resources/20190824_191924.dng")).toGMTString());
	}

}

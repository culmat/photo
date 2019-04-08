package culmat.photo;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.Test;

public class PhotoTest {

	@Test
	public void testGetDateFromFileName() throws Exception {
		Photo photo = new Photo();
		assertEquals(new Date(113, 11, 23, 13, 34, 54), photo.getDateFromFileName("2013-12-23_13-34-54.jpg"));
		assertEquals(new Date(113, 11, 23, 14, 34, 54), photo.getDateFromFileName("20131223.14:34:54.nef"));
		assertEquals(new Date(118, 10, 23, 17, 31, 52), photo.getDateFromFileName("20181123_173152(0).jpg"));
		
	}

}

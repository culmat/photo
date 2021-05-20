package culmat.photo;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;

import org.junit.Test;

public class PhotoTest {

	@Test
	public void testDEcoder() throws Exception {
		Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("JPG");
		while (readers.hasNext()) {
		    System.out.println("reader: " + readers.next());
		}
	}
	
	@Test
	public void testGetExtension() throws Exception {
		assertEquals("jpg", Photo.getExtension(Paths.get("aa.JPG")));
	}
	
	@Test
	public void testAdapt() throws Exception {
		final Path tmp = Files.createTempFile("tmp", ".jpg");
		createTmpFile(tmp);
		final Path adapted = Photo.adapt(tmp);
		assertEquals(tmp.toString().replace(".jpg", "_1.jpg"), adapted.toString());
		
		createTmpFile(adapted);
		final Path adapted2 = Photo.adapt(tmp);
		assertEquals(tmp.toString().replace(".jpg", "_2.jpg"), adapted2.toString());
	}

	private void createTmpFile(final Path tmp) throws IOException {
		File tmpFile = tmp.toFile();
		tmpFile.createNewFile();
		tmpFile.deleteOnExit();
	}
	
	

}

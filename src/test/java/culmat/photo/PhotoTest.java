package culmat.photo;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

public class PhotoTest {

	@Test
	public void testGetExtension() throws Exception {
		assertEquals("jpg", Photo.getExtension(Paths.get("aa.JPG")));
	}
	
	@Test
	public void testAdapt() throws Exception {
		final Path tmp = Files.createTempFile("tmp", ".jpg");
		createTmpFile(tmp);
		final Path adapted = Photo.adapt(tmp);
		assertEquals(tmp.toString().replace(".jpg", "_A1.jpg"), adapted.toString());
		
		createTmpFile(adapted);
		final Path adapted2 = Photo.adapt(tmp);
		assertEquals(tmp.toString().replace(".jpg", "_A2.jpg"), adapted2.toString());
	}

	private void createTmpFile(final Path tmp) throws IOException {
		File tmpFile = tmp.toFile();
		tmpFile.createNewFile();
		tmpFile.deleteOnExit();
	}
	
	

}

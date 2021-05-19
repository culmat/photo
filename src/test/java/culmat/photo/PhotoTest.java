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
		assertEquals("png", Photo.getExtension(Paths.get("aa.PNG")));
	}
	
	@Test
	public void testAdapt() throws Exception {
		final Path tmp = Files.createTempFile("tmp", ".png");
		createTmpFile(tmp);
		final Path adapted = Photo.adapt(tmp);
		assertEquals(tmp.toString().replace(".png", "_1.png"), adapted.toString());
		
		createTmpFile(adapted);
		final Path adapted2 = Photo.adapt(tmp);
		assertEquals(tmp.toString().replace(".png", "_2.png"), adapted2.toString());
	}

	private void createTmpFile(final Path tmp) throws IOException {
		File tmpFile = tmp.toFile();
		tmpFile.createNewFile();
		tmpFile.deleteOnExit();
	}

}

package culmat.photo;

import static culmat.photo.Checksum.hash;
import static java.nio.file.Paths.get;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ChecksumTest {

		@Test
		public void testHashJPG() throws Exception {
			assertEquals("dfdaf7525731696fcbea3d32d1c48380a001d0e4", hash(get("src/test/resources/_MM03592.jpg")));
		}
		@Test
		public void testHashNEF() throws Exception {
			assertEquals("4d1bc98fee340b165bcfe5fa60926039b905cbae", hash(get("src/test/resources/_MM03592.NEF")));
		}
		@Test
		public void testHashDNG() throws Exception {
			assertEquals("19d24ac8a9c4b0756d2182531a93f90610e00834", hash(get("src/test/resources/20190824_191924.dng")));
		}

}

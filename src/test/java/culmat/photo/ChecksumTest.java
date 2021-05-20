package culmat.photo;

import static culmat.photo.Checksum.hash;
import static java.nio.file.Paths.get;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ChecksumTest {

		@Test
		public void testHashJPG() throws Exception {
			assertEquals("6d05d9f6e44d9b6f3f5ce743078dcbb99fbd0fd0", hash(get("src/test/resources/_MM03592.thumb.jpg")));
		}
		
		@Test
		public void testHashJPGPano() throws Exception {
			assertEquals("37a6cc84a19fee24f0a1428eef4e6822e95b20e0", hash(get("src/test/resources/2017-09-24_15-42-42.jpg")));
		}
		@Test
		public void testHashNEF() throws Exception {
			if(!Checksum.HAS_DCRAW) return;
			assertEquals("4d1bc98fee340b165bcfe5fa60926039b905cbae", hash(get("src/test/resources/_MM03592.NEF")));
		}
		@Test
		public void testHashDNG() throws Exception {
			if(!Checksum.HAS_DCRAW) return;
			assertEquals("19d24ac8a9c4b0756d2182531a93f90610e00834", hash(get("src/test/resources/20190824_191924.dng")));
		}
}

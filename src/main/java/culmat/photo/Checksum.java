package culmat.photo;

import static java.util.Arrays.asList;
import static org.apache.commons.codec.digest.DigestUtils.sha1Hex;
import static org.apache.commons.imaging.Imaging.getBufferedImage;
import static org.apache.commons.imaging.Imaging.writeImageToBytes;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.imaging.ImageFormats;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;

public class Checksum {

	public static final Set<String> EXTENSIONS = new TreeSet<>(asList("jpg", "jpeg"));
	
	private static boolean hasDCRaw() {
		try {
			int exitcode = new ProcessBuilder("dcraw").start().waitFor();
			if(exitcode == 1) {
				System.out.println("dcraw detected");
				EXTENSIONS.addAll(asList("dng", "nef"));
				return true;
			}
		} catch (IOException | InterruptedException e) {}
		finally {
			System.out.println("supported extensions : "+EXTENSIONS);
		}
		System.out.println("/!\\ dcraw not detected");		
		System.out.println("To enable processing of RAW images please install https://de.wikipedia.org/wiki/Dcraw");		
		return false;
	}
	
	public final static boolean HAS_DCRAW = hasDCRaw();
	
	
	
	public static String hash(Path path) throws IOException {
		switch (Photo.getExtension(path)) {
		case "dng":
		case "nef":
			Process proc = new ProcessBuilder("dcraw", "-d", "-j", "-t", "0,", "-c",
					path.toAbsolutePath().normalize().toString()).start();
			final List<String> sha = new ArrayList<>(1);
			new Thread(() -> {
				try {
					sha.add(DigestUtils.sha1Hex(proc.getInputStream()));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}).run();
			try {
				proc.waitFor();
			} catch (InterruptedException letsgo) {}
			return sha.get(0);
		default:
			try {
				return sha1Hex(writeImageToBytes(getBufferedImage(path.toFile()), ImageFormats.BMP, new HashMap<>()));
			} catch (ImageWriteException | ImageReadException e) {
				throw new IOException(e);
			}
		}
	}

}

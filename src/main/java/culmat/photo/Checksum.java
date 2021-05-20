package culmat.photo;

import static java.util.Arrays.asList;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.imageio.ImageIO;

import org.apache.commons.codec.digest.DigestUtils;

public class Checksum {

	public static final Set<String> EXTENSIONS = new TreeSet<>(asList("jpg", "jpeg"));
	
	private static boolean hasDCRaw() {
		try {
			int exitcode = new ProcessBuilder("dcraw2").start().waitFor();
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
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(ImageIO.read(path.toFile()), "bmp", baos);
			return DigestUtils.sha1Hex(baos.toByteArray());
		}
	}

}

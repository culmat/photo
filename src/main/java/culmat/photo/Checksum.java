package culmat.photo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.codec.digest.DigestUtils;

public class Checksum {

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

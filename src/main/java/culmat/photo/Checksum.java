package culmat.photo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public interface Checksum {
	default byte[] calcSHA1(File file) throws FileNotFoundException, IOException, NoSuchAlgorithmException {

		MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
		try (InputStream input = new FileInputStream(file)) {

			byte[] buffer = new byte[8192];
			int len = input.read(buffer);

			while (len != -1) {
				sha1.update(buffer, 0, len);
				len = input.read(buffer);
			}

			return sha1.digest();
		}
	}
}

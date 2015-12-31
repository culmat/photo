package culmat.photo;

import static java.lang.String.format;
import static java.nio.file.Files.list;
import static java.util.UUID.randomUUID;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;

public class Photo implements Consumer<Path> {

	private final Path input;
	private final Path output;

	public Photo(String input, String output) {
		this.input = Paths.get(input);
		this.output = Paths.get(output);
	}

	public static void main(String[] args) throws Exception {
//		new Photo("input", "output").iterate();
		new Photo("/Users/matthi/Pictures/Samsung/Camera", "/Volumes/Multimedia/Photos").iterate();
		
	}

	private void iterate() throws IOException {
		list(input).forEach(this);
	}

	@Override
	public void accept(Path path) {
		try {
			List<String> extensions = Arrays.asList(".jpg", ".nef", ".jpeg");
			String extension = getExtension(path);
			if(!extensions.contains(extension.toLowerCase())) {
				System.out.println("Ignoring " + path);
				return;
			}
			System.out.println(path);
			Metadata metadata = ImageMetadataReader.readMetadata(Files.newInputStream(path));
			ExifSubIFDDirectory dic = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
			Date date = dic.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
			String targetDir = DateHelper.getPath(date);
			output.resolve(targetDir).getParent().toFile().mkdirs();
			Files.move(path,checkAndAdapt(path,output.resolve(targetDir+extension)));

		} catch (IOException | ImageProcessingException | NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

	}

	private Path checkAndAdapt(Path source, Path dest) throws NoSuchAlgorithmException, IOException {
		return dest.toFile().exists() && unequal(source, dest)? adapt(dest) : dest;
	}

	private boolean unequal(Path source, Path dest) throws IOException, NoSuchAlgorithmException {
		if(calcSHA1(source.toFile()).equals(calcSHA1(dest.toFile()))) throw new IOException(format("File %s and %s are equal", source, dest));
		return true;
	}

	private static String calcSHA1(File file) throws FileNotFoundException, IOException, NoSuchAlgorithmException {

		MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
		try (InputStream input = new FileInputStream(file)) {

			byte[] buffer = new byte[8192];
			int len = input.read(buffer);

			while (len != -1) {
				sha1.update(buffer, 0, len);
				len = input.read(buffer);
			}

			return new HexBinaryAdapter().marshal(sha1.digest());
		}
	}

	private Path adapt(Path path) {
		String extension = getExtension(path);
		return Paths.get(path.toString().replace(extension, "_" + randomUUID() + extension));
	}

	private String getExtension(Path path) {
		String fileName = path.toString();
		int i = fileName.lastIndexOf('.');
		return i > 0 ? fileName.substring(i) : "";
	}

}

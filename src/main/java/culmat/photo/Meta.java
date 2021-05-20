package culmat.photo;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;

public class Meta {

	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");

	public Meta() {
		// TODO Auto-generated constructor stub
	}

	public static Date getDate(Path path) throws ImageProcessingException, IOException {
		Date date;
		Metadata metadata = readMeta(path);
		date = getDateTagSubIFD(metadata);
		if (date == null)
			date = getDateTagIFD0(metadata);
		if (date == null)
			date = getDateFromFileName(path.getFileName().toString());
		return date;
	}

	public static Date getDateFromFileName(final String string) {
		try {
			return DATE_FORMAT.parse(extractDateString(string));
		} catch (ParseException e) {
			e.getMessage();
			return null;
		}
	}

	public static String extractDateString(String string) {
		return string.
				replaceAll("\\D+", "").
				substring(0, Math.min(string.length(), 14));
	}

	public static Date getDateTagIFD0(Metadata metadata) {
		ExifIFD0Directory dic = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
		if (dic == null)
			return null;
		Date date = dic.getDate(ExifIFD0Directory.TAG_DATETIME_ORIGINAL);
		if (date != null)
			return date;
		date = dic.getDate(ExifIFD0Directory.TAG_DATETIME);
		if (date != null)
			return date;
		date = dic.getDate(ExifIFD0Directory.TAG_DATETIME_DIGITIZED);
		return date;
	}

	public static Date getDateTagSubIFD(Metadata metadata) {
		ExifSubIFDDirectory dic = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
		if (dic == null)
			return null;
		Date date = dic.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
		if (date != null)
			return date;
		date = dic.getDate(ExifSubIFDDirectory.TAG_DATETIME);
		if (date != null)
			return date;
		date = dic.getDate(ExifSubIFDDirectory.TAG_DATETIME_DIGITIZED);
		return date;
	}

	private static void dump(Directory directory) {
		for (Tag tag : directory.getTags()) {
			System.out.println(tag);
		}
	}

	public static void dump(Metadata metadata) {
		for (Directory directory : metadata.getDirectories()) {
			dump(directory);
		}
	}

	public static Metadata readMeta(Path path) throws IOException, ImageProcessingException {
		try (InputStream  is = Files.newInputStream(path)){
			return ImageMetadataReader.readMetadata(is);
		}
	}

}

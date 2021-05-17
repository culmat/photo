package culmat.photo;

import static java.lang.String.format;
import static java.nio.file.Files.isSymbolicLink;
import static java.nio.file.Files.walk;
import static java.util.UUID.randomUUID;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.BooleanOptionHandler;
import org.kohsuke.args4j.spi.FileOptionHandler;
import org.kohsuke.args4j.spi.PathOptionHandler;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;

public class Photo implements Consumer<Path> {
	// mdls SIMG0054.JPG | grep reat

	private transient Checksum checksum = new Checksum() {};

	@Option(name = "-dest", handler = PathOptionHandler.class)
	private Path output = Paths.get("./photos-ordered/");
	@Option(name = "-source", handler = FileOptionHandler.class)
	private Path input = Paths.get(".");
	@Option(name = "-symlink", handler = BooleanOptionHandler.class)
	private boolean symlink;
	@Option(name = "-dryrun", handler = BooleanOptionHandler.class)
	private boolean dryrun;
	List<String> extensions = Arrays.asList(".jpg", ".nef", ".jpeg");

	public Photo checkParams() throws FileNotFoundException {
		checkPathExists(input);
		checkPathExists(output);
		return this;
	}

	private void checkPathExists(Path path) throws FileNotFoundException {
		if (!path.toFile().exists()) {
			throw new FileNotFoundException(path.toString());
		}
	}

	public static void main(String[] args) throws Exception {
		Photo photo = new Photo();
		CmdLineParser parser = new CmdLineParser(photo);
		try {
			parser.parseArgument(args);
		} catch (CmdLineException e) {
			System.err.println(e.getMessage());
			System.err.println("Photo [options...] arguments...");
			parser.printUsage(System.err);
			System.err.println();
			return;
		}

		photo.checkParams().iterate();
	}

	private void iterate() throws IOException {
		walk(input).forEach(this);
	}
	

	@Override
	public void accept(Path path) {
		try {
			if(path.toFile().isDirectory()) return;
			String extension = getExtension(path);
			if (path.getFileName().startsWith("._") || !extensions.contains(extension.toLowerCase())) {
				System.out.println("Ignoring " + path);
				return;
			}
			System.out.println(path);
			Metadata metadata = ImageMetadataReader.readMetadata(Files.newInputStream(path));
			Date date = getDateTagSubIFD(metadata);
			if (date == null)
				date = getDateTagIFD0(metadata);
			if (date == null)
				date = getDateFromFileName(path.getFileName().toString());
			if(date == null) {
				System.err.println("could not determine date. skipping");
			}
			String targetDir = DateHelper.getPath(date);
			mkdirs(output.resolve(targetDir).getParent());
			Path dest = null;
			try {
				dest = checkAndAdapt(path, output.resolve(targetDir + extension));
				path = resolveSymbolicLink(path);
				move(path, dest);
			} catch (EqualException e) {
				System.out.println(e.dest + " already exists");
				delete(resolveSymbolicLink(e.source));
				dest = e.dest;
			}
			if (symlink)
				Files.createSymbolicLink(path, dest);

		} catch (IOException | ImageProcessingException | NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

	}

	private void move(Path source, Path target) throws IOException {
		if (dryrun) {
			System.out.println(format("dryrun: moving %s to %s", source, target));
		} else {
			Files.move(source, target);
		}
	}
	
	private void mkdirs(Path path) throws IOException {
		if (dryrun) {
			System.out.println(format("dryrun: mkdirs %s", path));
		} else {
			path.toFile().mkdirs();
		}
	}
	
	private void delete(Path path) {
		if (dryrun) {
			System.out.println(format("dryrun: deleting %s", path));
		} else {
			path.toFile().delete();
		}
	}

	Date getDateFromFileName(String string) {
		string = string.replaceAll("\\D+", "");
		string = string.substring(0, Math.min(string.length(), 14));
		try {
			return new SimpleDateFormat("yyyyMMddHHmmss").parse(string);
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}

	public Path resolveSymbolicLink(Path path) throws IOException {
		if (isSymbolicLink(path)) {
			Path linkTarget = Files.readSymbolicLink(path);
			delete(path);
			path = linkTarget;
		}
		return path;
	}


	private Date getDateTagIFD0(Metadata metadata) {
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

	public Date getDateTagSubIFD(Metadata metadata) {
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

	private void dump(Metadata metadata) {
		for (Directory directory : metadata.getDirectories()) {
			dump(directory);
		}
	}

	public void dump(Directory directory) {
		for (Tag tag : directory.getTags()) {
			System.out.println(tag);
		}
	}

	private Path checkAndAdapt(Path source, Path dest) throws NoSuchAlgorithmException, IOException {
		return dest.toFile().exists() && unequal(source, dest) ? adapt(dest) : dest;
	}

	private boolean unequal(Path source, Path dest) throws IOException, NoSuchAlgorithmException {
		if (checksum.calcSHA1(source.toFile()).equals(checksum.calcSHA1(dest.toFile())))
			throw new EqualException(source, dest);
		return true;
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

package culmat.photo;

import static java.nio.file.Files.isSymbolicLink;
import static java.nio.file.Files.walk;
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

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.BooleanOptionHandler;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;

public class Photo implements Consumer<Path> {

	private Path input;
	private Path output;

	@Option(name="-dest")        
    private String outputParam = "/Volumes/Multimedia/Photos";
	@Option(name="-source")        
	private String sourceParam = ".";
	@Option(name="-symlink",handler=BooleanOptionHandler.class)
	private boolean symlink;
	
	public Photo init() throws FileNotFoundException {
		this.input = Paths.get(sourceParam);
		this.output = Paths.get(outputParam);
		if(!this.output.toFile().exists()) {
			throw new FileNotFoundException(outputParam);
		}
		return this;
	}

	public static void main(String[] args) throws Exception {
		Photo photo = new Photo();
		CmdLineParser parser = new CmdLineParser(photo);
		try {
            parser.parseArgument(args);
        } catch( CmdLineException e ) {
            System.err.println(e.getMessage());
            System.err.println("Photo [options...] arguments...");
            parser.printUsage(System.err);
            System.err.println();
            return;
        }

		photo.init().iterate();
	}

	private void iterate() throws IOException {
		walk(input).forEach(this);
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
			Date date = getDateTagSubIFD(metadata);
			if(date == null) date = getDateTagIFD0(metadata);
			String targetDir = DateHelper.getPath(date);
			output.resolve(targetDir).getParent().toFile().mkdirs();
			Path dest = null;
			try {
				dest = checkAndAdapt(path,output.resolve(targetDir+extension));
				path = resolveSymbolicLink(path);
				Files.move(path,dest);
			} catch (EqualException e) {
				System.out.println(e.dest +" already exists");
				resolveSymbolicLink(e.source).toFile().delete();
				dest = e.dest;
			}
			if(symlink) Files.createSymbolicLink(path , dest);

		} catch (IOException | ImageProcessingException | NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

	}

	public Path resolveSymbolicLink(Path path) throws IOException {
		if(isSymbolicLink(path)) {
			Path linkTarget = Files.readSymbolicLink(path);
			path.toFile().delete();
			path = linkTarget;
		}
		return path;
	}

	private Date getDateTagIFD0(Metadata metadata) {
		ExifIFD0Directory dic = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
		Date date = dic.getDate(ExifIFD0Directory.TAG_DATETIME_ORIGINAL);
		if(date != null) return date;
		date = dic.getDate(ExifIFD0Directory.TAG_DATETIME);
		if(date != null) return date;
		date =  dic.getDate(ExifIFD0Directory.TAG_DATETIME_DIGITIZED);
		return date;
	}

	public Date getDateTagSubIFD(Metadata metadata) {
		ExifSubIFDDirectory dic = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
		Date date = dic.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
		if(date != null) return date;
		date = dic.getDate(ExifSubIFDDirectory.TAG_DATETIME);
		if(date != null) return date;
		date =  dic.getDate(ExifSubIFDDirectory.TAG_DATETIME_DIGITIZED);
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
		return dest.toFile().exists() && unequal(source, dest)? adapt(dest) : dest;
	}

	private boolean unequal(Path source, Path dest) throws IOException, NoSuchAlgorithmException {
		if(calcSHA1(source.toFile()).equals(calcSHA1(dest.toFile()))) throw new EqualException(source, dest);
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

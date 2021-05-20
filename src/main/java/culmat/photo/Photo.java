package culmat.photo;

import static java.lang.String.format;
import static java.nio.file.Files.isSymbolicLink;
import static java.nio.file.Files.walk;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.BooleanOptionHandler;
import org.kohsuke.args4j.spi.PathOptionHandler;

public class Photo implements Consumer<Path> {
	// mdls SIMG0054.JPG | grep reat

	@Option(name = "-dest", handler = PathOptionHandler.class)
	private Path output = Paths.get("./photos-ordered/");
	@Option(name = "-source", handler = PathOptionHandler.class)
	private Path input = Paths.get(".");
	@Option(name = "-symlink", handler = BooleanOptionHandler.class)
	private boolean symlink;
	@Option(name = "-dryrun", handler = BooleanOptionHandler.class)
	private boolean dryrun;
	
	Set<File> directories = new TreeSet<>((File f1, File f2) -> {	
		String p1 = f2.getAbsolutePath();
		String p2 = f1.getAbsolutePath();
		int ret = Integer.compare(p1.length(), p2.length());
		if(ret == 0) ret = p1.compareTo(p2);
		return ret;
	});

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
		for (File dir: directories) {
			System.out.println(dir);
			if(dir.exists() && dir.list().length == 0) {
				System.out.println("removing empty dir "+ dir);
				dir.delete();
			}
		}
	}
	

	@Override
	public void accept(final Path path) {
		try {
			if(path.toString().toLowerCase().contains("thumb")) {
				System.out.println("Skipping thumb "+path.toAbsolutePath().normalize());
				return;
			}
			if(path.startsWith(output)) {
				System.out.println("Skipping output directory "+output.toAbsolutePath().normalize());
				return;
			}
			if(path.toFile().isDirectory()) {
				directories.add(path.toAbsolutePath().normalize().toFile());
				return;
			}
			String extension = getExtension(path);
			if (path.getFileName().startsWith("._") || !Checksum.EXTENSIONS.contains(extension)) {
				System.out.println("Ignoring " + path);
				return;
			}
			System.out.println(path);
			Date date = Meta.getDate(path);
			if(date == null) {
				System.err.println("Could not determine date. skipping");
				return;
			}

			Path dest = output.resolve(DateHelper.getPath(date) + "."+extension);
			mkdirs(dest.getParent());
			Path resolvedPath = resolveSymbolicLink(path);
			String hash = Checksum.hash(resolvedPath);
			Path hashPath = output.resolve("_hashes/"+hash.substring(0, 2)+"/"+hash);
			if(hashPath.toFile().exists()) {
				System.out.println(hashPath + " exists pointing to " + Files.readString(hashPath));
				delete(resolvedPath);
			} else {
				dest = adapt(dest);
				move(resolvedPath, dest);
				mkdirs(hashPath.getParent());
				Files.writeString(hashPath,dest.toString().replace(output.toString(),""));
			}
			Path xmpPath = resolveSymbolicLink(Paths.get(path+".xmp"));
			File xmpFile = xmpPath.toFile();
			File xmpFileDest = Paths.get(dest+".xmp").toFile();
			if(xmpFile.exists()) {
				if((!xmpFileDest.exists() ||xmpFile.lastModified() > xmpFileDest.lastModified() )) {
					move(xmpPath, xmpFileDest.toPath());
				} else {
					xmpFile.delete();
				}				
			}
			if (symlink)
				Files.createSymbolicLink(path, dest);

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void move(Path source, Path target) throws IOException {
		if (dryrun) {
			System.out.print("dryrun: ");
		} 
		System.out.println(format("moving %s to %s", source, target));
	    Files.move(source, target);
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

	public Path resolveSymbolicLink(Path path) throws IOException {
		if (isSymbolicLink(path)) {
			Path linkTarget = Files.readSymbolicLink(path);
			delete(path);
			path = linkTarget;
		}
		return path;
	}


	/**
	 * @param path
	 * @return
	 */
	static Path adapt(Path path) {
		final String extension = getExtension(path);
		final String pathString = path.toString();
		final String trunc = pathString.substring(0, pathString.length()- extension.length()-1);
		int i = 1;
		while (path.toFile().exists()) {
			path = Paths.get(trunc + "_" + (i++) + "."+ extension);
		} 
		return path;
	}

	public static String getExtension(Path path) {
		String fileName = path.toString();
		int i = fileName.lastIndexOf('.');
		return i > 0 ? fileName.substring(i+1).toLowerCase() : "";
	}
}

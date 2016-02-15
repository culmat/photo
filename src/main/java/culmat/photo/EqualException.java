package culmat.photo;

import static java.lang.String.format;

import java.io.IOException;
import java.nio.file.Path;

public class EqualException extends IOException {

	public final Path dest;
	public final Path source;

	public EqualException(Path source, Path dest) {
		super(format("File %s and %s are equal", source, dest));
		this.source = source;
		this.dest = dest;
	}

}

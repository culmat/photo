package culmat.photo;

import static org.apache.commons.codec.digest.DigestUtils.sha1Hex;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.makernotes.NikonType2MakernoteDirectory;

public class Compare {

	public static String hash(Path path) throws IOException {
		try(InputStream in = Files.newInputStream(path)) {
			return sha1Hex(in);
		}
	}
	static String getNikonID(Metadata metadata) throws IOException {
		try {
			StringBuilder ret = new StringBuilder();
			NikonType2MakernoteDirectory nikon = metadata.getFirstDirectoryOfType(NikonType2MakernoteDirectory.class);
			if(nikon != null) {
				if(nikon.containsTag(NikonType2MakernoteDirectory.TAG_EXPOSURE_SEQUENCE_NUMBER))
					ret.append(nikon.getLong(NikonType2MakernoteDirectory.TAG_EXPOSURE_SEQUENCE_NUMBER));
				if(nikon.containsTag(NikonType2MakernoteDirectory.TAG_CAMERA_SERIAL_NUMBER))
					ret.append(nikon.getLong(NikonType2MakernoteDirectory.TAG_CAMERA_SERIAL_NUMBER));
			}
			if(ret.isEmpty()) ret.append(UUID.randomUUID().toString());
			return ret.toString();
		} catch (MetadataException e) {
			throw new IOException(e);
		}
		
	}
	
	
	public static boolean equal(Path p1, Path p2) throws IOException {
		try {
			Metadata meta1 = ImageMetadataReader.readMetadata(p1.toFile());
			Metadata meta2 = ImageMetadataReader.readMetadata(p2.toFile());
			if(getNikonID(meta1).equals(getNikonID(meta2))) return true;
		} catch (ImageProcessingException e) {
			throw new IOException(e);
		}
		if(hash(p1).equals(hash(p2))) return true;
		return false;
	}

}

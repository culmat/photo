package culmat.photo;

import java.io.IOException;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata;


public class Meta {

	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");

	public static Date getDateFromFileName(final String string) {
		try {
			return DATE_FORMAT.parse(extractDateString(string));
		} catch (ParseException e) {
			e.getMessage();
			return null;
		}
	}

	public static String extractDateString(String string) {
		return string.replaceAll("\\D+", "").substring(0, Math.min(string.length(), 14));
	}
	
	public static Date getDate(final Path path) throws IOException {
		try {
			ImageMetadata metadata = Imaging.getMetadata(path.toFile());
			if(metadata == null) return null;
			Map<String, String> meta = metadata.getItems().stream().map(i -> i.toString().split(": ", 2))
					.collect(Collectors.toMap(t -> t[0], t -> t[1], (v1,v2)->v1));
			String date = meta
					.getOrDefault("DateTime", meta.getOrDefault("DateTimeOriginal", meta.get("DateTimeDigitized")))
					.replace("'", "");
			return date == null ? null : com_drew_metadata_Directory.getDate(date);
		} catch (ImageReadException e) {
			throw new IOException(e);
		}
	}

	private static class com_drew_metadata_Directory{
		/**
		 * as of
		 * https://github.com/drewnoakes/metadata-extractor/blob/master/Source/com/drew/metadata/Directory.java#L862
		 */
		public static java.util.Date getDate(String dateString) {
			String subsecond = null;
			TimeZone timeZone = null;

			java.util.Date date = null;

			// This seems to cover all known Exif and Xmp date strings
			// Note that " : : : : " is a valid date string according to the Exif spec
			// (which means 'unknown date'):
			// http://www.awaresystems.be/imaging/tiff/tifftags/privateifd/exif/datetimeoriginal.html
			String datePatterns[] = { "yyyy:MM:dd HH:mm:ss", "yyyy:MM:dd HH:mm", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm",
					"yyyy.MM.dd HH:mm:ss", "yyyy.MM.dd HH:mm", "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd'T'HH:mm", "yyyy-MM-dd",
					"yyyy-MM", "yyyyMMdd", // as used in IPTC data
					"yyyy" };

			// if the date string has subsecond information, it supersedes the subsecond
			// parameter
			Pattern subsecondPattern = Pattern.compile("(\\d\\d:\\d\\d:\\d\\d)(\\.\\d+)");
			Matcher subsecondMatcher = subsecondPattern.matcher(dateString);
			if (subsecondMatcher.find()) {
				subsecond = subsecondMatcher.group(2).substring(1);
				dateString = subsecondMatcher.replaceAll("$1");
			}

			// if the date string has time zone information, it supersedes the timeZone
			// parameter
			Pattern timeZonePattern = Pattern.compile("(Z|[+-]\\d\\d:\\d\\d|[+-]\\d\\d\\d\\d)$");
			Matcher timeZoneMatcher = timeZonePattern.matcher(dateString);
			if (timeZoneMatcher.find()) {
				timeZone = TimeZone.getTimeZone("GMT" + timeZoneMatcher.group().replaceAll("Z", ""));
				dateString = timeZoneMatcher.replaceAll("");
			}

			for (String datePattern : datePatterns) {
				try {
					DateFormat parser = new SimpleDateFormat(datePattern);
					if (timeZone != null)
						parser.setTimeZone(timeZone);
					else
						parser.setTimeZone(TimeZone.getTimeZone("GMT")); // don't interpret zone time

					date = parser.parse(dateString);
					break;
				} catch (ParseException ex) {
					// simply try the next pattern
				}
			}

			if (date == null)
				return null;

			if (subsecond == null)
				return date;

			try {
				int millisecond = (int) (Double.parseDouble("." + subsecond) * 1000);
				if (millisecond >= 0 && millisecond < 1000) {
					Calendar calendar = Calendar.getInstance();
					calendar.setTime(date);
					calendar.set(Calendar.MILLISECOND, millisecond);
					return calendar.getTime();
				}
				return date;
			} catch (NumberFormatException e) {
				return date;
			}
		}
	}

}

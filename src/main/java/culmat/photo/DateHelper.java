package culmat.photo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DateHelper {
	static final String DATEFORMAT = "yyyy/MM/yyyy-MM-dd_HH-mm-ss";
	static final SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT);
	static {
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	public static String getPath(Date date) {
		return sdf.format(date);
	}

}

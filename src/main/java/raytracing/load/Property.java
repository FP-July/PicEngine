package raytracing.load;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Property {

	public String key, value;
	
	public Property(String key, String value) {
		this.key = key;
		this.value = value;
	}

	private static Pattern pat = Pattern.compile("^(.*)=(.*)?$");
	
	public static Property getProperty(String conf) 
		throws NullPointerException {
		Matcher mat = pat.matcher(conf);
		if (mat.find()) {
			if (mat.groupCount() > 1) {
				Property prop = new Property(mat.group(1).trim(), mat.group(2).trim());
				return prop;
			} else {
				return null;
			}
		} else {
			throw new NullPointerException();
		}
	}
}

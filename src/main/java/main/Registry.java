package main;

import java.util.HashMap;
import java.util.Set;

public class Registry {

	private static HashMap<String, String> registry = new HashMap<String, String>();
	
	public static void registerJobAlias(String key, String value) {
		registry.put(key, value);
	}
	
	public static Set<String> getJob() {
		return registry.keySet();
	}
	
	public static String getJob(String key) {
		return registry.get(key);
	}
}

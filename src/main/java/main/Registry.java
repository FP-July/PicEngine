package main;

import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;

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
	
	public static String printUsage(int indent) {
		StringBuffer ind = new StringBuffer();
		for (int i = 0; i < indent; ++i)
			ind.append("\t");
		
		StringBuffer sb = new StringBuffer();
		for (Entry<String, String> entry : registry.entrySet()) {
			sb.append(ind.toString() + entry.getKey() + "\t:" + entry.getValue() + "\n");
		}
		return sb.toString();
	}
}

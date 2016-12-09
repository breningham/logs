package es.hiiberia.simpatico.utils;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

public class SimpaticoProperties {

	public static String elasticSearchIp;
	public static int elasticSearchPort;
	public static String elasticSearchClusterName;
	public static String elasticSearchIndex;
	public static String elasticSearchType;
	
	
	@SuppressWarnings("serial")
	public static boolean getStrings() {
		boolean result = false;
		
		ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("simpatico");
		
		try {
			// Database
			elasticSearchIp = RESOURCE_BUNDLE.getString("elasticsearch.ip");
			elasticSearchPort = Integer.parseInt(RESOURCE_BUNDLE.getString("elasticsearch.port"));
			elasticSearchClusterName = RESOURCE_BUNDLE.getString("elasticsearch.clustername");
			elasticSearchIndex = RESOURCE_BUNDLE.getString("elasticsearch.index");
			elasticSearchType = RESOURCE_BUNDLE.getString("elasticsearch.type");
			result = true;
		} catch (MissingResourceException e) {
			e.printStackTrace();
			Logger.getRootLogger().error("Properties file error");
		}
		
		return result;
	}
}

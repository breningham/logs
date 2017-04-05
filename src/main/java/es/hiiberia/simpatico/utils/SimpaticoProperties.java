package es.hiiberia.simpatico.utils;

import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

public class SimpaticoProperties {

	public static String simpaticoLog_Error = "errors";
	public static String simpaticoLog_Logs =  "logs";
	public static String simpaticoLog_Analytics = "analytics";
	public static String simpaticoLog_Piwik =  "piwik";
	
	public static String simpaticoLog_Test =  "test";
	
	// ES config
	public static String elasticSearchIp;
	public static int elasticSearchPort;
	public static String elasticSearchClusterName;
	
	// Logs
	public static String elasticSearchHIIndex;
	public static String elasticSearchSharedIndex;
	public static String elasticSearchCreatedFieldName;
	// Search
	public static String elasticSearchFieldSearch;
	// Piwik
	public static String piwikApiUrl;
	public static String piwikAuthToken;
	public static String elasticSearchPiwikIndex;
	public static String elasticSearchPiwikType;
	
	
	public static boolean getStrings() {
		boolean result = false;
		
		Properties RESOURCE_BUNDLE = new Properties();
		
		try {			
			RESOURCE_BUNDLE.load(SimpaticoProperties.class.getResourceAsStream("/simpatico.properties"));
			// Database
			elasticSearchIp = RESOURCE_BUNDLE.getProperty("elasticsearch.ip");
			elasticSearchPort = Integer.parseInt(RESOURCE_BUNDLE.getProperty("elasticsearch.port"));
			elasticSearchClusterName = RESOURCE_BUNDLE.getProperty("elasticsearch.clustername");
			
			elasticSearchHIIndex = RESOURCE_BUNDLE.getProperty("elasticsearch.hi.index");
			elasticSearchSharedIndex = RESOURCE_BUNDLE.getProperty("elasticsearch.shared.index");
			
			elasticSearchCreatedFieldName = RESOURCE_BUNDLE.getProperty("elasticsearch.created.field.name");
			elasticSearchFieldSearch = RESOURCE_BUNDLE.getProperty("elasticsearch.search.field");
			
			// Piwik
			piwikApiUrl = RESOURCE_BUNDLE.getProperty("piwik.api_url");
			piwikAuthToken = RESOURCE_BUNDLE.getProperty("piwik.auth_token");
			elasticSearchPiwikIndex = RESOURCE_BUNDLE.getProperty("elasticsearch.piwik.index");
			elasticSearchPiwikType = RESOURCE_BUNDLE.getProperty("elasticsearch.piwik.type");
			
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
			Logger.getRootLogger().error("Properties file error");
		}
		
		return result;
	}
}

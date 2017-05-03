package es.hiiberia.simpatico.utils;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

public class SimpaticoProperties {

	public static String simpaticoLog_Error = "errors";
	public static String simpaticoLog_Logs =  "logs";
	public static String simpaticoLog_Analytics = "analytics";
	public static String simpaticoLog_Piwik =  "piwik";
	
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
		
		ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("simpatico");
		
		try {			
			// Database
			elasticSearchIp = RESOURCE_BUNDLE.getString("elasticsearch.ip");
			elasticSearchPort = Integer.parseInt(RESOURCE_BUNDLE.getString("elasticsearch.port"));
			elasticSearchClusterName = RESOURCE_BUNDLE.getString("elasticsearch.clustername");
			
			elasticSearchHIIndex = RESOURCE_BUNDLE.getString("elasticsearch.hi.index");
			elasticSearchSharedIndex = RESOURCE_BUNDLE.getString("elasticsearch.shared.index");
			
			elasticSearchCreatedFieldName = RESOURCE_BUNDLE.getString("elasticsearch.created.field.name");
			elasticSearchFieldSearch = RESOURCE_BUNDLE.getString("elasticsearch.search.field");
			
			// Piwik
			piwikApiUrl = RESOURCE_BUNDLE.getString("piwik.api_url");
			piwikAuthToken = RESOURCE_BUNDLE.getString("piwik.auth_token");
			elasticSearchPiwikIndex = RESOURCE_BUNDLE.getString("elasticsearch.piwik.index");
			elasticSearchPiwikType = RESOURCE_BUNDLE.getString("elasticsearch.piwik.type");
			
			result = true;
		} catch (MissingResourceException e) {
			e.printStackTrace();
			Logger.getRootLogger().error("Properties file error");
		}
		
		return result;
	}
}

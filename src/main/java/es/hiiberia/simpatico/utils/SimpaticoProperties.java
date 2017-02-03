package es.hiiberia.simpatico.utils;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

public class SimpaticoProperties {

	public static String simpaticoLogsClients;
	public static String simpaticoAnalyticsClients;
	public static String elasticSearchIp;
	public static int elasticSearchPort;
	public static String elasticSearchClusterName;
	// Logs
	public static String elasticSearchLogsIndex;
	public static String elasticSearchLogsType;
	public static String elasticSearchCreatedFieldName;
	// Analytics
	public static String elasticSearchAnalyticsIndex;
	public static String elasticSearchAnalyticsType;
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
			// Simpatico
			simpaticoLogsClients = RESOURCE_BUNDLE.getString("simpatico.log4j.logs.clients");
			simpaticoAnalyticsClients = RESOURCE_BUNDLE.getString("simpatico.log4j.analytics.clients");
			
			// Database
			elasticSearchIp = RESOURCE_BUNDLE.getString("elasticsearch.ip");
			elasticSearchPort = Integer.parseInt(RESOURCE_BUNDLE.getString("elasticsearch.port"));
			elasticSearchClusterName = RESOURCE_BUNDLE.getString("elasticsearch.clustername");
			elasticSearchLogsIndex = RESOURCE_BUNDLE.getString("elasticsearch.logs.index");
			elasticSearchLogsType = RESOURCE_BUNDLE.getString("elasticsearch.logs.type");
			elasticSearchCreatedFieldName = RESOURCE_BUNDLE.getString("elasticsearch.logs.created.field.name");
			elasticSearchAnalyticsIndex = RESOURCE_BUNDLE.getString("elasticsearch.analytics.index");
			elasticSearchAnalyticsType = RESOURCE_BUNDLE.getString("elasticsearch.analytics.type");
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

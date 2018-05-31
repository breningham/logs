package es.hiiberia.simpatico.utils;

import java.util.ArrayList;
import java.util.List;
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
	
	// Session feedback configuration JSONs
	public static String elasticSearchSFQuestionsIndex;
	
	// Authentication AAC
	public static Boolean aacUse;
	public static String aacUrlServer;
	
	public static String aacGetAuthUser;
	public static String aacGetAuthPass;
	
	// Real IP header name
	public static String realIpHeaderName;
	// IPs allowed
	public static List<String> ipsAllowed = new ArrayList<>();
	
	// Authentication domains
	public static String domainNames;
	// Domains allowed
	public static List<String> domainsAllowed = new ArrayList<>();
	// Referer Domains allowed
	public static List<String> refererDomainsAllowed = new ArrayList<>();
	
	
	
	public static boolean getStrings() {
		boolean result = false;
		
		ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("simpatico");
		
		try {
			// AAC
			aacUse = Boolean.parseBoolean(RESOURCE_BUNDLE.getString("authentication.use"));
			aacUrlServer = RESOURCE_BUNDLE.getString("authentication.url");
			
			aacGetAuthUser = RESOURCE_BUNDLE.getString("authentication.getauth.user");
			aacGetAuthPass = RESOURCE_BUNDLE.getString("authentication.getauth.pass");
			
			
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
			
			// Session feedback configuration JSONs
			elasticSearchSFQuestionsIndex = RESOURCE_BUNDLE.getString("elasticsearch.sf.index");
			
			// Real Ip header name
			realIpHeaderName = RESOURCE_BUNDLE.getString("http.header.realip");
			String [] ips = RESOURCE_BUNDLE.getString("authentication.whitelist.ip").split(",");
			for (String ip : ips) {
				ipsAllowed.add(ip.trim());
			}
			
			// Domain
			String [] domains = RESOURCE_BUNDLE.getString("authentication.domains.allowed").split(",");
			for (String domain : domains) {
				domainsAllowed.add(domain.trim());
			}
			
			// Domain
			String [] refererDomains = RESOURCE_BUNDLE.getString("authentication.referers.domains.allowed").split(",");
			for (String refererDomain : refererDomains) {
				refererDomainsAllowed.add(refererDomain.trim());
			}
			
			result = true;
		} catch (MissingResourceException e) {
			e.printStackTrace();
			Logger.getRootLogger().error("Properties file error");
		}
		
		return result;
	}
}

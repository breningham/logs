package es.hiiberia.simpatico.filters;

import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;

import es.hiiberia.simpatico.rest.SimpaticoResourceUtils;
import es.hiiberia.simpatico.utils.SimpaticoProperties;
import eu.trentorise.smartcampus.profileservice.BasicProfileService;
import eu.trentorise.smartcampus.profileservice.model.BasicProfile;


public class AuthenticationFilter implements Filter {
	
	private static final long CACHE_VALIDITY = 1 * 60 * 60 * 1000; // 1 hour 
	
    private static class TokenCacheEntry {
    	long validUntil;
    	String userId;
    	
		public TokenCacheEntry(String userId) {
			this.userId = userId;
			validUntil = System.currentTimeMillis() + CACHE_VALIDITY;
		}
    	
    	boolean isValid()  {
    		return validUntil > System.currentTimeMillis();
    	}
    }	
    
    private static ConcurrentHashMap<String, TokenCacheEntry> tokenCache = new ConcurrentHashMap<>();
	
   private BasicProfileService profileService = null;
   
   private BasicProfileService getService() {
	   if (profileService == null) {
		   synchronized (profileService) {
			   if (profileService == null) {
				   profileService = new BasicProfileService(SimpaticoProperties.aacUrlServer);
			   }
		   }
	   }
	   return profileService;
   }
	
   public void init(FilterConfig filterConfig) throws ServletException {
	   Logger.getRootLogger().info("INIT SIMPATICO AUTHENTICATION FILTER");		

   }

   public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) {
	   
	   try {
		   String ipClient = request.getRemoteAddr();
		   Logger.getRootLogger().info("[Auth filter] IP request: " + ipClient);		
	       HttpServletRequest httpRequest = (HttpServletRequest) request;   
	       
	       //Logger.getRootLogger().debug(SimpaticoResourceUtils.getHeaders(httpRequest));   // Print all headers
	       
	       if (SimpaticoProperties.aacUse) {
	    	   
	    	   // Check allowed IPs
	    	   String proxyRealIp;
	           if ((proxyRealIp = SimpaticoResourceUtils.getRealIPHeader(httpRequest)) != null) { // If null -> Not using proxy (or header in simpatico.properties dont match)
	        	   Logger.getRootLogger().info("[Auth filter] Looks like there is a proxy server. Changing IP (" + ipClient + ") to 'real ip header' (" + proxyRealIp + ")");
	        	   ipClient = proxyRealIp;
	           }
	           if (SimpaticoProperties.ipsAllowed.contains(ipClient)) {
	    		   Logger.getRootLogger().debug("[Auth filter] IP exists in whitelist. Method: " + httpRequest.getMethod());
	    		   filterChain.doFilter(request, response);
	    		   return;
	    	   } else {
	    		   Logger.getRootLogger().debug("[Auth filter] IP doesnt exists in whitelist. IP: " + ipClient + ". Method: " + httpRequest.getMethod());
	    	   }
	    	   
	    	   Logger.getRootLogger().info("[Auth filter] Using filter. Method: " + httpRequest.getMethod());
	    	   if (httpRequest.getMethod().equalsIgnoreCase("GET")) {
	        	   // User/Pass base64 compare
	    		   String authRequest = httpRequest.getHeader("Authorization");
	    		   Logger.getRootLogger().debug("[Auth filter] AuthRequest: " + authRequest);
				   
	    		   if (authRequest != null) {
	    			   String [] splitAuthRequest = authRequest.split(" ");  // AuthRequest = Basic <token>
	    			   if (splitAuthRequest.length >= 2) {
	    				   String basicAuth = SimpaticoProperties.aacGetAuthUser + ":" + SimpaticoProperties.aacGetAuthPass;
	    				   byte[] bytes = basicAuth.getBytes("UTF-8");
	    				   String encoded = Base64.getEncoder().encodeToString(bytes);
	    				   
	    				   Logger.getRootLogger().info("[Auth filter]  My user/pass to encode: " + basicAuth + ". Encoded: " + encoded + ". Recv: " + splitAuthRequest[1]);
	    				   if (encoded.equals(splitAuthRequest[1])) {
	    					   filterChain.doFilter(request, response);
	    	        		   return;
	    				   } else {
	    					   Logger.getRootLogger().info("[Auth filter] Basic auth dont match (" + encoded + " recv: " + splitAuthRequest[1]);
	    				   }
	    			   }
	    		   }
	           } else { // POST/PUT/DELETE	        	   
	        	   String token = extractHeader(httpRequest.getHeader("Authorization"));
	        	   if (token != null) {
	        		   WrappedRequest myRequestWrapper = new WrappedRequest((HttpServletRequest) request);	            	   
					   JSONObject jsonRequest = new JSONObject(myRequestWrapper.getBody());
					   // take from cache if present
	        		   if (tokenCache.containsKey(token) && tokenCache.get(token).isValid()) {
						   if (jsonRequest.has("userID") && jsonRequest.getString("userID").equals(tokenCache.get(token).userId)) {
							   filterChain.doFilter(myRequestWrapper, response);
							   return;
						   } else {
							   Logger.getRootLogger().debug("[Auth filter] userId field doesnt match. Request data: " + jsonRequest.toString() + ". UserID: " + tokenCache.get(token).userId);
						   }
	        		   } else {
			        	   try {
			        		   BasicProfile profile = getService().getBasicProfile(token);
			        		   tokenCache.putIfAbsent(token, new TokenCacheEntry(profile.getUserId()));
							   if (jsonRequest.has("userID") && jsonRequest.getString("userID").equals(profile.getUserId())) {
								   filterChain.doFilter(myRequestWrapper, response);
								   return;
							   } else {
								   Logger.getRootLogger().debug("[Auth filter] userId field doesnt match. Request data: " + jsonRequest.toString() + ". Profile response: " + profile);
							   }
							} catch (Exception e) {
								   Logger.getRootLogger().error("[Auth filter] Exception: " + e.getMessage() + ". \n" + SimpaticoResourceUtils.exceptionStringifyStack(e, 10));
							}	            	   
	        			   
	        		   }		        	   
	        	   }

	           }
	    	   
	    	   response.resetBuffer();
	   		   response.getOutputStream().write("{\"message\": \"Access Denied\"}".getBytes());
	   		   HttpServletResponse hsr = (HttpServletResponse) response;
	   		   hsr.setStatus(403);
	   		   return;
	       } else {
	    	   filterChain.doFilter(request, response);
	       }
	   } catch (Exception e) {
		   Logger.getRootLogger().error("[Auth filter] Exception: " + e.getMessage() + ". \n" + SimpaticoResourceUtils.exceptionStringifyStack(e, 10));
		   Logger.getLogger(SimpaticoProperties.simpaticoLog_Error).error("[Auth filter] Exception: " + e.getMessage() + ". \n" + SimpaticoResourceUtils.exceptionStringifyStack(e, 10));
	   }
   }

   /**
 * @param header
 * @return
 */
private String extractHeader(String header) {
	if (header != null && header.toLowerCase().startsWith("bearer ")) {
		return header.substring(7);
	} 
	return null;
}

public void destroy() {
	   Logger.getRootLogger().info("DESTROY SIMPATICO AUTHENTICATION FILTER");		
   }
}
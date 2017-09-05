package es.hiiberia.simpatico.filters;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.log4j.Logger;

import es.hiiberia.simpatico.rest.SimpaticoResourceUtils;
import es.hiiberia.simpatico.utils.SimpaticoProperties;


public class NoAuthenticationFilter implements Filter {
	

	public static final String NO_AUTHENTICATION_PARAM = "NO_AUTHENTICATION";

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) {
	   
	   try {
		   request.setAttribute(NO_AUTHENTICATION_PARAM, true);
		   filterChain.doFilter(request, response);
	   } catch (Exception e) {
		   Logger.getRootLogger().error("[Auth filter] Exception: " + e.getMessage() + ". \n" + SimpaticoResourceUtils.exceptionStringifyStack(e, 10));
		   Logger.getLogger(SimpaticoProperties.simpaticoLog_Error).error("[Auth filter] Exception: " + e.getMessage() + ". \n" + SimpaticoResourceUtils.exceptionStringifyStack(e, 10));
	   }
   }

   public void init(FilterConfig filterConfig) throws ServletException {
	   Logger.getRootLogger().info("INIT SIMPATICO NO-AUTHENTICATION FILTER");		

   }

   public void destroy() {
	   Logger.getRootLogger().info("DESTROY SIMPATICO NO-AUTHENTICATION FILTER");		
   }
}
package es.hiiberia.simpatico.utils;

import java.lang.management.ManagementFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;

import es.hiiberia.simpatico.batch.PiwikBatch;
import es.hiiberia.simpatico.rest.InternalErrorException;

public class ServletContextClass implements ServletContextListener {

	public void contextInitialized(ServletContextEvent arg0) {
		if (!SimpaticoProperties.getStrings()){
			throw new InternalErrorException("Problems with SIMPATICO properties file");
		}
		
		String version = "1.10";

		// Start SIMPATICO
		PiwikBatch.getInstance(); // Start getting data from Piwik
		
		Logger.getLogger(SimpaticoProperties.simpaticoLog_Logs).info("\n\n\nNew Execution. SIMPATICO Version: " + version + " STARTED. PID: " + ManagementFactory.getRuntimeMXBean().getName());
		Logger.getLogger(SimpaticoProperties.simpaticoLog_Analytics).info("\n\n\nNew Execution. SIMPATICO Version: " + version + " STARTED. PID: " + ManagementFactory.getRuntimeMXBean().getName());
		Logger.getLogger(SimpaticoProperties.simpaticoLog_Piwik).info("\n\n\nNew Execution. SIMPATICO Version: " + version + " STARTED. PID: " + ManagementFactory.getRuntimeMXBean().getName());
		Logger.getLogger(SimpaticoProperties.simpaticoLog_Error).info("\n\n\nNew Execution. SIMPATICO Version: " + version + " STARTED. PID: " + ManagementFactory.getRuntimeMXBean().getName());
		Logger.getRootLogger().info("\n\n\nNew Execution. SIMPATICO Version: " + version + " STARTED. PID: " + ManagementFactory.getRuntimeMXBean().getName());
	}	
	
	public void contextDestroyed(ServletContextEvent arg0) {
		// Stop SIMPATICO		
		Logger.getRootLogger().info("SIMPATICO STOPPED");		
	}
}

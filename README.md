# Logs
Logs is a module for store/search/update multiple data from another modules.


## Installation Requirements
- Java 1.8+
- J2EE Servlet Container (Tomcat 7+)
- Elastic Search (5.0+)
- Java IDE (eclipse)
- Piwik 

## Configuration Elastic Search

  1. Open elasticsearch.yml and change clustername, ip and port (if you want)

## Configuration Java project

  1. Open simpatico.properties and set ip, port and cluster name
  2. Set piwik api url and token auth


## Installation

  1. Import java project into IDE and export like war file
  2. Deploy war file into Tomcat webapps folder
  3. Run elastic search  
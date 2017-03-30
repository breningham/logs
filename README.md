# Logs
Logs is a module for store/search/update multiple data from another modules.


## Installation Requirements
- Java 1.8+
- J2EE Servlet Container (Tomcat 7+)
- Elastic Search (5.0+)  				https://www.elastic.co/downloads/elasticsearch
- Java IDE (eclipse)
- Piwik 								https://piwik.org/docs/installation/

## Configuration Elastic Search

  1. Open elasticsearch.yml and change clustername, ip and port (if you want)

## Configuration Java project

  1. Open simpatico.properties and set ip, port and cluster name
  2. Set piwik api url and token auth


## Installation

  1. Export java project like war file
  2. Run elastic search  
  3. Deploy war file into Tomcat webapps folder
  
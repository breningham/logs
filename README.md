# Logs
Logs is a module for store/search/update multiple data from another modules.


## Installation Requirements
- Java 1.8+
- J2EE Servlet Container (Tomcat 7+)
- [Elastic Search (5.0+)](https://www.elastic.co/downloads/elasticsearch)
- [Java IDE (eclipse)](https://www.eclipse.org/downloads/?)
- [Piwik](https://piwik.org/docs/installation/)

## Configuration Elastic Search

  1. Open `elasticsearch.yml` and change `clustername, ip` and `port` if you want

## Configuration Piwik

  1. Open piwik and get `token_auth` by the [API](https://developer.piwik.org/api-reference/reporting-api#authenticate-to-the-api-via-token_auth-parameter)

## Configuration Swagger files

  1. Open each file in `src/main/webapp/dist/yaml_files` and change `host` and `basePath, schemes` if you want
  
## Configuration Java project

  1. Open `simpatico.properties` and set `ip, port` and `clustername`
  2. Set `piwik.api_url` and `piwik.auth_token`

## Installation

  1. Export java project like war file
  2. Run elastic search
  3. Deploy war file into Tomcat webapps folder
  

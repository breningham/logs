[![Build Status](https://travis-ci.org/SIMPATICOProject/logs.svg?branch=master)](https://travis-ci.org/SIMPATICOProject/logs)
[![codecov](https://codecov.io/gh/SIMPATICOProject/logs/branch/master/graph/badge.svg)](https://codecov.io/gh/SIMPATICOProject/logs)

# Logs
Logs is a module for store/search/update multiple data from another modules.


## Installation Requirements
- Java 1.8+
- J2EE Servlet Container (Tomcat 7+)
- [Elastic Search (5.0+)](https://www.elastic.co/downloads/elasticsearch)
- [Kibana (5.0+)](https://www.elastic.co/downloads/kibana) (Optional)
- [Java IDE (eclipse)](https://www.eclipse.org/downloads/?)
- [Piwik](https://piwik.org/docs/installation/)

## Configuration Elastic Search

  1. Open `elasticsearch.yml` and change `cluster.name`, `network.host` and `http.port`.
  2. Set `path.data` and `path.logs` (optional)

## Configuration Kibana

  1. Set `elasticsearch.url` to elastic search ip (localhost)

## Configuration Piwik

  1. Open piwik and get `token_auth` by the [API](https://developer.piwik.org/api-reference/reporting-api#authenticate-to-the-api-via-token_auth-parameter)

## Configuration Swagger files

  1. Open each file in `src/main/webapp/dist/yaml_files` and change `host` and `basePath, schemes` if you want
 
## Configuration Session Feedback Questions
  In the root of this project you can find a `SF_Configuration.pdf` file explaining how to create the JSON file use to create the questions for the Session Feedback modal at the end of each service. We are in the process of updating the Swagger file with the new endpoints and its structure, but in the meantime there is the `SF_Configuration.pdf` file.
  
  The goal is to create one JSON for each service in each language.
  
## Configuration Java project

  1. Open `simpatico.properties` and set `ip, port` and `clustername`
  2. Set `piwik.api_url` and `piwik.auth_token`

## Extra Configuration
  
  #### 1. Proxy 
  
  If you use a proxy server (i.e Nginx), set `proxy_set_header X-Real-IP $remote_addr;` in each Logs project location to be able to access to real ip client and the whitelist ip filter works fine.
  
## Installation

  1. Export java project like war file
  2. Run elastic search
  3. Deploy war file into Tomcat webapps folder
  

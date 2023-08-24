# coveocc
SAP Coveo OCC extension 

This extension extends SAP Commerce OCC extension with a new Rest Service that returns [Coveo Search Token Authentication](https://docs.coveo.com/en/56/build-a-search-ui/use-search-token-authentication)

## Installation

### 1. Copy extension folder to bin/custom directory

### 2. Add the Coveocc extension to the config/localextensions.xml file

### 3. add coveo org url and api key with impersonate privilege in coveocc/project.properties

```
# coveo platform url  ex https://platformstg.cloud.coveo.com
coveocc.org.url =
# org api key with impersonate privilege
coveocc.org.api.key=
```

### 4. Build

```
ant clean all
```

### 5. Start the server


## Test

to test locally you can use  swagger on this [url](https://localhost:9002/occ/v2/swagger-ui.html#/search-token-controller/getSearchToken)

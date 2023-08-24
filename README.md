# coveocc
SAP Coveo OCC extension 

This extension extends SAP Commerce OCC extension with a new Rest Service that returns [Coveo Search Token Authentication](https://docs.coveo.com/en/56/build-a-search-ui/use-search-token-authentication)

## Installation

### 1. Copy extension folder to bin/custom directory

### 2. Add the Coveocc extension to the config/localextensions.xml file

### 3. Build

```
ant clean all
```

### 4. Start the server

### 5. run update in hac with the selection of coveocc extension to create new introduced fields 

### 5. add coveo org url and api key with impersonate privilege in the backoffice , WCMS/website choose the corresponding site  in Coveo Api Key and Coveo Platform URL(ex https://platformstg.cloud.coveo.com) fields

## Test

to test locally you can use  swagger on this [url](https://localhost:9002/occ/v2/swagger-ui.html#/search-token-controller/getSearchToken)

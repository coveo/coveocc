# SAP Coveo OCC extension

This extension adds a new API to the *SAP Commerce OCC extension* to use [Coveo search token authentication](https://docs.coveo.com/en/56/build-a-search-ui/use-search-token-authentication) in your project.

## Installation

1. Copy the extension folder to the `hybris/bin/custom` directory of your project.

1. In the project folder, open the `hybris/config/localextensions.xml` file and add the `coveocc` extension:

   ```xml
   <extension name='coveocc' />
   ```

1. From the root of your project folder, run the following command:

   ```bash
   ant clean all
   ```

1. Start the server by executing the `hybris/bin/platform/hybrisserver.sh` script.

1. Update the project in the Hybris Administration Console:

   1. Open the Hybris Administration console at https://localhost:9002/platform/update.
   
   1. Find and select the checkboxes for `coveoccc` extension to create the newly introduced fields.

   1. At the top of the page, click the *Update* button.

1. Set the Coveo credentials in the Backoffice Administration Cockpit at https://localhost:9002/backoffice:

   1. In the Administration Cockpit, go to the **WCMS → Website** page.

   1. In the list of websites, double-click the required website.

   1. Switch to the *Administration* tab.

   1. Fill in the following fields:

      * *Coveo Platform URL*.

        For production, it must be `https://platform.cloud.coveo.com/`. For a testing environment, it can be `https://platformstg.cloud.coveo.com`.

      * *Coveo* [*API KEY*](https://docs.coveo.com/en/1718). The API key must have the `Impersonate` privilege granted.

        See details in link:{site-baseurl}/1707#search-impersonate-domain[Impersonate domain].

## Local testing

To test retrieving a search token, you can use a Swagger UI that's available at `https://localhost:9002/occ/v2/swagger-ui.html`, where you can find and test the `/coveo/token/{searchHub}` endpoint.

This endpoint returns a JSON object with the token property that contains the search token. 
Depending on the user type, the endpoint returns a JWT token for a logged-in user or an anonymous user.
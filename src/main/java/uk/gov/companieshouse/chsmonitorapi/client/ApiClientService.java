package uk.gov.companieshouse.chsmonitorapi.client;

import java.io.IOException;
import uk.gov.companieshouse.api.ApiClient;
import uk.gov.companieshouse.api.InternalApiClient;

public interface ApiClientService {

    ApiClient getOauthAuthenticatedClient(String ericPassThroughHeader) throws IOException;

    ApiClient getApiClient();

    InternalApiClient getInternalOauthAuthenticatedClient(String ericPassThroughHeader)
            throws IOException;

    InternalApiClient getInternalApiClient();
}

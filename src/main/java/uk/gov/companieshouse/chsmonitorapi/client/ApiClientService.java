package uk.gov.companieshouse.chsmonitorapi.client;

import uk.gov.companieshouse.api.ApiClient;
import uk.gov.companieshouse.api.InternalApiClient;

public interface ApiClientService {

//    InternalApiClient getInternalApiClient();

    ApiClient getApiClient();
}

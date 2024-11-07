package uk.gov.companieshouse.chsmonitorapi.client.impl;

import java.io.IOException;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.ApiClient;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.chsmonitorapi.client.ApiClientService;
import uk.gov.companieshouse.sdk.manager.ApiSdkManager;

@Component
public class ApiClientServiceImpl implements ApiClientService {

    @Override
    public ApiClient getOauthAuthenticatedClient(String ericPassThroughHeader) throws IOException {
        return ApiSdkManager.getSDK(ericPassThroughHeader);
    }

    @Override
    public ApiClient getApiClient() {
        return ApiSdkManager.getPrivateSDK();
    }

    @Override
    public InternalApiClient getInternalOauthAuthenticatedClient(String ericPassThroughHeader)
            throws IOException {
        return ApiSdkManager.getPrivateSDK(ericPassThroughHeader);
    }

    @Override
    public InternalApiClient getInternalApiClient() {
        return ApiSdkManager.getPrivateSDK();
    }
}
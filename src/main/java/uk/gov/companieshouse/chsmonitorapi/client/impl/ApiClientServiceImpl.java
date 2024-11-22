package uk.gov.companieshouse.chsmonitorapi.client.impl;

import java.io.IOException;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.chsmonitorapi.client.ApiClientService;
import uk.gov.companieshouse.sdk.manager.ApiSdkManager;

@Component
public class ApiClientServiceImpl implements ApiClientService {

    @Override
    public InternalApiClient getInternalApiClient(String passthroughHeader) throws IOException {
        return ApiSdkManager.getPrivateSDK(passthroughHeader);
    }
}
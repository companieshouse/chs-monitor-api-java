package uk.gov.companieshouse.chsmonitorapi.client.impl;

import java.io.IOException;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.ApiClient;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.chsmonitorapi.client.ApiClientService;
import uk.gov.companieshouse.sdk.manager.ApiSdkManager;

@Component
public class ApiClientServiceImpl implements ApiClientService {

//    @Override
//    public InternalApiClient getInternalApiClient() {
//        return ApiSdkManager.getPrivateSDK();
//    }

    @Override
    public ApiClient getApiClient() {
        return ApiSdkManager.getSDK();
    }
}
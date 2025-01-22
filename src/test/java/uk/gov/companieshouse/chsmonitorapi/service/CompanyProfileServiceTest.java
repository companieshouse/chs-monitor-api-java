package uk.gov.companieshouse.chsmonitorapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.handler.company.CompanyResourceHandler;
import uk.gov.companieshouse.api.handler.company.request.CompanyGet;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.chsmonitorapi.client.ApiClientService;
import uk.gov.companieshouse.chsmonitorapi.config.TestApplicationConfig;
import uk.gov.companieshouse.chsmonitorapi.exception.ServiceException;
import uk.gov.companieshouse.chsmonitorapi.service.impl.CompanyProfileServiceImpl;
import uk.gov.companieshouse.logging.Logger;

@ExtendWith(MockitoExtension.class)
@Import(TestApplicationConfig.class)
class CompanyProfileServiceTest {

    private static final String COMPANY_NUMBER = "12345678";
    @Mock
    private Logger logger;
    @Mock
    private ApiClientService apiClientService;
    @Mock
    private InternalApiClient internalApiClient;
    @Mock
    private ApiResponse<CompanyProfileApi> getCompanyDetailsApiResponse;
    @InjectMocks
    private CompanyProfileServiceImpl companyProfileService;
    @Mock
    private CompanyProfileApi companyProfileApi;
    @Mock
    private CompanyResourceHandler companyResourceHandler;
    @Mock
    private CompanyGet companyGet;

    @BeforeEach
    void setup() {
        companyProfileApi.setCompanyName("TestName");
        companyProfileApi.setCompanyNumber(COMPANY_NUMBER);
        companyProfileApi.setCompanyStatus("TestStatus");
    }

    @Test
    void testGetCompanyDetails_Success() throws Exception {
        companyProfileApi.setCompanyName("TestName");
        companyProfileApi.setCompanyNumber(COMPANY_NUMBER);
        companyProfileApi.setCompanyStatus("TestStatus");
        when(apiClientService.getApiClient()).thenReturn(internalApiClient);
        when(internalApiClient.company()).thenReturn(companyResourceHandler);
        when(internalApiClient.company().get(anyString())).thenReturn(companyGet);
        when(internalApiClient.company().get(anyString()).execute()).thenReturn(
                getCompanyDetailsApiResponse);
        when(getCompanyDetailsApiResponse.getData()).thenReturn(companyProfileApi);

        CompanyProfileApi response = companyProfileService.getCompanyDetails(COMPANY_NUMBER);

        assertEquals(companyProfileApi, response);
    }

    @Test
    void testGetCompanyDetails_ApiErrorResponseException()
            throws IOException, URIValidationException {
        when(apiClientService.getApiClient()).thenReturn(internalApiClient);
        when(internalApiClient.company()).thenReturn(companyResourceHandler);
        when(internalApiClient.company().get(anyString())).thenReturn(companyGet);
        when(internalApiClient.company().get(anyString()).execute()).thenThrow(
                new URIValidationException("Invalid URI"));

        ServiceException exception = assertThrows(ServiceException.class, () -> {
            companyProfileService.getCompanyDetails(COMPANY_NUMBER);
        });

        assertTrue(exception.getMessage().contains("Error Retrieving Company Details"));
    }

    @Test
    void testGetCompanyDetails_NullResponse() throws IOException, URIValidationException {

        when(apiClientService.getApiClient()).thenReturn(internalApiClient);
        when(internalApiClient.company()).thenReturn(companyResourceHandler);
        when(internalApiClient.company().get(anyString())).thenReturn(companyGet);
        when(internalApiClient.company().get(anyString()).execute()).thenReturn(
                getCompanyDetailsApiResponse);
        when(getCompanyDetailsApiResponse.getData()).thenReturn(null);

        ServiceException exception = assertThrows(ServiceException.class, () -> {
            companyProfileService.getCompanyDetails(COMPANY_NUMBER);
        });

        assertTrue(exception.getMessage()
                .contains("Company details not found for company number: 12345678"));
    }
}

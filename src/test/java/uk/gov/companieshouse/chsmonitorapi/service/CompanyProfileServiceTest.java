package uk.gov.companieshouse.chsmonitorapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import config.TestApplicationConfig;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.company.CompanyDetails;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.company.PrivateCompanyDetailResourceHandler;
import uk.gov.companieshouse.api.handler.company.request.PrivateCompanyDetailsGet;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.chsmonitorapi.client.ApiClientService;
import uk.gov.companieshouse.chsmonitorapi.exception.ServiceException;
import uk.gov.companieshouse.chsmonitorapi.service.impl.CompanyProfileServiceImpl;
import uk.gov.companieshouse.logging.Logger;

@ExtendWith(MockitoExtension.class)
@Import(TestApplicationConfig.class)
class CompanyProfileServiceTest {

    @Mock
    private Logger logger;

    @Mock
    private ApiClientService apiClientService;

    @Mock
    private InternalApiClient internalApiClient;

    @Mock
    private PrivateCompanyDetailResourceHandler privateCompanyDetailResourceHandler;

    @Mock
    private PrivateCompanyDetailsGet getPrivateCompanyDetails;

    @Mock
    private ApiResponse<CompanyDetails> getCompanyDetailsApiResponse;

    @InjectMocks
    private CompanyProfileServiceImpl companyProfileService;

    private static final String COMPANY_NUMBER = "12345678";
    private static final String GET_COMPANY_DETAILS_URI = "/company/12345678/company-detail";

    @Test
    void testGetCompanyDetails_Success() throws Exception {
        CompanyDetails companyDetails = new CompanyDetails()
                .companyName("TestName")
                .companyNumber(COMPANY_NUMBER)
                .companyStatus("TestStatus");

        when(apiClientService.getInternalApiClient()).thenReturn(internalApiClient);
        when(internalApiClient.privateCompanyDetailResourceHandler()).thenReturn(
                privateCompanyDetailResourceHandler);
        when(privateCompanyDetailResourceHandler
                .getCompanyDetails(GET_COMPANY_DETAILS_URI))
                .thenReturn(getPrivateCompanyDetails);
        when(getPrivateCompanyDetails.execute()).thenReturn(getCompanyDetailsApiResponse);
        when(getCompanyDetailsApiResponse.getData()).thenReturn(companyDetails);

        var response = companyProfileService.getCompanyDetails(COMPANY_NUMBER);

        assertEquals(companyDetails, response);
        verify(privateCompanyDetailResourceHandler).getCompanyDetails(
                GET_COMPANY_DETAILS_URI);
    }

    @Test
    void testGetCompanyDetails_URIValidationException()
            throws ApiErrorResponseException, URIValidationException {
        when(apiClientService.getInternalApiClient()).thenReturn(internalApiClient);
        when(internalApiClient.privateCompanyDetailResourceHandler()).thenReturn(
                privateCompanyDetailResourceHandler);
        when(privateCompanyDetailResourceHandler
                .getCompanyDetails(GET_COMPANY_DETAILS_URI))
                .thenReturn(getPrivateCompanyDetails);
        when(getPrivateCompanyDetails.execute()).thenThrow(
                new URIValidationException("Invalid URI"));

        ServiceException exception = assertThrows(ServiceException.class, () -> {
            companyProfileService.getCompanyDetails(COMPANY_NUMBER);
        });

        assertTrue(exception.getMessage().contains("Error Retrieving Company Details"));
    }

    @Test
    void testGetCompanyDetails_IOException()
            throws ApiErrorResponseException, URIValidationException {
        when(apiClientService.getInternalApiClient()).thenReturn(internalApiClient);
        when(internalApiClient.privateCompanyDetailResourceHandler()).thenReturn(
                privateCompanyDetailResourceHandler);
        when(privateCompanyDetailResourceHandler
                .getCompanyDetails(GET_COMPANY_DETAILS_URI))
                .thenReturn(getPrivateCompanyDetails);
        when(getPrivateCompanyDetails.execute()).thenThrow(
                ApiErrorResponseException.fromIOException(new IOException("Some error happened")));

        ServiceException exception = assertThrows(ServiceException.class, () -> {
            companyProfileService.getCompanyDetails(COMPANY_NUMBER);
        });

        assertTrue(exception.getMessage().contains("Error Retrieving Company Details"));
    }

    @Test
    void testGetCompanyDetails_NullResponse()
            throws ApiErrorResponseException, URIValidationException {

        when(apiClientService.getInternalApiClient()).thenReturn(internalApiClient);
        when(internalApiClient.privateCompanyDetailResourceHandler()).thenReturn(
                privateCompanyDetailResourceHandler);
        when(privateCompanyDetailResourceHandler
                .getCompanyDetails(GET_COMPANY_DETAILS_URI))
                .thenReturn(getPrivateCompanyDetails);
        when(getPrivateCompanyDetails.execute()).thenReturn(getCompanyDetailsApiResponse);
        when(getCompanyDetailsApiResponse.getData()).thenReturn(null);

        ServiceException exception = assertThrows(ServiceException.class, () -> {
            companyProfileService.getCompanyDetails(COMPANY_NUMBER);
        });

        assertTrue(exception.getMessage()
                .contains("Company details not found for company number: 12345678"));
    }
}

package uk.gov.companieshouse.chsmonitorapi.service.impl;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriTemplate;
import uk.gov.companieshouse.api.company.CompanyDetails;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.chsmonitorapi.client.ApiClientService;
import uk.gov.companieshouse.chsmonitorapi.exception.ServiceException;
import uk.gov.companieshouse.chsmonitorapi.service.CompanyProfileService;
import uk.gov.companieshouse.logging.Logger;

@Service
public class CompanyProfileServiceImpl implements CompanyProfileService {

    private static final UriTemplate GET_COMPANY_DETAILS_URI = new UriTemplate(
            "/company/{companyNumber}/company-detail");
    private final ApiClientService apiClientService;
    private final Logger logger;

    @Autowired
    public CompanyProfileServiceImpl(ApiClientService apiClientService, Logger logger) {
        this.apiClientService = apiClientService;
        this.logger = logger;
    }

    @Override
    public CompanyDetails getCompanyDetails(String companyNumber) throws ServiceException {
        var uri = GET_COMPANY_DETAILS_URI.expand(companyNumber).toString();

        try {
            CompanyDetails companyDetails = apiClientService.getInternalApiClient()
                    .privateCompanyDetailResourceHandler().getCompanyDetails(uri).execute()
                    .getData();

            if (companyDetails == null) {
                throw new ServiceException(
                        "Company details not found for company number: " + companyNumber);
            }
            return companyDetails;
        } catch (URIValidationException | IOException e) {
            var message = "Error Retrieving Company Details: " + companyNumber;
            logger.error(message, e);
            throw new ServiceException(message, e);
        }
    }
}

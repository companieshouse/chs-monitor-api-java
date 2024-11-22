package uk.gov.companieshouse.chsmonitorapi.service;

import uk.gov.companieshouse.api.company.CompanyDetails;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.chsmonitorapi.exception.ServiceException;

public interface CompanyProfileService {

    CompanyDetails getCompanyDetails(String companyNumber, String passthroughHeader)
            throws ServiceException;
}

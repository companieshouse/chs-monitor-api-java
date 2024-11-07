package uk.gov.companieshouse.chsmonitorapi.service;

import uk.gov.companieshouse.api.company.CompanyDetails;
import uk.gov.companieshouse.chsmonitorapi.exception.ServiceException;

public interface CompanyProfileService {

    CompanyDetails getCompanyDetails(String companyNumber) throws ServiceException;
}

package uk.gov.companieshouse.chsmonitorapi.service;

import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.chsmonitorapi.exception.ServiceException;

public interface CompanyProfileService {

    CompanyProfileApi getCompanyDetails(String companyNumber) throws ServiceException;
}

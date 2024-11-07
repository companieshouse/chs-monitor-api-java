package uk.gov.companieshouse.chsmonitorapi.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.company.CompanyDetails;
import uk.gov.companieshouse.chsmonitorapi.client.ApiClientService;
import uk.gov.companieshouse.chsmonitorapi.exception.ServiceException;
import uk.gov.companieshouse.chsmonitorapi.service.CompanyProfileService;

@Service
public class CompanyProfileServiceImpl implements CompanyProfileService {

    @Autowired
    private ApiClientService apiClientService;

    @Override
    public CompanyDetails getCompanyDetails(String companyNumber) throws ServiceException {
        return null;
    }
}

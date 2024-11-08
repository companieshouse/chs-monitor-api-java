package uk.gov.companieshouse.chsmonitorapi.service;

import java.util.List;
import uk.gov.companieshouse.chsmonitorapi.exception.ServiceException;
import uk.gov.companieshouse.chsmonitorapi.model.Subscription;

public interface SubscriptionService {

    List<Subscription> getSubscriptions(String companyNumber, int startIndex, int itemsPerPage)
            throws ServiceException;

    Subscription getSubscription(String companyNumber) throws ServiceException;

    void createSubscription(String companyNumber) throws ServiceException;

    void deleteSubscription(String companyNumber) throws ServiceException;

}

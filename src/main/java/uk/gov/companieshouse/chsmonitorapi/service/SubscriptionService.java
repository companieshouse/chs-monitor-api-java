package uk.gov.companieshouse.chsmonitorapi.service;

import org.springframework.data.domain.Page;
import uk.gov.companieshouse.chsmonitorapi.exception.ServiceException;
import uk.gov.companieshouse.chsmonitorapi.model.SubscriptionDocument;

public interface SubscriptionService {

    Page<SubscriptionDocument> getSubscriptions(String userId, int startIndex, int itemsPerPage)
            throws ServiceException;

    SubscriptionDocument getSubscription(String userId, String companyNumber)
            throws ServiceException;

    void createSubscription(String userId, String companyNumber) throws ServiceException;

    void deleteSubscription(String userId, String companyNumber) throws ServiceException;

}
